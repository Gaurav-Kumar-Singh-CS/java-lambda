package handler;

import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Checkout;

public class SNSHandler {

	ObjectMapper objectMapper = new ObjectMapper();

	public void snsHandler(SNSEvent event) {
		event.getRecords().forEach(snsRecord -> {
			try {
				Checkout checkOutEvent = objectMapper.readValue(snsRecord.getSNS().getMessage(), Checkout.class);
				System.out.println("Message from SNS Lambda " + checkOutEvent);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
			}
		});
	}
}
