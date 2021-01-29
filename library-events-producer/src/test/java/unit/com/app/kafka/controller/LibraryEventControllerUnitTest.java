package unit.com.app.kafka.controller;

import com.app.kafka.controller.LibraryEventsController;
import com.app.kafka.domain.Book;
import com.app.kafka.domain.LibraryEvent;
import com.app.kafka.producer.LibraryProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {
    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private LibraryProducer libraryProducer;

    @Test
    public void testPostLibraryEvent() throws Exception {
        LibraryEvent libraryEvent = buildLibraryEvent();

        String json = objectMapper.writeValueAsString(libraryEvent);

        doNothing().when(libraryProducer).sendLibraryEventApproach2(libraryEvent);

        mockMvc.perform(post("/v1/libraryevent")
        .content(json)
        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
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
