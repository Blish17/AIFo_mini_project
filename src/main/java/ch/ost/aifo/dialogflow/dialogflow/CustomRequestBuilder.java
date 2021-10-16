package ch.ost.aifo.dialogflow.dialogflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

import ch.ost.aifo.todolist.Todolist;
import ch.ost.aifo.todolist.TodolistManager;

interface ThrowingConsumer<T> extends Consumer<T> {

    @Override
    default void accept(final T elem) {
        try {
            acceptThrows(elem);
        } catch (Exception ex) {
        	System.out.println(ex.getMessage());
        }
    }

    void acceptThrows(T elem) throws Exception;

}

public class CustomRequestBuilder {
	private String projectId;
	private String sessionId;
	private TextInput.Builder textInput;
	private Map<String, ThrowingConsumer<Map<String, Value>>> intentsWithPayload;
	private Map<String, Runnable> intentsWithoutPayload;
	private TodolistManager todolistManager;
	
	public CustomRequestBuilder(String projectId, String sessionId, String languageCode) {
		this.projectId = projectId;
		this.sessionId = sessionId;
		this.textInput = TextInput.newBuilder().setLanguageCode(languageCode);
		this.todolistManager = new TodolistManager();
		this.intentsWithPayload = new HashMap<>();
		this.intentsWithoutPayload = new HashMap<>();
		fillIntentsWithPayload();
		fillIntentsWithoutPayload();
	}
	
	private void fillIntentsWithPayload() {
		intentsWithPayload.put("list.add", (map)-> todolistManager.createTodolist(map));
		intentsWithPayload.put("list.remove", (map)-> todolistManager.removeTodolist(map));
		intentsWithPayload.put("task.add", (map)-> getTodolist(map).addTask(map));
		intentsWithPayload.put("task.remove", (map)-> getTodolist(map).removeTask(map));
		intentsWithPayload.put("tasks.overview", (map)-> getTodolist(map).printTasks());
	}
	
	private void fillIntentsWithoutPayload() {
		intentsWithoutPayload.put("lists.overview", ()-> todolistManager.printTodolists());
	}
	
	private Todolist getTodolist(Map<String, Value> fieldsMap) throws Exception {
		String listName = fieldsMap.get("list").getStringValue();
		Todolist requestedList = todolistManager.getTodolist(listName);
		if (requestedList == null) {
			throw new Exception("I couldn't find a todolist with the name \"" + listName + "\".");
		}
		return requestedList;
	}


	public void detectIntentTexts(String text) throws IOException, ApiException {
		// Instantiates a client
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			// Set the session name using the sessionId (UUID) and projectID (my-project-id)
			SessionName session = SessionName.of(projectId, sessionId);
			
			// Detect intents for each text input
			textInput.setText(text);

			// Build the query with the TextInput
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

			// Performs the detect intent request
			DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

			QueryResult queryResult = response.getQueryResult();
			
			
			// own code
			String intent = queryResult.getIntent().getDisplayName();
			
			
			if (intentsWithoutPayload.containsKey(intent)) {
				intentsWithoutPayload.get(intent).run();
				return;
			}
			
			if (intentsWithPayload.containsKey(intent) && queryResult.getAllRequiredParamsPresent()) {
				Struct payload = queryResult.getFulfillmentMessagesOrBuilder(1).getPayload();
				Map<String, Value> fieldsMap = payload.getFieldsMap();
				
				intentsWithPayload.get(intent).accept(fieldsMap);
				return;
			}
			
			System.out.println(queryResult.getFulfillmentText());
		}
	}
}
