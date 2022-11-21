package handler;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import models.Checkout;

public class S3Handler {

	AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
	ObjectMapper objectMapper = new ObjectMapper();
	AmazonSNS sns = AmazonSNSClientBuilder.defaultClient();

	public void s3Handler(S3Event event) {
		event.getRecords().forEach(record -> {
			S3ObjectInputStream s3inputStream = s3
					.getObject(record.getS3().getBucket().getName(), record.getS3().getObject().getKey())
					.getObjectContent();
			try {
				List<Checkout> checkoutEvents = Arrays.asList(objectMapper.readValue(s3inputStream, Checkout[].class));
				System.out.println("Contents of the bucket are : " + checkoutEvents);
				checkoutEvents.forEach(checkoutEvent -> {
					try {
						sns.publish("arn:aws:sns:us-west-2:736880270801:lambda-topic", objectMapper.writeValueAsString(checkoutEvent));
					} catch (JsonProcessingException e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

	}

}
