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
		todolist.put(name, new Task(name));
		System.out.println("I added \"" + name + "\" to your List");
	}
	
	public void printTasks() {
		System.out.println("Here you go: ");
		for (Task entry: todolist.values()) {
			System.out.println(" - " + entry.getName());
		}
	}
	
}
