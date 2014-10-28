package com.rogansoft.tasksdemo.domain;

import com.rogansoft.sync.ISyncable;

public class Task implements ISyncable {


	// fields
	private long id;
	private long updated;
	private String title;
	private String serverId;
	private String position;
	private String status;
	private boolean deleted;
	
	// getters 

  	public long getId() {
  		return id;
  	}

  	public long getUpdated() {
  		return updated;
  	}

  	public String getTitle() {
  		return title;
  	}

  	public String getServerId() {
  		return serverId;
  	}

  	public String getPosition() {
  		return position;
  	}

  	public String getStatus() {
  		return status;
  	}

  	public boolean isDeleted() {
  		return deleted;
  	}
  	
	// setters
  	public void setId(long id) {
  		this.id = id;
  	}

  	public void setUpdated(long updated) {
  		this.updated = updated;
  	}

  	public void setTitle(String title) {
  		this.title = title;
  	}

  	public void setServerId(String serverId) {
  		this.serverId = serverId;
  	}

  	public void setPosition(String position) {
  		this.position = position;
  	}

  	public void setStatus(String status) {
  		this.status = status;
  	}

  	public void setDeleted(boolean deleted) {
  		this.deleted = deleted;
  	}

	// copy
	public Task copy() {
		Task result = new Task(
			this.id ,
			this.updated ,
			this.title ,
			this.serverId ,
			this.position ,
			this.status ,
			this.deleted
		);
		return result;
	}
	
	// constructor
	public Task(long id, long updated, String title, String serverId, String position, String status, boolean deleted) {
		super();
		this.id = id; 
		this.updated = updated; 
		this.title = title; 
		this.serverId = serverId; 
		this.position = position; 
		this.status = status; 
		this.deleted = deleted;
	}
	
	public Task() {
		super();
	}	
	
	// Implement ISyncable...
	
	@Override
	public String getRemoteId() {
		return serverId;
	}

	@Override
	public void setRemoteId(String id) {
		this.serverId = id;
	}

	@Override
	public Long getLastUpdatedSequence() {
		return updated;
	}

	@Override
	public void setLastUpdatedSequence(Long value) {
		this.updated = value;
		
	}

	@Override
	public void mapFromRemote(ISyncable remote) {
		Task remoteTask = (Task) remote;
		
		setTitle(remoteTask.getTitle());
		setStatus(remoteTask.getStatus());

		// server read only
		setPosition(remoteTask.getPosition());
		setServerId(remote.getRemoteId());
		setLastUpdatedSequence(remote.getLastUpdatedSequence());
		setDeleted(remoteTask.isDeleted());
		
	}

	@Override
	public void mapFromLocal(ISyncable local) {
		Task localTask = (Task) local;
		
		setTitle(localTask.getTitle());
		setStatus(localTask.getStatus());
		setDeleted(localTask.isDeleted());
		
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", updated=" + updated + ", title=" + title
				+ ", serverId=" + serverId + ", position=" + position
				+ ", status=" + status + ", deleted="+deleted+"]";
	}
	
	
	
}