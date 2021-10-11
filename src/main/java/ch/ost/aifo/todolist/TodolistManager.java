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
}
