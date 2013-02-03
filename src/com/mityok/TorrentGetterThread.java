package com.mityok;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.mityok.inter.NotificationHandler;
import com.mityok.inter.NotificationItem;
import com.mityok.inter.NotificationItem.Status;
import com.mityok.model.AirDateData;
import com.mityok.model.TorrentData;

public class TorrentGetterThread extends Thread {

	private static final String BASE_URL = "http://torrentz.eu";
	private static final String MAGNET = "magnet:?xt";
	private List<String> torrentSitesLinks;
	private int linksCounter = 0;
	private NotificationHandler notificationHandler;
	private List<AirDateData> validLinks;
	private String format = "%1$02d";
	private AirDateData currentLink;

	public TorrentGetterThread(NotificationHandler notificationHandler,
			List<AirDateData> validLinks) {
		this.notificationHandler = notificationHandler;
		this.validLinks = validLinks;
	}

	@Override
	public void run() {
		for (AirDateData link : validLinks) {
			currentLink = link;
			if (currentLink != null) {
				loadTorrent();
			}
		}
		notificationHandler.respond(new NotificationItem(Status.SUCCESS,
				"download started"));
	}

	private String buildValidName(AirDateData link) {
		// BASE_URL + "/search?f=The+Big+Bang+Theory+s06e12";
		String searchUrl = BASE_URL + "/search?f="
				+ link.getTitle().replace(" ", "+");
		if (link.isByDate()) {
			DateFormat df = new SimpleDateFormat("yyyy+MM+dd");
			searchUrl += "+" + df.format(link.getDate());
		} else {
			searchUrl += "+s" + String.format(format, link.getSeason()) + "e"
					+ String.format(format, link.getEpisode());
		}
		return searchUrl;
	}

	private void loadTorrent() {
		String urlString = buildValidName(currentLink);
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				responseBuilder.append(line + '\n');
			}
			String string = responseBuilder.toString();
			parseTorrentsListings(string);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	private void parseTorrentsListings(String responce) {
		Pattern titleFinder = Pattern.compile("<dl[^>]*>(.*?)</dl>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(responce);
		StringBuilder xml = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><data>");
		while (regexMatcher.find()) {
			String group = regexMatcher.group(1);
			group.replaceAll("<b>", "");
			group.replaceAll("</b>", "");
			xml.append("<dl>" + group + "</dl>");
		}
		xml.append("</data>");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					xml.toString())));

			document.getDocumentElement().normalize();

			NodeList listOfPersons = document.getElementsByTagName("dl");
			int totalPersons = listOfPersons.getLength();

			if (totalPersons <= 4) {
				return;
			}
			List<TorrentData> torrents = new ArrayList<TorrentData>();
			for (int s = 0; s < totalPersons; s++) {
				Node firstPersonNode = listOfPersons.item(s);
				if (firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) firstPersonNode;
					NodeList elementsByTagName = eElement
							.getElementsByTagName("span");
					int spanLength = elementsByTagName.getLength();
					if (spanLength > 0) {
						String date = "";
						int size = 0;

						for (int i = 0; i < spanLength; i++) {
							String data = elementsByTagName.item(i)
									.getTextContent();
							String attribute = ((Element) elementsByTagName
									.item(i)).getAttribute("title");
							if (data.toLowerCase().indexOf("mb") >= 0) {
								size = Integer.parseInt(data.toLowerCase()
										.replace("mb", "").trim());
							} else if (!attribute.isEmpty()) {
								date = attribute;
							}
						}
						TorrentData torrent = new TorrentData(eElement
								.getElementsByTagName("a").item(0)
								.getTextContent(),
								((Element) eElement.getElementsByTagName("a")
										.item(0)).getAttribute("href"), date,
								0, size);
						torrents.add(torrent);
					}

				}
			}
			loadLink(torrents.get(0));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void loadLink(TorrentData torrentData) {
		String urlString = BASE_URL + torrentData.getUrl();
		System.out.println(urlString);
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");

			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				responseBuilder.append(line + '\n');
			}
			String string = responseBuilder.toString();
			parseTorrentsLinks(string);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	private void parseTorrentsLinks(String responce) {
		Pattern titleFinder = Pattern.compile("<dl[^>]*>(.*?)</dl>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(responce);
		StringBuilder xml = new StringBuilder(
				"<?xml version=\"1.0\" encoding=\"utf-8\"?><data>");
		while (regexMatcher.find()) {
			String group = regexMatcher.group(1);
			group.replaceAll("<b>", "");
			group.replaceAll("</b>", "");
			xml.append("<dl>" + group + "</dl>");
		}
		xml.append("</data>");
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();
			Document document = builder.parse(new InputSource(new StringReader(
					xml.toString())));

			document.getDocumentElement().normalize();

			NodeList listOfPersons = document.getElementsByTagName("dl");
			int totalPersons = listOfPersons.getLength();

			if (totalPersons <= 0) {
				return;
			}
			torrentSitesLinks = new ArrayList<String>();
			linksCounter = 0;
			for (int s = 0; s < totalPersons; s++) {
				Node firstPersonNode = listOfPersons.item(s);
				if (firstPersonNode.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) firstPersonNode;

					String attribute = ((Element) eElement
							.getElementsByTagName("a").item(0))
							.getAttribute("href");
					if (attribute.indexOf("http:") >= 0) {
						torrentSitesLinks.add(attribute);
					}

				}
			}
			if (!torrentSitesLinks.isEmpty()) {
				launchTorrentSite(torrentSitesLinks.get(linksCounter));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void launchTorrentSite(String urlString) {

		System.out.println(urlString);
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			conn.setRequestProperty(
					"User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 6.0; en-US; rv:1.9.1.2) Gecko/20090729 Firefox/3.5.2 (.NET CLR 3.5.30729)");
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				responseBuilder.append(line + '\n');
			}
			String string = responseBuilder.toString();
			int magnetPos = string.indexOf(MAGNET);
			if (magnetPos < 0) {
				checkNext();
			} else {
				extractLink(magnetPos, string);
			}
		} catch (Exception e1) {
			checkNext();
		}

	}

	private void extractLink(int magnetPos, String resp) {
		int finalPos = 0;
		for (int i = magnetPos; i < resp.length() - 1; i++) {
			if (resp.substring(i, i + 1).equals("\"")) {
				finalPos = i;
				break;
			}
		}
		if (finalPos > magnetPos) {
			String magnetLink = resp.substring(magnetPos, finalPos);
			System.out.println(magnetLink);
			java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
			if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
				System.err
						.println("Desktop doesn't support the browse action (fatal)");

			}
			java.net.URI uri;
			try {
				uri = new java.net.URI(magnetLink);
				desktop.browse(uri);
				if (currentLink != null) {
					currentLink.setLoading(true);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void checkNext() {
		linksCounter++;
		if (linksCounter < torrentSitesLinks.size()) {
			launchTorrentSite(torrentSitesLinks.get(linksCounter));
		} else {
			notificationHandler.respond(new NotificationItem(Status.FAIL,
					"no valid nagnet links"));
		}

	}

}
