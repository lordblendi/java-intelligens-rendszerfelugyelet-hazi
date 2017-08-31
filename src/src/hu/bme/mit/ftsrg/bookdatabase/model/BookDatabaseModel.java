package hu.bme.mit.ftsrg.bookdatabase.model;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class BookDatabaseModel {
	private final Map<String, User> users = new HashMap<>();
	private final Map<ISBN, Book> books = new HashMap<>();
	private final Map<String, String> pages = new HashMap<>();
	private final Map<UUID, User> sessions = new HashMap<>();

	public Map<String, User> users() {
		return users;
	}

	public Map<ISBN, Book> books() {
		return books;
	}

	public Map<String, String> pages() {
		return pages;
	}

	public Map<UUID, User> sessions() {
		return sessions;
	}
}
