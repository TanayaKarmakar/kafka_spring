package com.app.kafka.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.app.kafka.domain.LibraryEvent;
import com.app.kafka.domain.LibraryEventType;
import com.app.kafka.producer.LibraryProducer;

@RestController
public class LibraryEventsController {
	private Logger logger = LoggerFactory.getLogger(LibraryEventsController.class);
	
	@Autowired
	private LibraryProducer libraryProducer;

	@PostMapping("/v1/libraryevent")
	public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws Exception {
		libraryEvent.setLibraryEventType(LibraryEventType.NEW);
		libraryProducer.sendLibraryEventApproach2(libraryEvent);
		logger.info("after SendLibraryEvent");
		
		return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
	}
}
