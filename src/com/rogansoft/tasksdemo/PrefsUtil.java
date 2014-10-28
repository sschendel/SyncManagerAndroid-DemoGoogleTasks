package com.rogansoft.tasksdemo;

import android.content.Context;

public class PrefsUtil {
    private static final String GOOGLE_TASKS_PREFERENCES = "GOOGLE_TASKS_PREFERENCES";

    private static final String KEY_GOOGLE_TASKS_USER = "KEY_GOOGLE_TASKS_USER";

    public static void saveGoogleTasksUser(Context context, String userName) {
        context.getSharedPreferences(GOOGLE_TASKS_PREFERENCES, Context.MODE_MULTI_PROCESS)
                .edit()
                .putString(KEY_GOOGLE_TASKS_USER, userName)
                .commit();
    }

    public static String retrieveGoogleTasksUser(Context context) {
        return context.getSharedPreferences(GOOGLE_TASKS_PREFERENCES, Context.MODE_MULTI_PROCESS)
                .getString(KEY_GOOGLE_TASKS_USER, null);
    }
}
