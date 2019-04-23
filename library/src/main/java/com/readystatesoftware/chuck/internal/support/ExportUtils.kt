package com.readystatesoftware.chuck.internal.support

import android.content.Context
import android.os.Environment
import android.widget.Toast
import com.readystatesoftware.chuck.internal.data.ChuckContentProvider
import com.readystatesoftware.chuck.internal.data.HttpTransaction
import com.readystatesoftware.chuck.internal.data.LocalCupboard
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ExportUtils(private val context: Context) {

    fun export(appExecutors: AppExecutors) {
        appExecutors.diskIO().execute {
            if (isExternalStorageWritable()) {
                val cursor = context.contentResolver.query(ChuckContentProvider.TRANSACTION_URI,
                        null, null, null, null) ?: return@execute

                cursor.moveToFirst()

                val transactions = LocalCupboard.getInstance().withCursor(cursor).list(HttpTransaction::class.java)

                val directory = getPrivateStorageDirectory(context, "feeds-capture")

                directory?.let {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.getDefault())

                    for (transaction in transactions) {
                        val formattedUrl = transaction.url.replace("[^a-zA-Z0-9\\.\\-]".toRegex(), "_")
                        val prefix = dateFormat.format(Date()) + "_$formattedUrl"

                        val fileName = when {
                            transaction.responseCode != null && transaction.responseCode >= 500 -> prefix + "_ERROR_500"
                            transaction.responseCode != null && transaction.responseCode >= 400 -> prefix + "_ERROR_400"
                            transaction.responseCode != null && transaction.malformedJson == 1 -> prefix + "_MALFORMED"
                            else -> prefix
                        }

                        val file = File(it, "$fileName.txt")

                        file.writeText(FormatUtils.getShareText(context, transaction))
                    }

                    appExecutors.mainThread().execute {
                        Toast.makeText(context, "All transactions logged successfully: check your 'feeds-capture' folder", Toast.LENGTH_LONG).show()
                    }
                }

                cursor.close()
            }
        }
    }

    fun delete(appExecutors: AppExecutors) {
        appExecutors.diskIO().execute {
            if (isExternalStorageWritable()) {
                val directory = getPrivateStorageDirectory(context, "feeds-capture")
                directory?.deleteRecursively()

                appExecutors.mainThread().execute {
                    Toast.makeText(context, "All transactions logs deleted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun getPrivateStorageDirectory(context: Context, directoryName: String): File? {
        val file = File(context.getExternalFilesDir(null), directoryName)

        if (!file.mkdirs()) {
            // Not created...
        }

        return file
    }
}