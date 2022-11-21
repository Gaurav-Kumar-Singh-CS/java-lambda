package handler;

import java.util.List;
import java.util.stream.Collectors;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.gson.JsonObject;

import models.Order;

public class DynamoDBHandler implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

	@Override
	public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent input, Context context) {
		LambdaLogger logger = context.getLogger();
		String path = input.getPath().substring(input.getPath().lastIndexOf("/") + 1, input.getPath().length());
		// Map<String, String> pathParameters = input.getPathParameters();

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();

		if (path.equals("orders") && input.getHttpMethod().equals("POST")) {
			response = createOrder(input);
			logger.log("path is " + path);
			return response;
		}
		if (path.equals("orders") && input.getHttpMethod().equals("GET")) {
			response = readOrder(input);
			logger.log("path is " + path);
			return response;
		}
		if (path.equals("users") && input.getHttpMethod().equals("GET")) {
			response = helloFunction(input);
			logger.log("path is " + path);
			return response;
		}

		return response.withStatusCode(400).withBody("error in handler");
	}

	public APIGatewayProxyResponseEvent helloFunction(APIGatewayProxyRequestEvent input) {
		String userId = input.getPathParameters().get("userId");

		JsonObject returnValue = new JsonObject();
		returnValue.addProperty("firstName", "Xander");
		returnValue.addProperty("lastName", "Jaxon");
		returnValue.addProperty("id", userId);

		APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
		response.withStatusCode(200).withBody(returnValue.toString());

		return response;
	}

	public APIGatewayProxyResponseEvent createOrder(APIGatewayProxyRequestEvent input) {
		ObjectMapper objectMapper = new ObjectMapper();
		ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
		DynamoDB dynamoDB = new DynamoDB(AmazonDynamoDBClientBuilder.defaultClient());
		Table table = dynamoDB.getTable("ordersTable");
		Order order;
		try {
			order = objectMapper.readValue(input.getBody(), Order.class);
			Item item = new Item().withPrimaryKey("id", order.getId()).withString("itemName", order.getItemName())
					.withInt("quantity", order.getQuantity());
			table.putItem(item);
			return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(writer.writeValueAsString(order));
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error in createOrder");
	}

	public APIGatewayProxyResponseEvent readOrder(APIGatewayProxyRequestEvent input) {
		ObjectWriter writer = new ObjectMapper().writer().withDefaultPrettyPrinter();
		AmazonDynamoDB dynamoDB = AmazonDynamoDBClientBuilder.defaultClient();
		ScanResult scanResult = dynamoDB.scan(new ScanRequest().withTableName("ordersTable"));
		List<Order> orders = scanResult
				.getItems().stream().map(item -> new Order(Integer.parseInt(item.get("id").getN()),
						item.get("itemName").getS(), Integer.parseInt(item.get("quantity").getN())))
				.collect(Collectors.toList());

		try {
			String jsonOutput = writer.writeValueAsString(orders);
			return new APIGatewayProxyResponseEvent().withStatusCode(200).withBody(jsonOutput);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return new APIGatewayProxyResponseEvent().withStatusCode(400).withBody("Error in createOrder");
	}
}
