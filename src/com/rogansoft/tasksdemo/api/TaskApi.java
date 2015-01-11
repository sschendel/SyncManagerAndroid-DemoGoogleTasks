package com.rogansoft.tasksdemo.api;

import java.util.List;

import com.rogansoft.tasksdemo.domain.Task;
import com.rogansoft.tasksdemo.domain.TaskList;

public interface TaskApi {
	
	List<TaskList> getTaskList();
	
	Task get(String remoteId);
	List<Task> get();
	Task post(Task t);
	Task put(Task t);
}
