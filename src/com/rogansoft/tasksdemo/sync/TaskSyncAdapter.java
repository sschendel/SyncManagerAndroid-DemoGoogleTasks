package com.rogansoft.tasksdemo.sync;

import java.io.IOException;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableNotifiedException;
import com.rogansoft.sync.SyncManager;
import com.rogansoft.tasksdemo.PrefsUtil;
import com.rogansoft.tasksdemo.api.GoogleTaskApiService;
import com.rogansoft.tasksdemo.api.ITaskApi;
import com.rogansoft.tasksdemo.api.TaskApi;
import com.rogansoft.tasksdemo.db.TaskDb;
import com.rogansoft.tasksdemo.domain.Task;

public class TaskSyncAdapter extends AbstractThreadedSyncAdapter {
	private static final String TAG = "TaskSyncAdapter";

	private Context mContext;
	
	public TaskSyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		
		mContext = context;
		
	}

	public TaskSyncAdapter(Context context, boolean autoInitialize,
			boolean allowParallelSyncs) {
		super(context, autoInitialize, allowParallelSyncs);
		mContext = context;
	}
	
	
	private String getGoogleAuthToken() throws IOException {
		String googleUserName = PrefsUtil.retrieveGoogleTasksUser(mContext);
		
		String token = "";
		try {
			Log.d(TAG, "getGoogleAuthToken... "+googleUserName);
			token = GoogleAuthUtil.getTokenWithNotification(mContext,
					googleUserName, GoogleTaskApiService.SCOPE, null);
		} catch (UserRecoverableNotifiedException userNotifiedException) {
			// Notification has already been pushed.
			// Continue without token or stop background task.
		} catch (GoogleAuthException authEx) {
			// This is likely unrecoverable.
			Log.e(TAG,
					"Unrecoverable authentication exception: "
							+ authEx.getMessage(), authEx);
		}
		return token;
	}
	
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.d(TAG, "onPerformSync");
		try {
			ITaskApi api = new TaskApi(getGoogleAuthToken());
			TaskDb db = new TaskDb(mContext);
	
			db.open();
			try {
				TaskSyncLocalDatastore localDatastore = new TaskSyncLocalDatastore(db); 
				TaskSyncRemoteDatastore remoteDatastore = new TaskSyncRemoteDatastore(api);
				SyncManager<Task, Task> syncManager = new SyncManager<Task, Task>(localDatastore, remoteDatastore);

				syncManager.sync();
				
			}	finally {
				db.close();
			}
			getContext().getContentResolver().notifyChange(TaskSyncContentProvider.CONTENT_URI, null);
			
		} catch (Exception e) {
			Log.e(TAG, "syncFailed:" + e.getMessage());
		}	
	}

}
