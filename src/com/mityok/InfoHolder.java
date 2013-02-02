package com.mityok;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.prefs.Preferences;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mityok.inter.PopulateTable;

public class InfoHolder {
	private static final String EPISODE = "episode";
	private static final String SEASON = "season";
	private static final String IMDB = "imdb";
	private static final String TITLE = "title";
	private static final String SERIAL = "serial";
	public static final String DEFAULT_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?><catalog></catalog>";
	private Preferences prefs;
	public static final String PREF_NAME = "serial_catalog";
	private Document mainDocFile;
	private final int MAX_SIZE = Preferences.MAX_VALUE_LENGTH;

	public InfoHolder(PopulateTable populator) {
		prefs = Preferences.userNodeForPackage(com.mityok.TorrentClient.class);
		String xmlPref = prefs.get(PREF_NAME, DEFAULT_XML);
		System.out.println(xmlPref.length());
		mainDocFile = getDocFromString(xmlPref);
		populator.populate(getObjectMatrixFromDoc(mainDocFile));
	}

	private Object[][] getObjectMatrixFromDoc(Document doc) {
		if (doc == null) {
			return null;
		}
		NodeList listOfEpisodes = doc.getElementsByTagName(SERIAL);
		int totalSerials = listOfEpisodes.getLength();

		Object[][] matrix = new Object[totalSerials][4];
		if (totalSerials <= 0) {
			return null;
		}

		for (int s = 0; s < totalSerials; s++) {
			Object[] obj = new Object[4];
			Node firstSerialNode = listOfEpisodes.item(s);
			if (firstSerialNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) firstSerialNode;
				obj[0] = eElement.getAttribute(TITLE);
				NodeList elementsByTagName = eElement
						.getElementsByTagName(IMDB);
				obj[1] = elementsByTagName.item(0).getTextContent();
				elementsByTagName = eElement.getElementsByTagName(SEASON);
				obj[2] = elementsByTagName.item(0).getTextContent();
				elementsByTagName = eElement.getElementsByTagName(EPISODE);
				obj[3] = elementsByTagName.item(0).getTextContent();
				matrix[s] = obj;
			}
		}

		return matrix;
	}

	public Document getDocFromString(String xml) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					xml.toString())));
			document.getDocumentElement().normalize();
			return document;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public String getStringFromDoc(Document doc) {
		try {
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer();
			StreamResult result = new StreamResult(new StringWriter());
			DOMSource source = new DOMSource(doc);
			transformer.transform(source, result);
			return result.getWriter().toString();
		} catch (TransformerException ex) {
			ex.printStackTrace();
			return null;
		}
	}

	public void resetAll() {
		prefs.put(PREF_NAME, DEFAULT_XML);
		mainDocFile = this.getDocFromString(DEFAULT_XML);
	}
	public void addNewItem(Object[] obj) {
	

		if (mainDocFile == null) {
			mainDocFile = getDocFromString(DEFAULT_XML);
		}
		// clone document
		Document mainDocFileLastVersion = null;
		try {
			mainDocFileLastVersion = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
			Node copiedRoot = mainDocFileLastVersion.importNode(
					mainDocFile.getDocumentElement(), true);
			mainDocFileLastVersion.appendChild(copiedRoot);
		} catch (ParserConfigurationException e) {

		}

		//
		Element docElem = mainDocFile.getDocumentElement();
		//
		Element rootElement = mainDocFile.createElement(SERIAL);
		docElem.appendChild(rootElement);

		Attr attr = mainDocFile.createAttribute(TITLE);
		attr.setValue((String)obj[0]);
		rootElement.setAttributeNode(attr);
		//
		Element imdbElem = mainDocFile.createElement(IMDB);
		imdbElem.appendChild(mainDocFile.createTextNode((String)obj[1]));
		rootElement.appendChild(imdbElem);
		//
		Element seasonElem = mainDocFile.createElement(SEASON);
		seasonElem.appendChild(mainDocFile.createTextNode(obj[2].toString()));
		rootElement.appendChild(seasonElem);
		//
		Element episodeElem = mainDocFile.createElement(EPISODE);
		episodeElem.appendChild(mainDocFile.createTextNode(obj[3].toString()));
		rootElement.appendChild(episodeElem);
		//
		String xml = getStringFromDoc(mainDocFile);
		if (xml.length() >= MAX_SIZE) {
			// TODO: raise an alert
			mainDocFile = mainDocFileLastVersion;
			xml = getStringFromDoc(mainDocFile);
		}
		prefs.put(PREF_NAME, xml);
	}

	public Object[][] getData() {
		return getObjectMatrixFromDoc(mainDocFile);
	}

	public void updateRow(Object[] rowData) {
		NodeList listOfEpisodes = mainDocFile.getElementsByTagName(SERIAL);
		int totalSerials = listOfEpisodes.getLength();
		if (rowData == null || totalSerials == 0) {
			return;
		}
		for (int s = 0; s < totalSerials; s++) {
			Node firstSerialNode = listOfEpisodes.item(s);
			if (firstSerialNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) firstSerialNode;
				NodeList elementsByTagName = eElement
						.getElementsByTagName(IMDB);
				if (((Element) elementsByTagName.item(0)).getTextContent()
						.equals(rowData[1])) {
					eElement.getElementsByTagName(SEASON).item(0)
							.setTextContent((String) rowData[2]);
					eElement.getElementsByTagName(EPISODE).item(0)
							.setTextContent((String) rowData[3]);
				}
			}
		}
		//
		prefs.put(PREF_NAME, getStringFromDoc(mainDocFile));
	}

	public void removeRow(Object[] rowData) {
		NodeList listOfEpisodes = mainDocFile.getElementsByTagName(SERIAL);
		int totalSerials = listOfEpisodes.getLength();
		if (rowData == null || totalSerials == 0) {
			return;
		}
		for (int s = 0; s < totalSerials; s++) {
			Node firstSerialNode = listOfEpisodes.item(s);
			if (firstSerialNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) firstSerialNode;
				NodeList elementsByTagName = eElement
						.getElementsByTagName(IMDB);
				if (((Element) elementsByTagName.item(0)).getTextContent()
						.equals(rowData[1])) {
					firstSerialNode.getParentNode()
							.removeChild(firstSerialNode);
					break;
				}
			}
		}
		prefs.put(PREF_NAME, getStringFromDoc(mainDocFile));
	}
}
