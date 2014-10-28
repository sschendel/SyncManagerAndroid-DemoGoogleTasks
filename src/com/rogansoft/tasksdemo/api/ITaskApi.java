package com.rogansoft.tasksdemo.api;

import java.util.List;

import com.rogansoft.tasksdemo.domain.Task;

public interface ITaskApi {
	Task get(String remoteId);
	List<Task> get();
	Task post(Task t);
	Task put(Task t);
}
