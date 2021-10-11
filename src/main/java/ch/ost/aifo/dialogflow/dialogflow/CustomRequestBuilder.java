package ch.ost.aifo.dialogflow.dialogflow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
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

public class CustomRequestBuilder {
	private String projectId;
	private String sessionId;
	private TextInput.Builder textInput;
	// TODO: rename processManageIntent, processIntent, answerIntent
	private Map<String, Consumer<Map<String, Value>>> processManageIntent;
	private Map<String, BiConsumer<Todolist, Map<String, Value>>> processIntent;
	private Map<String, Consumer<Todolist>> answerIntent;
	private TodolistManager todolistManager;
	
	public CustomRequestBuilder(String projectId, String sessionId, String languageCode) {
		this.projectId = projectId;
		this.sessionId = sessionId;
		this.textInput = TextInput.newBuilder().setLanguageCode(languageCode);
		this.todolistManager = new TodolistManager();
		this.processManageIntent = new HashMap<>();
		this.processIntent = new HashMap<>();
		this.answerIntent = new HashMap<>();
		fillProcessManageIntent();
		fillProcessIntent();
		fillAnswerIntent();
	}
	
	private void fillProcessManageIntent() {
		processManageIntent.put("list.add", (map)-> todolistManager.createTodolist(map));
	}

	private void fillProcessIntent() {
		processIntent.put("task.add", (todolist, map)-> todolist.addTask(map));
		processIntent.put("task.remove", (todolist, map)-> todolist.removeTask(map));
	}
	
	private void fillAnswerIntent() {
		answerIntent.put("tasks.overview", (todolist)-> todolist.printTasks());
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
			
			if (queryResult.getFulfillmentMessagesCount() == 1) {
				System.out.println("default answer: " + queryResult.getFulfillmentText());
				return;
			}
			
			Struct payload = queryResult.getFulfillmentMessagesOrBuilder(1).getPayload();
			Map<String, Value> fieldsMap = payload.getFieldsMap();
			
			// TODO: refactor processManageIntent.containsKey(intent) && queryResult.getAllRequiredParamsPresent
			if (processManageIntent.containsKey(intent) && queryResult.getAllRequiredParamsPresent()) {
				processManageIntent.get(intent).accept(fieldsMap);
				return;
			}
			
			String listName = fieldsMap.get("list").getStringValue();
			Todolist requestedList = todolistManager.getTodolist(listName);
			
			if (requestedList == null) {
				System.out.println("I couldn't find a todolist with the name \"" + listName + "\".");
				return;
			}
			
			if(answerIntent.containsKey(intent) && queryResult.getAllRequiredParamsPresent()) {
				answerIntent.get(intent).accept(requestedList);
				return;
			}
			
			if (processIntent.containsKey(intent) && queryResult.getAllRequiredParamsPresent()) {
				processIntent.get(intent).accept(requestedList, fieldsMap);
				return;
			}
		}
	}
}
