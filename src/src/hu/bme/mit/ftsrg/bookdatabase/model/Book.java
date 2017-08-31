package hu.bme.mit.ftsrg.bookdatabase.model;

import hu.bme.mit.ftsrg.bookdatabase.BookDatabaseApplication;
import org.json.JSONObject;

public final class Book implements Comparable<Book> {
	private final String author;
	private final String title;
	private final int year;
	private final ISBN isbn;

	public Book(String author, String title, int year, ISBN isbn) {
		this.author = author;
		this.title = title;
		this.year = year;
		this.isbn = isbn;
	}

	public Book(String author, String title, int year, String isbn) {
		super();
		this.author = author;
		this.title = title;
		this.year = year;
		this.isbn = ISBN.fromString(isbn);
	}

	public String getAuthor() {
		return author;
	}

	public String getTitle() {
		return title;
	}

	public int getYear() {
		return year;
	}

	public ISBN getISBN() {
		return isbn;
	}

	@Override
	public String toString() {
		return String.format("%s: %s (%d)  - ISBN: %s", author, title, year,
				isbn.getValue());
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("author", author);
		json.put("title", title);
		json.put("year", year);
		json.put("isbn", isbn.getValue());
		return json;
	}

	public static Book fromJSON(JSONObject json) {
		if (json == null) {
            BookDatabaseApplication.logger.warn("Unable to create new book!");
			throw new IllegalArgumentException("The JSON data is null");
		}

		String author = json.getString("author");
		String title = json.getString("title");
		int year = json.getInt("year");
		String isbn = json.getString("isbn");
        BookDatabaseApplication.logger.info("New book creation was succesful!");
		return new Book(author, title, year, isbn);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((author == null) ? 0 : author.hashCode());
		result = prime * result + ((isbn == null) ? 0 : isbn.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Book other = (Book) obj;
		if (author == null) {
			if (other.author != null)
				return false;
		} else if (!author.equals(other.author))
			return false;
		if (isbn == null) {
			if (other.isbn != null)
				return false;
		} else if (!isbn.equals(other.isbn))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	@Override
	public int compareTo(Book o) {
		if (o == null) {
			return -1;
		} else {
			return toString().compareTo(o.toString());
		}
	}
}
