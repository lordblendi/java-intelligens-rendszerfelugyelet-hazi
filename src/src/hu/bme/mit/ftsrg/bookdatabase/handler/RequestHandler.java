package hu.bme.mit.ftsrg.bookdatabase.handler;

import hu.bme.mit.ftsrg.bookdatabase.model.BookDatabaseModel;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler implements Runnable {
	private final Socket socket;
	private final BookDatabaseModel model;
	private final PrintStream serverOut;
	private final PrintStream serverErr;
	private final BufferedReader socketIn;
	private final PrintStream socketOut;

	public RequestHandler(Socket socket, BookDatabaseModel model,
			PrintStream serverOut, PrintStream serverErr) throws IOException {
		this.socket = socket;
		this.model = model;
		this.serverOut = serverOut;
		this.serverErr = serverErr;
		this.socketIn = new BufferedReader(new InputStreamReader(
				socket.getInputStream()));
		this.socketOut = new PrintStream(socket.getOutputStream(), true);
	}

	@Override
	public void run() {
		try {
			// get headers
			Map<String, String> headers = readHttpHeaders();

			if (headers == null) {
				return;
			}

			// parse request
			String[] requestParts = headers.get(null).split("\\s+", 3);
			String method = requestParts[0];
			String resource = requestParts[1];
			String protocol = requestParts[2];

			HttpResponse response = null;

			// check protocol
			if (protocol.equals("HTTP/1.1")) {
				if (resource.equals("/")) {
					resource = "/pages/welcome";
				}

				// dispatch query
				String[] resourceParts = resource.split("/");
				if (resourceParts.length == 3) {
					String controllerName = resourceParts[1];
					String action = resourceParts[2];

					// create controller
					Controller controller = createController(controllerName);

					if (controller != null) {
						// get request data
						String data = getRequestPayload(headers);

						// get response
						response = controller.dispatch(method, action, data);
					}
				}
			} else {
				// not HTTP/1.1
				response = new HttpResponse(
						HttpStatusCodes.HTTP_VERSION_NOT_SUPPORTED);
			}

			if (response == null) {
				// no response from controllers, the request is invalid
				response = new HttpResponse(HttpStatusCodes.BAD_REQUEST);
			}

			// send response
			socketOut.println(response.toString());
		} catch (IOException e) {
			// exception when accessing the socket
			serverErr
					.println("Unexpected exception occured when accessing the client: "
							+ socket.getInetAddress() + ":" + socket.getPort());
			e.printStackTrace(serverErr);
		} finally {
			// close socket
			try {
				socket.close();
				serverOut.println("Client disconnected: "
						+ socket.getInetAddress() + ":" + socket.getPort());
			} catch (Exception e) {
				serverErr
						.println("Unexpected exception occured when closing the socket: "
								+ socket.getInetAddress()
								+ ":"
								+ socket.getPort());
				e.printStackTrace(serverErr);
			}
		}
	}

	private Map<String, String> readHttpHeaders() throws IOException,
			EOFException {
		Map<String, String> headers = new HashMap<>();
		while (true) {
			String line = socketIn.readLine();
			if (line == null) {
				// no good headers
				return null;
			} else if (line.trim().isEmpty()) {
				// headers end
				break;
			}

			String[] parts = line.split(":", 2);
			if (parts.length == 1) {
				headers.put(null, parts[0].trim());
			} else {
				headers.put(parts[0].trim(), parts[1].trim());
			}
		}
		return headers;
	}

	private Controller createController(String controllerName) {
		Controller controller;
		switch (controllerName) {
		case "pages":
			controller = new PagesController(model);
			break;

		case "books":
			controller = new BooksController(model);
			break;

		case "user":
			controller = new UserController(model);
			break;

		default:
			controller = null;
			break;
		}
		return controller;
	}

	private String getRequestPayload(Map<String, String> headers)
			throws IOException {
		// get request payload length
		int contentLength;
		if (headers.get("Content-Length") != null) {
			contentLength = Integer.parseInt(headers.get("Content-Length"));
		} else {
			contentLength = 0;
		}
	
		String payload = null;
		if (contentLength > 0) {
			// only get request payload if it is not empty
			payload = "";
			char[] buf = new char[1024];
			while (payload.getBytes().length < contentLength) {
				int count = socketIn.read(buf);
	
				// we are reading chars, but length is in
				// bytes!!!
				payload = payload.concat(new String(buf, 0, count));
			}
		}
		return payload;
	}
}
