package ch.ost.aifo.todolist;

import java.util.Hashtable;
import java.util.Map;

import com.google.protobuf.Value;

public class TodolistManager {
	private Hashtable<String, Todolist> todolists;
	
	public TodolistManager(){
		todolists = new Hashtable<String, Todolist>();
		todolists.put("default", new Todolist());
	}
	
	public void createTodolist(Map<String, Value> map) {
		String name = map.get("list").getStringValue();
		
		if (todolists.containsKey(name)) {
			System.out.println("A todolist with this name exists already.");
			return;
		}
		todolists.put(name, new Todolist());
		System.out.println("I have created the new todolist: "+ name);
	}
	
	
	public Todolist getTodolist(String name) {
		if (!name.equals("")) {
			return todolists.get(name);
		}
		return todolists.get("default");
	}

	public void removeTodolist(Map<String, Value> map) {
		String name = map.get("list").getStringValue();
		
		if (name.equals("default")) {
			System.out.println("The default list cannot be removed.");
			return;
		}
		
		Todolist removed = todolists.remove(name);
		if (removed != null) {
			System.out.println("I removed the list \"" + name + "\".");
		} else {
			System.out.println("I couldn't find a list with the name \"" + name + "\".");
		}
	}

	public void printTodolists() {
		System.out.println("Here you go: ");
		for (String name: todolists.keySet()) {
			System.out.println(" - " + name);
		}
	}
}
