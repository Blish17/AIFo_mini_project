package ch.ost.aifo.todolist;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import java.util.Map;

import com.google.protobuf.Value;

public class TodolistManager {
	private Hashtable<String, Todolist> todolists;
	
	public TodolistManager(){
		try {
			todolists = load();
		} catch (Exception ex) {
			System.out.println(ex);
			todolists = new Hashtable<String, Todolist>();
			todolists.put("default", new Todolist("default"));
		}
		registerOnClose();
	}
	
	public void createTodolist(Map<String, Value> map) {
		String name = map.get("list").getStringValue();
		
		if (todolists.containsKey(name)) {
			System.out.println("A todolist with this name exists already.");
			return;
		}
		todolists.put(name, new Todolist(name));
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
			todolists.put("default", new Todolist("default"));
			System.out.println("I removed everything from the default list.");
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
	
	@SuppressWarnings("unchecked")
	public Hashtable<String, Todolist> load() throws ClassNotFoundException, IOException {
		try (ObjectInputStream stream = new ObjectInputStream(new FileInputStream("saveFile.txt"))) {
			return todolists = (Hashtable<String, Todolist>) stream.readObject();
		}
	}

	public void save() {
		try (ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream("saveFile.txt"))) {
			stream.writeObject(todolists);
			System.out.println("I saved your changes");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public void registerOnClose() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
		    @Override
		    public void run()
		    {
		    	save();
		    	System.out.println("Goodbye");
		    }
		});
	}
}
