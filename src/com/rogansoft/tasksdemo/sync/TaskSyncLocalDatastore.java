package com.rogansoft.tasksdemo.sync;

import java.util.List;

import com.rogansoft.sync.IDatastore;
import com.rogansoft.tasksdemo.db.TaskDb;
import com.rogansoft.tasksdemo.domain.Task;

public class TaskSyncLocalDatastore implements IDatastore<Task> {
	private TaskDb mDb;

	@Override
	public List<Task> get() {
		return mDb.fetchAllTasks();
	}

	@Override
	public Task create() {
		return new Task();
	}

	@Override
	public Task add(Task localDataInstance) {
		long id = mDb.createTask(localDataInstance);
		Task result = mDb.fetchTask(id);
		return result;
	}

	@Override
	public Task update(Task localDataInstance) {
		mDb.updateTask(localDataInstance);
		Task result = mDb.fetchTask(localDataInstance.getId());
		return result;
	}

	public TaskSyncLocalDatastore(TaskDb mDb) {
		super();
		this.mDb = mDb;
	}

	
}
