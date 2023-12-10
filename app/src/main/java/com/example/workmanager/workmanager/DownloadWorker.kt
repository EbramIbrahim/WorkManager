package com.example.workmanager.workmanager

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.workmanager.ImageApi
import com.example.workmanager.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import kotlin.random.Random

class DownloadWorker(
    private val context: Context,
    workerParameters: WorkerParameters
) : CoroutineWorker(context, workerParameters) {


    override suspend fun doWork(): Result {
        startForegroundService()
        delay(5000L)
        val image = ImageApi.api.downloadImage()
        image.body()?.let { body ->
            return withContext(Dispatchers.IO) {
                val file = File(context.cacheDir, "image.jpg")
                val outputStream = FileOutputStream(file)
                outputStream.use { stream ->
                    try {
                        stream.write(body.bytes())
                    } catch (e: IOException) {
                        return@withContext Result.failure(
                            workDataOf(
                                WorkerKeys.ERROR_MSG to e.localizedMessage
                            )
                        )
                    }
                }
                Result.success(
                    workDataOf(
                        WorkerKeys.IMAGE_URI to file.toUri().toString()
                    )
                )
            }
        }

        if (!image.isSuccessful) {
            if (image.code().toString().startsWith("5")) {
                return Result.retry()
            }
            return Result.failure(
                workDataOf(
                    WorkerKeys.ERROR_MSG to "Network Error"
                )
            )
        }

        return Result.failure(
            workDataOf(
                WorkerKeys.ERROR_MSG to "Unknown Error"
            )
        )

    }


    private suspend fun startForegroundService() {

        setForeground(
            ForegroundInfo(
                Random.nextInt(),
                NotificationCompat.Builder(context, "download_channel")
                    .setSmallIcon(R.drawable.ic_download)
                    .setContentText("Downloading")
                    .setContentTitle("Download Image in progress...")
                    .build()
            )
        )

    }


}













