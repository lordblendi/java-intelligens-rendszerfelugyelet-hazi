package hu.bme.mit.ftsrg.bookdatabase;

public class BookDatabaseException extends Exception {
	private static final long serialVersionUID = 5988545628426077016L;

	public BookDatabaseException() {
		super();
	}

	public BookDatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BookDatabaseException(String message) {
		super(message);
	}

	public BookDatabaseException(Throwable cause) {
		super(cause);
	}
}
