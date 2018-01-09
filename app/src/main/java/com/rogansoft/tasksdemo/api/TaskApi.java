package com.rogansoft.tasksdemo.api;

import com.rogansoft.tasksdemo.domain.Task;
import com.rogansoft.tasksdemo.domain.TaskList;

import java.util.List;

public interface TaskApi {
	
	List<TaskList> getTaskList();
	
	Task get(String remoteId);
	List<Task> get();
	Task post(Task t);
	Task put(Task t);
}
