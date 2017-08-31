package hu.bme.mit.ftsrg.bookdatabase.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import hu.bme.mit.ftsrg.bookdatabase.BookDatabaseApplication;
import org.json.JSONArray;
import org.json.JSONObject;

import hu.bme.mit.ftsrg.bookdatabase.model.Book;
import hu.bme.mit.ftsrg.bookdatabase.model.BookDatabaseModel;
import hu.bme.mit.ftsrg.bookdatabase.model.ISBN;
import hu.bme.mit.ftsrg.bookdatabase.model.User;

public final class BooksController extends Controller {
	public BooksController(BookDatabaseModel model) {
		super(model);
	}

	@Override
	public HttpResponse dispatch(String method, String action, String data) {
		switch (action) {
		case "list":
			return doList(method, data);

		case "create":
			return doCreate(method, data);

		case "delete":
			return doDelete(method, data);

		default:
			return null;
		}
	}

	private HttpResponse doList(String method, String data) {
		if (!method.equals("GET")) {
			return null;
		}

		JSONArray jsonResponse = new JSONArray();

		synchronized (getModel()) {
			// get the sorted list of books
			List<Book> books = new ArrayList<>(getModel().books().values());
			Collections.sort(books);

			// add to response
			for (Book book : books) {
				jsonResponse.put(book.toJSON());
			}
		}

		return new HttpResponse(HttpStatusCodes.OK, "application/json",
				jsonResponse.toString(2));
	}

	private HttpResponse doCreate(String method, String data) {
		if (!method.equals("PUT")) {
			return null;
		}

		JSONObject jsonResponse = new JSONObject();
		try {
			// parse request
			JSONObject jsonRequest = new JSONObject(data);
			UUID sessionID = UUID
					.fromString(jsonRequest.getString("sessionID"));
			Book book = Book.fromJSON(jsonRequest.getJSONObject("book"));

			synchronized (getModel()) {
				// get user by sessionID
				User user = getModel().sessions().get(sessionID);

				if (user != null) {
					// valid sessionID // save book
					getModel().books().put(book.getISBN(), book);
					jsonResponse.put("status", "ok");
				} else {
					// invalid sessionID
                    BookDatabaseApplication.logger.info("User is not logged in.");
					jsonResponse.put("status", "error");
					jsonResponse.put("message", "User is not logged in");
				}
			}
		} catch (Exception e) {
            BookDatabaseApplication.logger.warn("Error while adding book.");
			jsonResponse.put("status", "error");
			jsonResponse.put("message",
					e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		return new HttpResponse(HttpStatusCodes.OK, "application/json",
				jsonResponse.toString(2));
	}

	private HttpResponse doDelete(String method, String data) {
		if (!method.equals("DELETE")) {
			return null;
		}

		JSONObject jsonResponse = new JSONObject();
		try {
			// parse request
			JSONObject jsonRequest = new JSONObject(data);
			UUID sessionID = UUID
					.fromString(jsonRequest.getString("sessionID"));
			ISBN isbn = ISBN.fromString(jsonRequest.getString("isbn"));

			synchronized (getModel()) {
				// get user by sessionID
				User user = getModel().sessions().get(sessionID);

				if (user != null) {
					// valid sessionID

					if (getModel().books().containsKey(isbn)) {
						// book was found
                        BookDatabaseApplication.logger.info("Book deleted.");
						getModel().books().remove(isbn);
						jsonResponse.put("status", "ok");
					} else {
						// book was not found
                        BookDatabaseApplication.logger.info("Book not found.");
						jsonResponse.put("status", "error");
						jsonResponse.put("message", "The specified ISBN "
								+ "was not found in the database");
					}
				} else {
					// invalid sessionID
                    BookDatabaseApplication.logger.info("User is not logged in.");
					jsonResponse.put("status", "error");
					jsonResponse.put("message", "User is not logged in");
				}
			}
		} catch (Exception e) {
            BookDatabaseApplication.logger.warn("Error while deleting book.");
			jsonResponse.put("status", "error");
			jsonResponse.put("message",
					e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		return new HttpResponse(HttpStatusCodes.OK, "application/json",
				jsonResponse.toString(2));
	}
}
