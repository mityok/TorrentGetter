package com.mityok.inter;

public class NotificationItem {
	public enum Status {
		FAIL, SUCCESS, INFO
	}

	private Status status;
	private String message;

	public NotificationItem(Status status, String message) {
		this.status = status;
		this.message = message;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
