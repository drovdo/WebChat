package storage;

import java.io.File;
import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import model.Message;
import model.Request;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public final class XMLHistory {
	private static final String STORAGE_LOCATION = System.getProperty("user.home") +  File.separator + "history.xml";
	private static SimpleDateFormat timeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private XMLHistory() {
	}

	public static synchronized void createStorage() throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("messages");
		doc.appendChild(rootElement);

		Transformer transformer = getTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
		transformer.transform(source, result);
	}

	public static synchronized void addData(Message m) throws ParserConfigurationException, SAXException, IOException, TransformerException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement(); // Root <tasks> element

		Element message = document.createElement("message");
		root.appendChild(message);

		message.setAttribute("id", Integer.toString(m.getId()));

		Element user = document.createElement("author");
		user.appendChild(document.createTextNode(m.getUser()));
		message.appendChild(user);

		Element text = document.createElement("text");
		text.appendChild(document.createTextNode(m.getText()));
		message.appendChild(text);

		Element date = document.createElement("date");
		date.appendChild(document.createTextNode(timeFormat.format(m.getDate())));
		message.appendChild(date);

		DOMSource source = new DOMSource(document);

		Transformer transformer = getTransformer();

		StreamResult result = new StreamResult(STORAGE_LOCATION);
		transformer.transform(source, result);
	}

	public static synchronized void deleteData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Node messageToDelete = getNodeById(document, Integer.valueOf(message.getId()).toString());

		if (messageToDelete != null) {

			messageToDelete.getParentNode().removeChild(messageToDelete);

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	public static synchronized void editData(Message message) throws ParserConfigurationException, SAXException, IOException, TransformerException, XPathExpressionException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Node messageToUpdate = getNodeById(document, Integer.valueOf(message.getId()).toString());

		if (messageToUpdate != null) {

			NodeList childNodes = messageToUpdate.getChildNodes();

			for (int i = 0; i < childNodes.getLength(); i++) {

				Node node = childNodes.item(i);

				if ("text".equals(node.getNodeName())) {
					node.setTextContent(message.getText());
				}
			}

			Transformer transformer = getTransformer();

			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(STORAGE_LOCATION));
			transformer.transform(source, result);
		} else {
			throw new NullPointerException();
		}
	}

	private static Node getNodeById(Document doc, String id) throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr = xpath.compile("//" + "message" + "[@id='" + id + "']");
		return (Node) expr.evaluate(doc, XPathConstants.NODE);
	}

	public static synchronized boolean doesStorageExist() {
		File file = new File(STORAGE_LOCATION);
		return file.exists();
	}

	public static synchronized List<Request> getMessages() throws SAXException, IOException, ParserConfigurationException {
		List<Request> messages = new ArrayList<>();
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		NodeList messageList = root.getElementsByTagName("message");
		for (int i = 0; i < messageList.getLength(); i++) {
			Element message = (Element) messageList.item(i);
			int id = Integer.valueOf(message.getAttribute("id"));
			String user = message.getElementsByTagName("author").item(0).getTextContent();
			String text = message.getElementsByTagName("text").item(0).getTextContent();
			Date date = timeFormat.parse(message.getElementsByTagName("date").item(0).getTextContent(), new ParsePosition(0));
			messages.add(new Request(new Message(id, user, text, date), "POST"));
		}
		return messages;
	}

	public static synchronized Integer getLastId() throws SAXException, IOException, ParserConfigurationException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		Document document = documentBuilder.parse(STORAGE_LOCATION);
		document.getDocumentElement().normalize();
		Element root = document.getDocumentElement();
		NodeList messageList = root.getElementsByTagName("message");
		Integer lastId = -1;
		if (messageList.getLength() != 0) {
			Element message = (Element) messageList.item(messageList.getLength() - 1);
			lastId = Integer.valueOf(message.getAttribute("id"));
		}
		return lastId;
	}

	private static Transformer getTransformer() throws TransformerConfigurationException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		return transformer;
	}

}
