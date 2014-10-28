package com.rogansoft.tasksdemo.api;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.Date;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.rogansoft.tasksdemo.Util;
import com.rogansoft.tasksdemo.domain.Task;
import com.rogansoft.tasksdemo.domain.TaskList;

public class TaskApi implements ITaskApi {
	//private static final String TAG = "TaskApi";

	private RestAdapter mRestAdapter;
	private GoogleTaskApiService mService;
	
	class TaskTypeAdapter extends TypeAdapter<Task> {

		private long parseRFC3339Date(String dateStr) {
			long result = 0; 
			try {
				Date date = Util.parseRFC3339Date(dateStr);
				result = date.getTime() / 1000;
			} catch (IndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
		public Task read(JsonReader in) throws IOException {
			final Task task = new Task();
			
			in.beginObject();
			
			while(in.hasNext()) {
				//Log.d(TAG, "json peek: "+in.peek().toString());
				String name = in.nextName();
				//Log.d(TAG, "json name: "+name);
				if (name.equals("id")){
					task.setServerId(in.nextString());
				} else if (name.equals("title")) {
					task.setTitle(in.nextString());
				} else if (name.equals("updated")) {
					task.setUpdated(parseRFC3339Date(in.nextString()));
				} else if (name.equals("status")) {
					task.setStatus(in.nextString());
				} else if (name.equals("position")) {
					task.setPosition(in.nextString());
				} else if (name.equals("deleted")) {
					//Log.d(TAG, "json deleted peek: "+in.peek().toString());
					task.setDeleted(in.nextBoolean());
					//Log.d(TAG, "task deleted:"+task.isDeleted());
				} else {
					in.skipValue();
				}
			}
			
			in.endObject();
			return task;
		}

		@Override
		public void write(JsonWriter out, Task task) throws IOException {
			out.beginObject();
			if (task.getServerId() != null) {
				out.name("id").value(task.getServerId());
			}
		    out.name("title").value(task.getTitle());
		    out.name("status").value(task.getStatus());
		    out.endObject();
		}
		
	}
	

	class TaskListDeserializer<T> implements JsonDeserializer<T>{

		@Override
		public T deserialize(JsonElement je, Type type,
				JsonDeserializationContext jdc) throws JsonParseException {
			// Get the "content" element from the parsed JSON
	        JsonElement items = je.getAsJsonObject().get("items");

		    Gson gson = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
	        .registerTypeAdapter(Task.class, new TaskTypeAdapter())
	        .create();

	        // Deserialize it. You use a new instance of Gson to avoid infinite recursion
	        // to this deserializer
	        return gson.fromJson(items, type);	
	        
		}
	}
	
	
	public TaskApi(String authToken) {
		final String token = authToken;

		RequestInterceptor requestInterceptor = new RequestInterceptor() {
			  @Override
			  public void intercept(RequestFacade request) {
			    request.addHeader("Authorization", "Bearer "+token);
			  }
		};
		
		Gson gson = 
			    new GsonBuilder()
        			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			        .registerTypeAdapter(List.class, new TaskListDeserializer<List<TaskList>>())
			        .registerTypeAdapter(List.class, new TaskListDeserializer<List<Task>>())
			        .registerTypeAdapter(Task.class, new TaskTypeAdapter())
			        .create();

		mRestAdapter = new RestAdapter.Builder()
			.setRequestInterceptor(requestInterceptor)
			.setEndpoint(GoogleTaskApiService.API_BASE_URL)
			.setConverter(new GsonConverter(gson))
			.build();
				
		//Log.d(TAG, "retrofit log level:"+mRestAdapter.getLogLevel());
		//mRestAdapter.setLogLevel(LogLevel.FULL);
		//Log.d(TAG, "retrofit log level after setting full:"+mRestAdapter.getLogLevel());
		
		mService = mRestAdapter.create(GoogleTaskApiService.class);
	}
	
	public List<TaskList> getTaskList(){
		return mService.getTaskLists();
	}
	
	@Override
	public Task get(String remoteId) {
		return mService.get(GoogleTaskApiService.GOOGLE_TASK_LIST_ID, remoteId);
	}

	@Override
	public List<Task> get() {
		return mService.get(GoogleTaskApiService.GOOGLE_TASK_LIST_ID);
	}

	@Override
	public Task post(Task t) {
		return mService.post(GoogleTaskApiService.GOOGLE_TASK_LIST_ID, t);
	}

	@Override
	public Task put(Task t) {
		return mService.put(GoogleTaskApiService.GOOGLE_TASK_LIST_ID, t.getRemoteId(), t);
	}

}
