package com.rogansoft.tasksdemo;

import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.rogansoft.syncmanagerexample.R;
import com.rogansoft.tasksdemo.db.TaskDb;
import com.rogansoft.tasksdemo.domain.Task;

public class TaskArrayAdapter extends ArrayAdapter<Task> {
	private static final String TAG = "TaskArrayAdapter";

	private ArrayList<Task> mTasks;
	private Context mContext;
	private TaskDb mDb;
	
	public TaskArrayAdapter(Context context, int textViewResourceId, ArrayList<Task> objects, TaskDb db) {
		super(context, textViewResourceId, objects);
		
		mTasks = objects;
		mContext = context;
		mDb = db;
		
	}

	private void updateViewGivenStatus(boolean isCompleted, TextView title, CheckBox status) {
        status.setChecked(isCompleted);
        if (isCompleted) {
            title.setPaintFlags(title.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            title.setPaintFlags(title.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
        }
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(R.layout.task_list_row, null);
        }
        final Task task = mTasks.get(position);
        if (task != null) {
	        final TextView title = (TextView) v.findViewById(R.id.task_title);
	        title.setText(task.getTitle());
	        final CheckBox status = (CheckBox) v.findViewById(R.id.task_status);
	        boolean isCompleted = checkIsTaskCompleted(task);
	        updateViewGivenStatus(isCompleted, title, status);
	        status.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View viewArg) {
					CheckBox cb = (CheckBox) viewArg;
					Log.d(TAG, "checked! "+task.getTitle());
					String statusTxt = "needsAction";
					if (cb.isChecked()) {
						statusTxt = "completed";
					}
					task.setStatus(statusTxt);
					//Calendar cal = Calendar.getInstance();
					Date date = new Date();
					long updatedSequence = date.getTime() / 1000;
					
					
					task.setLastUpdatedSequence(updatedSequence);
					Log.d(TAG, "last update: "+updatedSequence);
					mDb.updateTask(task);
					updateViewGivenStatus(cb.isChecked(), title, status);
				}
			});
        }
		return v;
	}

	private boolean checkIsTaskCompleted(Task task) {
		boolean result = false;
		String taskStatus = task.getStatus();
		if (taskStatus != null) {
			result = taskStatus.equalsIgnoreCase("completed");
		}
		return result;
	}

}
