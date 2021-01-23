package com.app.kafka.producer;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.app.kafka.domain.LibraryEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
//@Slf4j
public class LibraryProducer {
	private Logger logger = LoggerFactory.getLogger(LibraryProducer.class);
	private static final String TOPIC_NAME = "library-events-1";

	@Autowired
	private KafkaTemplate<Integer, String> kafkaTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.sendDefault(key, value);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}
		});
	}

	public SendResult<Integer, String> sendLibraryEventSynchronus(LibraryEvent libraryEvent) throws Exception {
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		try {
			SendResult<Integer, String> sendResult = kafkaTemplate.sendDefault(key, value).get();
			return sendResult;
		} catch (InterruptedException | ExecutionException e) {
			logger.error("InterruptedExcepion/ExecutionException sending the Message and the exception is: {} ",
					e.getMessage());
			throw e;
		} catch (Exception ex) {
			logger.error("Exception sending the Message and the exception is: {} ", ex.getMessage());
			throw ex;
		}
	}

	public void sendLibraryEventApproach2(LibraryEvent libraryEvent) throws JsonProcessingException {
		Integer key = libraryEvent.getLibraryEventId();
		String value = objectMapper.writeValueAsString(libraryEvent);

		ProducerRecord<Integer, String> producerRecord = buildProducerRecord(key, value);
		ListenableFuture<SendResult<Integer, String>> listenableFuture = kafkaTemplate.send(producerRecord);
		listenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {

			@Override
			public void onSuccess(SendResult<Integer, String> result) {
				handleSuccess(key, value, result);
			}

			@Override
			public void onFailure(Throwable ex) {
				handleFailure(key, value, ex);
			}
		});
	}

	private ProducerRecord<Integer, String> buildProducerRecord(Integer key, String value) {
		List<Header> recordHeaders = List.of(new RecordHeader("event-source", "scanner".getBytes()));
		
		return new ProducerRecord<>(TOPIC_NAME, null, key, value, recordHeaders);
	}

	private void handleSuccess(Integer key, String value, SendResult<Integer, String> result) {
		logger.info("Message sent successfully for the key: {} and the value is: {}, " + "partition is: {}", key, value,
				result.getRecordMetadata().partition());
	}

	private void handleFailure(Integer key, String value, Throwable ex) {
		logger.error("Error sending the Message and the exception is: {}", ex.getMessage());

		try {
			throw ex;
		} catch (Throwable throwable) {
			logger.error("Error in OnFailure: {}", throwable.getMessage());
		}
	}
}
