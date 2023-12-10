package com.example.workmanager

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import coil.compose.rememberImagePainter
import com.example.workmanager.ui.theme.WorkManagerTheme
import com.example.workmanager.workmanager.ColorFilterWorker
import com.example.workmanager.workmanager.DownloadWorker
import com.example.workmanager.workmanager.WorkerKeys

class MainActivity : ComponentActivity() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // OneTimeWorkRequestBuilder:- means we make only one work

        val downloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(
                        NetworkType.CONNECTED
                    )
                    .build()
            )
            .build()

        val colorFilterRequest = OneTimeWorkRequestBuilder<ColorFilterWorker>()
            .build()

        val workManager = WorkManager.getInstance(applicationContext)

        setContent {
            WorkManagerTheme {

                val workInfos = workManager
                    .getWorkInfosForUniqueWorkLiveData("download")
                    .observeAsState()
                    .value

                // recomposed only if workInfos change
                val downloadInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == downloadRequest.id }
                }

                // recomposed only if workInfos change
                val colorFilterInfo = remember(key1 = workInfos) {
                    workInfos?.find { it.id == colorFilterRequest.id }
                }

                // recomposed only if downloadInfo or colorFilterInfo change
                val imageUri by derivedStateOf {
                    val downloadUri =
                        downloadInfo?.outputData?.getString(WorkerKeys.IMAGE_URI)?.toUri()
                    val filteredImage =
                        colorFilterInfo?.outputData?.getString(WorkerKeys.FILTER_URI)?.toUri()
                    filteredImage ?: downloadUri
                }

                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    imageUri?.let { uri ->
                        Image(
                            painter = rememberImagePainter(data = uri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                    }

                    Button(
                        onClick = {
                            workManager
                                .beginUniqueWork(
                                    "download",
                                    ExistingWorkPolicy.KEEP, // policies to keep process working unless the app was killed
                                    downloadRequest
                                )
                                .then(colorFilterRequest)
                                .enqueue()
                        },
                        enabled = downloadInfo?.state != WorkInfo.State.RUNNING
                    ) {
                        Text(text = "Start download")
                    }
                    when(downloadInfo?.state) {
                        WorkInfo.State.RUNNING -> Text(text = "Downloading...")
                        WorkInfo.State.SUCCEEDED -> Text(text = "Download Succeeded...")
                        WorkInfo.State.FAILED -> Text("Download failed")
                        WorkInfo.State.CANCELLED -> Text("Download cancelled")
                        WorkInfo.State.ENQUEUED -> Text("Download enqueued")
                        WorkInfo.State.BLOCKED -> Text("Download blocked")
                        else -> Text("Download")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    when(colorFilterInfo?.state) {
                        WorkInfo.State.RUNNING -> Text("Applying filter...")
                        WorkInfo.State.SUCCEEDED -> Text("Filter succeeded")
                        WorkInfo.State.FAILED -> Text("Filter failed")
                        WorkInfo.State.CANCELLED -> Text("Filter cancelled")
                        WorkInfo.State.ENQUEUED -> Text("Filter enqueued")
                        WorkInfo.State.BLOCKED -> Text("Filter blocked")
                        else -> Text("Filter")

                    }

                }
            }
        }
    }

}










