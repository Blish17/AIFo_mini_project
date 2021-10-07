package ch.ost.aifo.todolist;

public class Task {
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
