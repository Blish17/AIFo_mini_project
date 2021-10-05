package ch.ost.aifo.dialogflow.dialogflow;

import java.io.IOException;
import java.util.Map;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.dialogflow.v2.DetectIntentResponse;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.TextInput;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;

public class CustomRequestBuilder {

	// same as template
	public static void detectIntentTexts(String projectId, String text, String sessionId, String languageCode) throws IOException, ApiException {
		// Instantiates a client
		try (SessionsClient sessionsClient = SessionsClient.create()) {
			// Set the session name using the sessionId (UUID) and projectID (my-project-id)
			SessionName session = SessionName.of(projectId, sessionId);
			System.out.println("Session Path: " + session.toString());

			// Detect intents for each text input

			// Set the text (hello) and language code (en-US) for the query
			TextInput.Builder textInput = TextInput.newBuilder().setText(text).setLanguageCode(languageCode);

			// Build the query with the TextInput
			QueryInput queryInput = QueryInput.newBuilder().setText(textInput).build();

			// Performs the detect intent request
			DetectIntentResponse response = sessionsClient.detectIntent(session, queryInput);

			QueryResult queryResult = response.getQueryResult();
			
			
			// own code
			// Display the query result
			
			String intent = queryResult.getIntent().getDisplayName();
			
			System.out.println("getAllRequiredParamsPresent: " + queryResult.getAllRequiredParamsPresent());
			if (intent.equals("order.drink") && queryResult.getAllRequiredParamsPresent()) {
				print_ordered_drink(queryResult);
			}

			// get the intent as a String
			System.out.println("intent: " + intent);

			// print the text answer
			System.out.println("answer: " + queryResult.getFulfillmentText());
			
		}
	}
	
	private static void print_ordered_drink(QueryResult queryResult) {
		System.out.println(queryResult.getFulfillmentMessagesOrBuilder(1).getPayload().getFieldsCount());
		Struct payload = queryResult.getFulfillmentMessagesOrBuilder(1).getPayload();
		System.out.println(payload);
		Map<String, Value> fieldsMap = payload.getFieldsMap();
		System.out.println("fieldsMap: " + fieldsMap);
		System.out.println(fieldsMap.get("drink").getStringValue());
		System.out.println("\n\n\n\n\n\n\n\n\n");
	}
}
