package ch.ost.aifo.todolist;

import java.util.Hashtable;
import java.util.Map;


import com.google.protobuf.Value;

public class Todolist {
	private Hashtable<String, Task> todolist;

	public Todolist() {
		this.todolist = new Hashtable<String, Task>();
	}
		
	public void addTask(Map<String, Value> map) {
		String name = map.get("task").getStringValue();
		String priority = map.get("priority").getStringValue();
		if (todolist.containsKey(name)) {
			System.out.println("A task with this name exists already. Please create a new Task with a different name or edit this one.");
			return;
		}
		todolist.put(name, new Task(name, priority));
		System.out.println("I added \"" + name + "\" to your List with " + priority + " priority");
	}
	
	public void removeTask(Map<String, Value> map) {
		String name = map.get("task").getStringValue();
		if (todolist.remove(name) != null) {
			System.out.println("I removed \"" + name + "\" from your List");
		}
		else {
			System.out.println("I couldn't find \"" + name + "\" on your list");
		}
	}
	
	public void printTasks() {
		if (todolist.isEmpty()) {
			System.out.println("This list is empty");
			return;
		}
		System.out.println("Here you go: ");
		for (Task entry: todolist.values()) {
			System.out.println(" - " + entry.getName() + " ("+entry.getPriority()+")");
		}
	}
	
}
