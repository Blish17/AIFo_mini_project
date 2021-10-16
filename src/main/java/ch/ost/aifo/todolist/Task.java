package ch.ost.aifo.todolist;

import java.io.Serializable;

public class Task implements Serializable{
	private static final long serialVersionUID = 1L;
	private String name;
	private String priority;
	
	public Task(String name, String priority) {
		this.name = name;
		this.priority = priority;
	}

	public String getName() {
		return name;
	}
	
	public String getPriority() {
		return priority;
	}
	
}
