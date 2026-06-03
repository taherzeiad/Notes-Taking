package com.example.notes_taking.Worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.concurrent.TimeUnit

object AiWorkScheduler {

    fun scheduleAiProcessing(
        context: Context,
        noteId: Int,
        noteDate: String
    ) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // ← يعمل فقط عند توفر النت
            .build()

        val inputData = workDataOf(
            "note_id" to noteId,
            "note_date" to noteDate
        )

        val request = OneTimeWorkRequestBuilder<NoteAiWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                30, TimeUnit.SECONDS  // ← أعد المحاولة بعد 30 ثانية
            )
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}