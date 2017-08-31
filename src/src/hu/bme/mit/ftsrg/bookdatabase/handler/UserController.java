package hu.bme.mit.ftsrg.bookdatabase.handler;

import hu.bme.mit.ftsrg.bookdatabase.BookDatabaseApplication;
import hu.bme.mit.ftsrg.bookdatabase.model.Book;
import hu.bme.mit.ftsrg.bookdatabase.model.BookDatabaseModel;
import hu.bme.mit.ftsrg.bookdatabase.model.User;

import java.util.UUID;

import org.json.JSONObject;

public final class UserController extends Controller {
	public UserController(BookDatabaseModel model) {
		super(model);
	}

	@Override
	public HttpResponse dispatch(String method, String action, String data) {
		switch (action) {
		case "login":
			return doLogin(method, data);

		case "logout":
			return doLogout(method, data);

		default:
			return null;
		}
	}

	private HttpResponse doLogin(String method, String data) {
		if (!method.equals("POST")) {
			return null;
		}

		JSONObject jsonResponse = new JSONObject();
		try {
			// parse request
			User user = User.fromJSON(new JSONObject(data));
			
			synchronized (getModel()) {
				// get user by username
				User modelUser = getModel().users().get(user.getUsername());

				if (modelUser != null && modelUser.validatePassword(user)) {
					// password ok
					UUID sessionID = UUID.randomUUID();
					getModel().sessions().put(sessionID, modelUser);
                    BookDatabaseApplication.logger.info("Login OK.");
					jsonResponse.put("status", "ok");
					jsonResponse.put("sessionID", sessionID.toString());
				} else {
					// password invalid
                    BookDatabaseApplication.logger.info("Invalid credentials.");
					jsonResponse.put("status", "error");
					jsonResponse.put("message", "Invalid credentials");
				}
			}
		} catch (Exception e) {
            BookDatabaseApplication.logger.warn("Login was unsuccesful.");
			jsonResponse.put("status", "error");
			jsonResponse.put("message",
					e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		return new HttpResponse(HttpStatusCodes.OK, "application/json",
				jsonResponse.toString(2));
	}

	private HttpResponse doLogout(String method, String data) {
		if (!method.equals("POST")) {
			return null;
		}

		JSONObject jsonResponse = new JSONObject();
		try {
			// parse request
			JSONObject jsonRequest = new JSONObject(data);
			UUID sessionID = UUID
					.fromString(jsonRequest.getString("sessionID"));

			synchronized (getModel()) {
				// get user by sessionID
				User user = getModel().sessions().get(sessionID);

				if (user != null) {
					// logged in
					getModel().sessions().remove(sessionID);
                    BookDatabaseApplication.logger.info("Logout OK.");
					jsonResponse.put("status", "ok");
				} else {
					// not logged in
                    BookDatabaseApplication.logger.info("User is not logged in.");
					jsonResponse.put("status", "error");
					jsonResponse.put("message", "User was not logged in");
				}
			}
		} catch (Exception e) {
            BookDatabaseApplication.logger.warn("Logout was unsuccesful.");
			jsonResponse.put("status", "error");
			jsonResponse.put("message",
					e.getClass().getSimpleName() + ": " + e.getMessage());
		}

		return new HttpResponse(HttpStatusCodes.OK, "application/json",
				jsonResponse.toString(2));
	}
}
