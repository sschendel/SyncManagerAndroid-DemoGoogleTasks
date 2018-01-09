package com.rogansoft.tasksdemo.api;

import java.util.List;

import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;

import com.rogansoft.tasksdemo.domain.Task;
import com.rogansoft.tasksdemo.domain.TaskList;

public interface GoogleTaskApiService {
	static final String API_BASE_URL = "https://www.googleapis.com/tasks/v1";
	
	// Your Google Tasks task list id - see https://developers.google.com/google-apps/tasks/
	static final String GOOGLE_TASK_LIST_ID = "MDc1OTAzMTA4NTg1NDI2MDc5ODI6MDow";
	
	static final String API_KEY = "93945578907-dhmfkndf15mr093fbndvbvdecl5fb0e4.apps.googleusercontent.com";
	static final String SCOPE =
	        "oauth2:https://www.googleapis.com/auth/tasks";
	
	@GET("/users/@me/lists?key="+API_KEY)
	List<TaskList> getTaskLists();
	  
	@GET("/lists/{taskListId}/tasks?key="+API_KEY+"&showDeleted=true")
	List<Task> get(@Path("taskListId") String taskListId);

	@GET("/lists/{taskListId}/tasks/{taskId}?key="+API_KEY)
	Task get(@Path("taskListId") String taskListId, @Path("taskId") String taskId);
	
	@POST("/lists/{taskListId}/tasks?key="+API_KEY)
	Task post(@Path("taskListId") String taskListId, @Body Task task);

	@PUT("/lists/{taskListId}/tasks/{taskId}?key="+API_KEY)
	Task put(@Path("taskListId") String taskListId, @Path("taskId") String taskId, @Body Task task);
	
}
