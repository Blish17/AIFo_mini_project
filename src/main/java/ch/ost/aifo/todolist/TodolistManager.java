package ch.ost.aifo.todolist;

import java.util.Hashtable;
import java.util.Map;

import com.google.protobuf.Value;

public class TodolistManager {
	private Hashtable<String, Todolist> todolists;
	
	public TodolistManager(){
		todolists = new Hashtable<String, Todolist>();
		createTodolist("default");
	}
	
	public void addTask(String name, Map<String, Value> map) {
		getTodolist(name).addTask(map);
	}
	
	public void removeTask(String name, Map<String, Value> map) {
		getTodolist(name).removeTask(map);
	}
	
	public void printTasks(String name) {
		getTodolist(name).printTasks();
	}
	
	public Todolist createTodolist(String name) {
		// TODO: handle name already exists
		return todolists.put(name, new Todolist());
	}
	
	
	public Todolist getTodolist(String name) {
		// TODO: handle name not found
		return todolists.get(name);
	}
}
