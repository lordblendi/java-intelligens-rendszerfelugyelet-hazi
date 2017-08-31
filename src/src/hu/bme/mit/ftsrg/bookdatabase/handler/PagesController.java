package hu.bme.mit.ftsrg.bookdatabase.handler;

import hu.bme.mit.ftsrg.bookdatabase.model.BookDatabaseModel;

public class PagesController extends Controller {
	public PagesController(BookDatabaseModel model) {
		super(model);
	}

	@Override
	public HttpResponse dispatch(String method, String action, String data) {
		if (!method.equals("GET")) {
			return null;
		}
		String content;
		try {
			synchronized (getModel()) {
				// get content for requested page
				content = getModel().pages().get(action);

				if (content == null) {
					// page was not found
					return new HttpResponse(HttpStatusCodes.NOT_FOUND);
				}
			}
		} catch (Exception e) {
			content = e.getClass().getSimpleName() + ": " + e.getMessage();
		}

		return new HttpResponse(HttpStatusCodes.OK, "text/plain", content);
	}
}
