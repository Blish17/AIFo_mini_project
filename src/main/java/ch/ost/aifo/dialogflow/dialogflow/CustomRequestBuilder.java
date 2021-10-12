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
	// TODO: rename answerIntent, processIntent
	private Map<String, ThrowingConsumer<Map<String, Value>>> processIntent;
	private Map<String, Runnable> answerIntent;
	private TodolistManager todolistManager;
	
	public CustomRequestBuilder(String projectId, String sessionId, String languageCode) {
		this.projectId = projectId;
		this.sessionId = sessionId;
		this.textInput = TextInput.newBuilder().setLanguageCode(languageCode);
		this.todolistManager = new TodolistManager();
		this.processIntent = new HashMap<>();
		this.answerIntent = new HashMap<>();
		fillProcessIntent();
		fillAnswerIntent();
	}
	
	private void fillProcessIntent() {
		processIntent.put("list.add", (map)-> todolistManager.createTodolist(map));
		processIntent.put("list.remove", (map)-> todolistManager.removeTodolist(map));
		processIntent.put("task.add", (map)-> getTodolist(map).addTask(map));
		processIntent.put("task.remove", (map)-> getTodolist(map).removeTask(map));
		processIntent.put("tasks.overview", (map)-> getTodolist(map).printTasks());
	}
	
	private void fillAnswerIntent() {
		answerIntent.put("lists.overview", ()-> todolistManager.printTodolists());
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
			
			
			if (answerIntent.containsKey(intent)) {
				answerIntent.get(intent).run();
				return;
			}
			
			if (processIntent.containsKey(intent) && queryResult.getAllRequiredParamsPresent()) {
				Struct payload = queryResult.getFulfillmentMessagesOrBuilder(1).getPayload();
				Map<String, Value> fieldsMap = payload.getFieldsMap();
				
				processIntent.get(intent).accept(fieldsMap);
				return;
			}
			
			if (queryResult.getFulfillmentMessagesCount() == 1) {
				System.out.println("default answer: " + queryResult.getFulfillmentText());
			}
		}
	}
}
