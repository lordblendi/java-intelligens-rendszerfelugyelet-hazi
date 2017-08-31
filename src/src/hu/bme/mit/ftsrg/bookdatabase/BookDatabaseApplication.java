package hu.bme.mit.ftsrg.bookdatabase;

import hu.bme.mit.ftsrg.bookdatabase.handler.RequestHandler;
import hu.bme.mit.ftsrg.bookdatabase.model.Book;
import hu.bme.mit.ftsrg.bookdatabase.model.BookDatabaseModel;
import hu.bme.mit.ftsrg.bookdatabase.model.User;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.log4j.*;

/**
 * Hozzáadtam egy statikus változót, a loggert. Annak az elérésével bármelyik osztályból tudok logolni.
 * Ennek a beállításait a konstruktorban végeztem el a logger.properties fájl segítségével.
 */
public final class BookDatabaseApplication {
	private final int port;
	private final File configXML;
	private final PrintStream out;
	private final PrintStream err;
	private BookDatabaseModel model;

    public static Logger logger = Logger.getLogger(BookDatabaseApplication.class);

	public BookDatabaseApplication(int port, File configXML, PrintStream out,
			PrintStream err) {

        PropertyConfigurator.configure("logger.properties");
		this.port = port;
		this.configXML = configXML;
		this.out = out;
		this.err = err;
	}

	private void run() {
		out.println("===== BookDatabase Server =====");
		out.println("Run requested");
		out.println("  Desired port: " + port);
		out.println("  Configuration file: " + configXML);

		try {
			out.println("== Parsing configuration file ==");
			parseConfig();

			out.println("== Starting server ==");
			startServer();
		} catch (BookDatabaseException e) {
            logger.error("Starting server was unsuccesful!");
			err.println("Exception: " + e.getMessage());

			if (e.getCause() != null) {
				e.getCause().printStackTrace(err);
			}
		}
	}

	private void parseConfig() throws BookDatabaseException {
		try {
			model = new BookDatabaseModel();
            logger.debug("Parsing config!");

			// read and normalise XML
			DocumentBuilder builder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			Document doc = builder.parse(configXML);
			doc.getDocumentElement().normalize();

			// validate root element
			Element rootElement = doc.getDocumentElement();
			if ("config".equals(rootElement.getNodeName())) {
				parseUsers(rootElement);
				parseBooks(rootElement);
				parsePages(rootElement);
			} else {
                logger.error("Parsing the configuration was unsuccesful!");
				throw new BookDatabaseException(
						"Configuration file: the root element should be <config>");
			}

            logger.info("Configuration parsing was succesful!");
		} catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Parsing the configuration was unsuccesful!");
			throw new BookDatabaseException(
					"Unexpected exception occured when parsing"
							+ " the configuration file (see details)", e);
		}
	}

	private void parseUsers(Element rootElement) throws BookDatabaseException {
		NodeList elements = rootElement.getElementsByTagName("user");
        logger.debug("Parsing users!");
		if (elements.getLength() == 0) {
            logger.error("User parsing was unsuccesful!");
			throw new BookDatabaseException(
					"Configuration file: at least one user is required");
		}

		// get data for each element
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);

			Element usernameElement = (Element) element.getElementsByTagName(
					"username").item(0);
			Element passwordElement = (Element) element.getElementsByTagName(
					"password").item(0);

			// no missing data
			if (usernameElement == null || passwordElement == null) {
                logger.error("User parsing was unsuccesful!");
				throw new BookDatabaseException(
						"Configuration file: the username and password "
								+ "must be specified for each user");
			}

			// create User object
			User user = new User(usernameElement.getTextContent().trim(),
					passwordElement.getTextContent().trim());

			// save to the model
			model.users().put(user.getUsername(), user);

		}
        logger.debug("User parsing was succesful!");
	}

	private void parseBooks(Element rootElement) throws BookDatabaseException {
		NodeList elements = rootElement.getElementsByTagName("book");
        logger.debug("Parsing books!");
		if (elements.getLength() == 0) {
            logger.debug("No books to parse!");
			return;
		}

		// get data for each element
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);

			Element authorElement = (Element) element.getElementsByTagName(
					"author").item(0);
			Element titleElement = (Element) element.getElementsByTagName(
					"title").item(0);
			Element yearElement = (Element) element
					.getElementsByTagName("year").item(0);
			Element isbnElement = (Element) element
					.getElementsByTagName("isbn").item(0);

			// no missing data
			if (authorElement == null || titleElement == null
					|| yearElement == null || isbnElement == null) {
                logger.error("Book parsing was unsuccesful!");
				throw new BookDatabaseException(
						"Configuration file: the author, title, year and ISBN "
								+ "must be specified for each book");
			}

			try {
				// create Book object
				Book book = new Book(authorElement.getTextContent().trim(),
						titleElement.getTextContent().trim(),
						Integer.parseInt(yearElement.getTextContent().trim()),
						isbnElement.getTextContent().trim());

				// save to the model
				model.books().put(book.getISBN(), book);
			} catch (Exception e) {
                logger.error("Book parsing was unsuccesful!");
				throw new BookDatabaseException(
						"Configuration file: invalid data (see details)", e);

			}

		}
        logger.debug("Book parsing was succesful!");
	}

	private void parsePages(Element rootElement) throws BookDatabaseException {
		NodeList elements = rootElement.getElementsByTagName("page");
        logger.debug("Parsing Pages!");
		if (elements.getLength() == 0) {
            logger.debug("No pages to parse!");
			return;
		}

		// get data for each element
		for (int i = 0; i < elements.getLength(); i++) {
			Element element = (Element) elements.item(i);

			String name = element.getAttribute("name");
			String content = element.getTextContent().trim();

			// save to the model
			model.pages().put(name, content);
		}

        logger.debug("Page parsing was succesful!");
	}

	private void startServer() throws BookDatabaseException {
		// open socket
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			out.println("Socket created!");
            logger.info("Socket created! Waiting for connections!");
			ExecutorService threadPool = Executors.newCachedThreadPool();

			while (true) {
				// accept connections from the clients
				try {
					Socket socket = serverSocket.accept();
					out.println("Client connected: " + socket.getInetAddress()
							+ ":" + socket.getPort());
                     logger.info("Client connected!");
					// handle request
					threadPool.execute(new RequestHandler(socket, model, out,
							err));
				} catch (IOException e) {
                    logger.error("Client connection was unsuccesful!");
					throw new BookDatabaseException(
							"Unexpected exception occured when creating"
									+ " a socket (see details)", e);
				}
			}
		} catch (IOException e) {
            logger.error("Starting server was unsuccesful!");
			throw new BookDatabaseException(
					"Unexpected exception occured when starting"
							+ " the server socket (see details)", e);
		}
	}

	public static void main(String[] args) {
		// parse command line arguments
		int port = -1;
		File configXML = null;
		List<String> errors = new ArrayList<>();

		if (args.length == 2) {
			// port
			try {
				port = Integer.parseInt(args[0]);

				if (port <= 0 || port >= 65536) {
					errors.add("Invalid port: " + port);
				}
			} catch (NumberFormatException e) {
				errors.add("Not a number: " + port);
			}

			// configuration file
			configXML = new File(args[1]);

			if (!configXML.exists()) {
				errors.add("The configuration file does not exists");
			} else if (!configXML.canRead()) {
				errors.add("The configuration file cannot be read");
			}
		} else {
			errors.add("Missing parameters");
		}

		if (errors.size() > 0) {
			// print errors
			System.err.println("An error occured");

			for (String error : errors) {
				System.err.println("  " + error);
			}

			System.err
					.println("Usage: java -jar BookDatabase.jar <port> <configxml>");
		} else {
			// start application
			try {
				new BookDatabaseApplication(port, configXML, System.out,
						System.err).run();
			} catch (Exception e) {
				System.err.println("Uncaught exception");
				System.err.println("  Class: " + e.getClass());
				System.err.println("  Message: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
