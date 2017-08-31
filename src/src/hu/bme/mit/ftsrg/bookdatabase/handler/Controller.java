package hu.bme.mit.ftsrg.bookdatabase.handler;

import hu.bme.mit.ftsrg.bookdatabase.model.BookDatabaseModel;

public abstract class Controller {
	private final BookDatabaseModel model;

	public Controller(BookDatabaseModel model) {
		this.model = model;
	}

	public abstract HttpResponse dispatch(String method, String action,
			String data);

	public final BookDatabaseModel getModel() {
		return model;
	}
}
