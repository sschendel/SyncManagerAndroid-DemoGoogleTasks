package com.rogansoft.tasksdemo.db;

import java.util.ArrayList;

import com.rogansoft.tasksdemo.domain.Task;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TaskDb {
	private static final String TAG = "TaskDb";
    private static final String DATABASE_NAME = "task_db";
    private static final int DATABASE_VERSION = 1;

	// Table names
	private static final String TASK_TABLE_NAME = "task";

    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;
    
    // task table column names
	public static String TASK_FIELD_NAME_ID = "id"; // long
	public static String TASK_FIELD_NAME_UPDATED = "updated"; // long
	public static String TASK_FIELD_NAME_TITLE = "title"; // String
	public static String TASK_FIELD_NAME_SERVER_ID = "server_id"; // String
	public static String TASK_FIELD_NAME_POSITION = "position"; // String
	public static String TASK_FIELD_NAME_STATUS = "status"; // String
	public static String TASK_FIELD_NAME_DELETED = "deleted"; // boolean

	//task table creation sql
	private static final String TASK_CREATE_QUERY =
		"create table "+TASK_TABLE_NAME+" (" +
			TASK_FIELD_NAME_ID + " integer primary key autoincrement not null," + 
			TASK_FIELD_NAME_UPDATED + " integer  null," + 
			TASK_FIELD_NAME_TITLE + " text not null," + 
			TASK_FIELD_NAME_SERVER_ID + " text  null," + 
			TASK_FIELD_NAME_POSITION + " text  null," + 
			TASK_FIELD_NAME_STATUS + " text  null," + 
			TASK_FIELD_NAME_DELETED + " integer null" + 
	");";
	
	// task all fields String[]
	private final String[] TASK_ALL_FIELDS = new String[] {
		TASK_FIELD_NAME_ID,
		TASK_FIELD_NAME_UPDATED,
		TASK_FIELD_NAME_TITLE,
		TASK_FIELD_NAME_SERVER_ID,
		TASK_FIELD_NAME_POSITION,
		TASK_FIELD_NAME_STATUS,
		TASK_FIELD_NAME_DELETED
	};	
	
    private static class DatabaseHelper extends SQLiteOpenHelper {

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "onCreate...");
			initialDbBuild(db);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.d(TAG, "onUpgrade... oldversion:"+oldVersion+" newVersion:"+newVersion);

			initialDbBuild(db);

		}

		private void initialDbBuild(SQLiteDatabase db) {
        	Log.d(TAG, "initialDbBuild");
            db.execSQL("DROP TABLE IF EXISTS "+TASK_TABLE_NAME);
            db.execSQL(TASK_CREATE_QUERY);
            
		}
		
    }    
    
    public TaskDb(Context ctx) {
        this.mCtx = ctx;
    }


	public TaskDb open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }    

	public static int fieldByNameInt(Cursor cur, String fldName) {
        return cur.getInt(cur.getColumnIndex(fldName));
	}

	public static String fieldByNameString(Cursor cur, String fldName) {
        return cur.getString(cur.getColumnIndex(fldName));
	}
	
	public static long fieldByNameLong(Cursor cur, String fldName) {
        return cur.getLong(cur.getColumnIndex(fldName));
	}

	public static double fieldByNameDouble(Cursor cur, String fldName) {
        return cur.getDouble(cur.getColumnIndex(fldName));
	}

	public static boolean fieldByNameBoolean(Cursor cur, String fldName) {
        return cur.getInt(cur.getColumnIndex(fldName)) == 1;
	}
    
	// Task CRUD

	// Used by user of content provider to build Task object from a cursor row.
	public static Task taskFromCursorRow(Cursor cur) {
		Task result;
		result = 
			new Task(
				fieldByNameLong(cur, TASK_FIELD_NAME_ID),
				fieldByNameLong(cur, TASK_FIELD_NAME_UPDATED),
				fieldByNameString(cur, TASK_FIELD_NAME_TITLE),
				fieldByNameString(cur, TASK_FIELD_NAME_SERVER_ID),
				fieldByNameString(cur, TASK_FIELD_NAME_POSITION),
				fieldByNameString(cur, TASK_FIELD_NAME_STATUS),
				fieldByNameBoolean(cur, TASK_FIELD_NAME_DELETED)
			);
		return result;
	}  

	// Used by user of content provider to setup fields for an insert
	public static ContentValues contentValuesFromTask(Task task, boolean includeId) {
		ContentValues initialValues = new ContentValues();
		if (includeId) { initialValues.put(TASK_FIELD_NAME_ID, task.getId()); } 
		initialValues.put(TASK_FIELD_NAME_UPDATED, task.getUpdated());
		initialValues.put(TASK_FIELD_NAME_TITLE, task.getTitle());
		initialValues.put(TASK_FIELD_NAME_SERVER_ID, task.getServerId());
		initialValues.put(TASK_FIELD_NAME_POSITION, task.getPosition());
		initialValues.put(TASK_FIELD_NAME_STATUS, task.getStatus());
		initialValues.put(TASK_FIELD_NAME_DELETED, task.isDeleted());
		return initialValues;
	}       
	
	// create
	static private long createTask(SQLiteDatabase db, Task task) {
		ContentValues initialValues = contentValuesFromTask(task, false);
		long result = db.insert(TASK_TABLE_NAME, null, initialValues);
		Log.d(TAG, "createTask result:"+result);
		for(String key : initialValues.keySet()) {
			Log.d(TAG, "key:"+key);
			if (key.equals(TASK_FIELD_NAME_SERVER_ID)) {
				Log.d(TAG, "serverid: "+initialValues.getAsString(key));
			}
		}
		return result;
	}

	public long createTask(Task task) {
		return createTask(mDb, task);
	}
    
	// delete
	public int deleteTask(String where, String[] whereArgs) {
		return mDb.delete(TASK_TABLE_NAME, where, whereArgs);
	}

	public int deleteTask(long rowId) {
		return deleteTask(TASK_FIELD_NAME_ID + "=" + rowId, null);
	}

	public int deleteAllTasks() {
		return deleteTask(null, null);
	}
	
	// read
	public Task fetchTask(long rowId) throws SQLException {
		Task result = null;

		Cursor cur =
			mDb.query(true, TASK_TABLE_NAME, TASK_ALL_FIELDS, TASK_FIELD_NAME_ID + "=" + rowId, null,
				null, null, null, null);

		if (cur.moveToFirst()) {
			result = taskFromCursorRow(cur);
		}
		cur.close();
		return result;
	}

	public ArrayList<Task> fetchAllTasks() {
		ArrayList<Task> result = new ArrayList<Task>();
		
		Cursor cur = mDb.query(TASK_TABLE_NAME, TASK_ALL_FIELDS, 
        		null, null, null, null, TASK_FIELD_NAME_POSITION);
				
		if (cur.moveToFirst()) {
			do {
				Task task = taskFromCursorRow(cur);
				result.add(task);
			} while (cur.moveToNext());
		}
		cur.close();
		return result;
	}

	public ArrayList<Task> fetchNonDeletedTasks() {
		ArrayList<Task> result = new ArrayList<Task>();
		
		Cursor cur = mDb.query(TASK_TABLE_NAME, TASK_ALL_FIELDS, 
        		TASK_FIELD_NAME_DELETED+" IS NULL OR "+TASK_FIELD_NAME_DELETED+" = 0", null, null, null, TASK_FIELD_NAME_POSITION);
				
		if (cur.moveToFirst()) {
			do {
				Task task = taskFromCursorRow(cur);
				result.add(task);
			} while (cur.moveToNext());
		}
		cur.close();
		return result;
	}
	
	
	// update
	public int updateTask(Task task) {
		ContentValues values = contentValuesFromTask(task, true);
		return mDb.update(TASK_TABLE_NAME, values, TASK_FIELD_NAME_ID + "=" + task.getId(), null);
	}    
	
}
