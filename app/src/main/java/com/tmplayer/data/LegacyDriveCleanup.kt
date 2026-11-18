package com.tmplayer.data

import android.app.job.JobScheduler
import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException
import java.security.KeyStore

/** Clears only this app's obsolete local Drive state; it makes no request to Google. */
internal object LegacyDriveCleanup {
    private const val TAG = "LegacyDriveCleanup"
    private const val MIGRATIONS = "tmplayer_feature_migrations"
    private const val MIGRATION_KEY = "legacy_drive_removed_v1"
    private const val DRIVE_PREFERENCES = "google_drive_preferences"
    private const val DRIVE_KEY_ALIAS = "com.tmplayer.google-drive.state.v1"
    private const val WORK_MANAGER_SERVICE = "androidx.work.impl.background.systemjob.SystemJobService"
    private const val WORK_MANAGER_DATABASE = "androidx.work.workdb"

    fun run(context: Context) {
        val app = context.applicationContext
        val migrations = app.getSharedPreferences(MIGRATIONS, Context.MODE_PRIVATE)
        if (migrations.getBoolean(MIGRATION_KEY, false)) return

        val cleaned = runCatching {
            val drivePreferences = app.getSharedPreferences(DRIVE_PREFERENCES, Context.MODE_PRIVATE)
            if (!drivePreferences.edit().clear().commit()) {
                throw IOException("Could not clear legacy Drive preferences")
            }

            val driveState = File(app.noBackupFilesDir, "google-drive")
            if (driveState.exists() && !driveState.deleteRecursively()) {
                throw IOException("Could not delete legacy Drive state")
            }

            val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
            if (keyStore.containsAlias(DRIVE_KEY_ALIAS)) keyStore.deleteEntry(DRIVE_KEY_ALIAS)

            val scheduler = app.getSystemService(JobScheduler::class.java)
                ?: throw IllegalStateException("JobScheduler is unavailable")
            scheduler.allPendingJobs.orEmpty()
                .filter { job ->
                    job.service?.let {
                        it.packageName == app.packageName && it.className == WORK_MANAGER_SERVICE
                    } == true
                }
                .forEach { scheduler.cancel(it.id) }

            val workDatabase = app.getDatabasePath(WORK_MANAGER_DATABASE)
            if (workDatabase.exists() && !app.deleteDatabase(WORK_MANAGER_DATABASE)) {
                throw IOException("Could not delete obsolete upload work")
            }
        }.onFailure {
            Log.w(TAG, "Could not clear all legacy local Drive state; will retry on next start", it)
        }.isSuccess

        if (cleaned && !migrations.edit().putBoolean(MIGRATION_KEY, true).commit()) {
            Log.w(TAG, "Could not save the legacy Drive cleanup marker")
        }
    }
}
