package com.rogansoft.tasksdemo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.rogansoft.syncmanagerexample.R;
import com.rogansoft.tasksdemo.api.GoogleTaskApi;
import com.rogansoft.tasksdemo.api.GoogleTaskApiService;
import com.rogansoft.tasksdemo.api.TaskApi;
import com.rogansoft.tasksdemo.db.TaskDb;
import com.rogansoft.tasksdemo.domain.Task;
import com.rogansoft.tasksdemo.domain.TaskList;
import com.rogansoft.tasksdemo.sync.TaskSyncContentProvider;


public class MainActivity extends Activity {
	private static final String TAG = "MainActivity";

	static final int REQUEST_CODE_PICK_ACCOUNT = 1000;
	static final int REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR = 2000;
	static final int ACTIVITY_ADD_TASK = 3000;
	
	private String mGoogleAccountName;
	private ListView mList;
	private ArrayList<Task> mTasks;
	
	private TaskDb mDb;
	
	private Handler handler;
	private TaskContentObserver mTaskContentObserver;
	
	class TaskContentObserver extends ContentObserver {

		public TaskContentObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			Log.d(TAG, "Task content changed!");
			loadModel();
			updateUI();
		}
		
	}
	

	// Set up content observer for our content provider
	private void registerContentObservers() {
		ContentResolver cr = getContentResolver();
		handler = new Handler();		
		mTaskContentObserver = new TaskContentObserver(handler);
		cr.registerContentObserver(TaskSyncContentProvider.CONTENT_URI, true,
				mTaskContentObserver);
	}

	private void unregisterContentObservers() {
		ContentResolver cr = getContentResolver();
		if (mTaskContentObserver != null) { // just paranoia
			cr.unregisterContentObserver(mTaskContentObserver);
			mTaskContentObserver = null;
			handler = null;
		}
	}	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		mDb = new TaskDb(this);
        mDb.open();

	    mList = (ListView) findViewById(R.id.task_list);
	    
	    final View relLayoutButton = (View) findViewById(R.id.button);
	    relLayoutButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//Log.d(TAG, "TBD: Add task view...");
				launchActivityAddTask();
			}
		});	    
	    
	}
	
	@Override
	protected void onDestroy() {
		mDb.close();
		super.onDestroy();
	}	

	@Override
	protected void onStart() {
		super.onStart();
		registerContentObservers();
	}

	@Override
	protected void onStop() {
		unregisterContentObservers();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		int conResult = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		Log.d(TAG, "GooglePlayServices available? "+ conResult);
		
		mGoogleAccountName = PrefsUtil.retrieveGoogleTasksUser(this);
		if (mGoogleAccountName == null) {
			pickUserAccount();
		}
		
		loadModel();
		updateUI();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.action_sync:
        	initiateSync();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }	
	
	private void initiateSync() {
		Log.d(TAG, "initiateSync... google account:"+ mGoogleAccountName);
		Account account = getGoogleAccount(mGoogleAccountName);
		if (account != null) {
			Log.d(TAG, "Found account! init sync...");
	        // Pass the settings flags by inserting them in a bundle
	        Bundle settingsBundle = new Bundle();
	        settingsBundle.putBoolean(
	                ContentResolver.SYNC_EXTRAS_MANUAL, true);
	        settingsBundle.putBoolean(
	                ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
	        /*
	         * Request the sync for the default account, authority, and
	         * manual sync settings
	         */
	        ContentResolver.requestSync(account, TaskSyncContentProvider.AUTHORITY, settingsBundle);		
			
		}
	}

	private void loadModel() {
		mTasks = mDb.fetchNonDeletedTasks();
		for(Task t : mTasks) {
			Log.d(TAG,t.toString());
		}
	}
	
	
	private void updateUI() {
		TaskArrayAdapter adapter = new TaskArrayAdapter(this, R.layout.task_list_row, mTasks, mDb);
		mList.setAdapter(adapter);
	}

    private void launchActivityAddTask() {
    	Intent i = new Intent(this, AddTaskActivity.class);
    	startActivityForResult(i, ACTIVITY_ADD_TASK);    	
    }	

    private Account getGoogleAccount(String userName) {
    	Account result = null;
		AccountManager manager = AccountManager.get(this);
	    Account[] accounts = manager.getAccountsByType("com.google");
	    for(Account account : accounts) {
    		Log.d(TAG, "check account:"+account.name);
	    	if (account.name.equals(userName)) {
	    		Log.d(TAG, "Found!  Setting syncable");
	    		result = account;
	    		break;
	    	}
	    }
	    return result;
    }
    
	private void setGoogleAccountSync(String userName) {
		Log.d(TAG, "setGoogleAccountSync:"+userName);
		Account account = getGoogleAccount(userName);
		if (account != null) {
    		Log.d(TAG, "Found!  Setting syncable");
    		ContentResolver.setIsSyncable(account, "com.rogansoft.syncmanagerexample.sync.examplesyncadapterservice", 1);
		}
	}
    
    
	protected void pickUserAccount() {
	    String[] accountTypes = new String[]{"com.google"};
	    Intent intent = AccountPicker.newChooseAccountIntent(null, null,
	            accountTypes, false, null, null, null, null);
	    startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
	    if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
	        // Receiving a result from the AccountPicker
	        if (resultCode == RESULT_OK) {
	        	mGoogleAccountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
	        	PrefsUtil.saveGoogleTasksUser(this, mGoogleAccountName);
	        	setGoogleAccountSync(mGoogleAccountName);
	            // With the account name acquired, go get the auth token
	            initiateGoogleAuthTokenRequest();
	        } else if (resultCode == RESULT_CANCELED) {
	            // The account picker dialog closed without selecting an account.
	            // Notify users that they must pick an account to proceed.
	            Toast.makeText(this, "You must choose a Google account", Toast.LENGTH_SHORT).show();
	        }
	    } else if (requestCode == REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR) {
	        if (resultCode == RESULT_OK) {
	            initiateGoogleAuthTokenRequest();
	        } 
	    } else if (requestCode == ACTIVITY_ADD_TASK) {
        	if (intent != null) {  // may be null if user backed up 
	        	Bundle extras = intent.getExtras();
	    	    String title = extras.getString("title");
	    	    
	    	    Task newTask = new Task();
	    	    newTask.setTitle(title);
	    	    newTask.setId(mDb.createTask(newTask));
	    	    loadModel();
	    	    updateUI();
        	}
	    	
	    }
	}	
	
	private void initiateGoogleAuthTokenRequest() {
	    if (mGoogleAccountName == null) {
	        pickUserAccount();
	    } else {
	        //if (isDeviceOnline()) {
	            new GetGoogleAuthTokenTask(mGoogleAccountName, GoogleTaskApiService.SCOPE).execute();
	        //} else {
	        //    Toast.makeText(this, R.string.not_online, Toast.LENGTH_LONG).show();
	       // }
	    }
	}

	protected void handleException(UserRecoverableAuthException e) {
            // Unable to authenticate, such as when the user has not yet granted
            // the app access to the account, but the user can fix this.
            // Forward the user to an activity in Google Play services.
            Intent intent = e.getIntent();
            startActivityForResult(intent,
                    REQUEST_CODE_RECOVER_FROM_PLAY_SERVICES_ERROR);
	}
	
	public class GetGoogleTaskListTasksTask extends AsyncTask<Void, Void, List<Task>>{
		private TaskApi mApi;
		private String mAuthToken;
		
		public GetGoogleTaskListTasksTask(String authToken) {
			mApi = new GoogleTaskApi(authToken);
			mAuthToken = authToken;
		}
		
		@Override
		protected List<Task> doInBackground(Void... params) {
			return mApi.get();
		}
		
		@Override
		protected void onPostExecute(List<Task> result) {
			Log.d(TAG, "done");
			
			for(Task t : result){
				Log.d(TAG, t.toString());
				
				if(t.getRemoteId().equals("MDc1OTAzMTA4NTg1NDI2MDc5ODI6MDoxMjU0MDgwNDQ5")) {
					Log.d(TAG, "Updating this task...");
					t.setTitle("Updated task title through API");
					new UpdateGoogleTaskTask(mAuthToken, t).execute();
				}
				
			}
		}
	}
	
	public class GetGoogleTaskListTask extends AsyncTask<Void, Void, List<TaskList>>{
		private TaskApi mApi;
		
		public GetGoogleTaskListTask(String authToken) {
			mApi = new GoogleTaskApi(authToken);
		}
		
		@Override
		protected List<TaskList> doInBackground(Void... params) {
			return mApi.getTaskList();
		}
		
		@Override
		protected void onPostExecute(List<TaskList> result) {
			Log.d(TAG, "done");
			
			for(TaskList tl : result){
				Log.d(TAG, tl.toString());
			}
		}
	}

	
	public class AddGoogleTaskTask extends AsyncTask<Void, Void, Task>{
		private TaskApi mApi;
		private Task mTask;
		
		public AddGoogleTaskTask(String authToken, Task task) {
			mApi = new GoogleTaskApi(authToken);
			mTask = task;
		}
		
		@Override
		protected Task doInBackground(Void... params) {
			return mApi.post(mTask);
		}
		
		@Override
		protected void onPostExecute(Task result) {
			Log.d(TAG, "AddGoogleTaskTask done");
			
			Log.d(TAG, result.toString());
			
		}
	}
	
	
	public class UpdateGoogleTaskTask extends AsyncTask<Void, Void, Task>{
		private TaskApi mApi;
		private Task mTask;
		
		public UpdateGoogleTaskTask(String authToken, Task task) {
			mApi = new GoogleTaskApi(authToken);
			mTask = task;
		}
		
		@Override
		protected Task doInBackground(Void... params) {
			return mApi.put(mTask);
		}
		
		@Override
		protected void onPostExecute(Task result) {
			Log.d(TAG, "UpdateGoogleTaskTask done");
			
			Log.d(TAG, result.toString());
		}
	}
		
	
	public class GetGoogleAuthTokenTask extends AsyncTask<Void,Void,String>{
	    String mScope;
	    String mEmailText;
	    String mTokenText;
	    
	    UserRecoverableAuthException mException;

	    GetGoogleAuthTokenTask(String name, String scope) {
	        this.mScope = scope;
	        this.mEmailText = name;
	    }

	    /**
	     * Executes the asynchronous job. This runs when you call execute()
	     * on the AsyncTask instance.
	     */
	    @Override
	    protected String doInBackground(Void... params) {
	        try {
	            String token = fetchToken();
	            if (token != null) {
	                // Insert the good stuff here.
	                // Use the token to access the user's Google data.
	            	mTokenText = token;
	            }
	        } catch (IOException e) {
	            // The fetchToken() method handles Google-specific exceptions,
	            // so this indicates something went wrong at a higher level.
	            // TIP: Check for network connectivity before starting the AsyncTask.
	        	Log.e(TAG, e.getMessage(), e);
	        }
	        return null;
	    }

	    @Override
	    protected void onPostExecute(String result) {
	    	if (mException != null ) {
	    		MainActivity.this.handleException(mException);
	    	}
	    	else {
		    	if (mTokenText != null){
		    		Log.d(TAG,"token:"+mTokenText);
		    		//new GetGoogleTaskListTask(mTokenText).execute();
		    		new GetGoogleTaskListTasksTask(mTokenText).execute();
		    		
//		    		Task task = new Task();
//		    		task.setTitle("New posted from API!");
//		    		new AddGoogleTaskTask(mTokenText, task).execute();
		    	}
	    	}
	    	
	    }
	    
	    
	    /**
	     * Gets an authentication token from Google and handles any
	     * GoogleAuthException that may occur.
	     */
	    protected String fetchToken() throws IOException {
	        try {
	            return GoogleAuthUtil.getToken(MainActivity.this, mEmailText, mScope);
	        } catch (UserRecoverableAuthException userRecoverableException) {
	            // GooglePlayServices.apk is either old, disabled, or not present
	            // so we need to show the user some UI in the activity to recover.
	            //mActivity.handleException(userRecoverableException);
	        	//Log.e(TAG, userRecoverableException.getMessage(), userRecoverableException);
	        	
	        	mException = userRecoverableException;
	        } catch (GoogleAuthException fatalException) {
	            // Some other type of unrecoverable exception has occurred.
	            // Report and log the error as appropriate for your app.
	        	Log.e(TAG, fatalException.getMessage(), fatalException);
	        }
	        return null;
	    }
	}	
	
	
}
