package com.rogansoft.tasksdemo.sync;

import java.util.List;

import android.util.Log;

import com.rogansoft.sync.IDatastore;
import com.rogansoft.tasksdemo.api.ITaskApi;
import com.rogansoft.tasksdemo.domain.Task;

public class TaskSyncRemoteDatastore implements IDatastore<Task> {
	private static final String TAG = "TaskSyncRemoteDatastore";

	private ITaskApi mRemoteApi;
	
	@Override
	public List<Task> get() {
		return mRemoteApi.get();
	}

	@Override
	public Task create() {
		return new Task();
	}

	@Override
	public Task add(Task item) {
		Log.d(TAG, "addRemote:"+item.toString());
		Task result = mRemoteApi.post(item);
		Log.d(TAG, "afterPost:"+result.toString());
		return result;
	}

	@Override
	public Task update(Task item) {
		Log.d(TAG, "updateRemote:"+item.toString());
		Task result = mRemoteApi.put(item);
		return result;
	}

	public TaskSyncRemoteDatastore(ITaskApi api) {
		super();
		this.mRemoteApi = api;
	}
	
	
}
