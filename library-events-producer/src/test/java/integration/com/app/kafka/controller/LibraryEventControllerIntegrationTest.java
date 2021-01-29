package integration.com.app.kafka.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import com.app.kafka.domain.Book;
import com.app.kafka.domain.LibraryEvent;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = { "library-events-1" }, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
		"spring.kafka.admin.properties.bootstrap.servers=${spring.embedded.kafka.brokers}"})
public class LibraryEventControllerIntegrationTest {
	@Autowired
	TestRestTemplate restTemplate;
	
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;
	
	private Consumer<Integer, String> consumer;
	
	@BeforeEach
	public void setUp() {
		Map<String, Object> configMap = new HashMap<>(KafkaTestUtils.consumerProps("group1", "true", embeddedKafkaBroker));
		consumer = new DefaultKafkaConsumerFactory<>(configMap, new IntegerDeserializer(), new StringDeserializer())
				.createConsumer();
		
		embeddedKafkaBroker.consumeFromAllEmbeddedTopics(consumer);
	}
	
	@AfterEach
	public void tearDown() {
		consumer.close();
	}

	@Test
	public void testPostLibraryEvent() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("content-type", MediaType.APPLICATION_JSON_VALUE);
		LibraryEvent libraryEvnt = buildLibraryEvent();
		HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvnt, headers);
		ResponseEntity<LibraryEvent> responseEntity = restTemplate.exchange("/v1/libraryevent", HttpMethod.POST,
				request, LibraryEvent.class);

		assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
		
		ConsumerRecord<Integer, String> consumerRecord = KafkaTestUtils.getSingleRecord(consumer, "library-events-1");
		String expectedRecord = "{\"libraryEventId\":null,\"book\":{\"bookId\":1234,\"bookName\":\"TestBook\","
				+ "\"bookAuthor\":\"TestBookAuthor\"},\"libraryEventType\":\"NEW\"}";
		String value = consumerRecord.value();
		
		assertEquals(expectedRecord, value);
	}

	private LibraryEvent buildLibraryEvent() {
		LibraryEvent libraryEvent = new LibraryEvent();
		libraryEvent.setBook(buildBook());
		return libraryEvent;
	}

	private Book buildBook() {
		Book book = new Book();
		book.setBookId(1234);
		book.setBookName("TestBook");
		book.setBookAuthor("TestBookAuthor");
		return book;
	}
}
