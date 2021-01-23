package com.app.kafka.domain;

//@NoArgsConstructor
//@AllArgsConstructor
//@Data
//@Builder
public class LibraryEvent {
	private Integer libraryEventId;
	private Book book;
	private LibraryEventType libraryEventType;

	public Integer getLibraryEventId() {
		return libraryEventId;
	}

	public void setLibraryEventId(Integer libraryEventId) {
		this.libraryEventId = libraryEventId;
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public LibraryEventType getLibraryEventType() {
		return libraryEventType;
	}

	public void setLibraryEventType(LibraryEventType libraryEventType) {
		this.libraryEventType = libraryEventType;
	}
}
