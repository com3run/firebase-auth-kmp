package dev.com3run.firebaseauthkmp

import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle

/**
 * Auto-initializes Firebase Auth KMP library using ContentProvider.
 * This runs automatically when the app starts - no manual initialization needed.
 */
class FirebaseAuthInitializer : ContentProvider() {
    override fun onCreate(): Boolean {
        val context = context ?: return false

        // Register activity lifecycle callbacks to auto-manage ActivityHolder
        if (context.applicationContext is Application) {
            val app = context.applicationContext as Application
            app.registerActivityLifecycleCallbacks(ActivityLifecycleTracker())
        }

        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}

/**
 * Tracks activity lifecycle to automatically set/clear ActivityHolder.
 */
internal class ActivityLifecycleTracker : Application.ActivityLifecycleCallbacks {
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        ActivityHolder.current = activity
    }

    override fun onActivityResumed(activity: Activity) {
        ActivityHolder.current = activity
    }

    override fun onActivityPaused(activity: Activity) {
        // Don't clear - keep reference for background operations
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (ActivityHolder.current == activity) {
            ActivityHolder.current = null
        }
    }

    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
}
