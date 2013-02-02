package com.mityok;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.HtmlNode;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.TagNodeVisitor;

import com.mityok.inter.NotificationHandler;
import com.mityok.inter.NotificationItem;
import com.mityok.inter.NotificationItem.Status;
import com.mityok.inter.TableDataHahdler;
import com.mityok.model.AirDateData;
import com.mityok.model.TorrentImdbData;

public class ImdbTimerTask extends TimerTask {

	private static final String MMM_DD_YYYY = "MMM. dd, yyyy";
	private static final int CURRENT_YEAR = 2013;
	private NotificationHandler handler;
	private List<TorrentImdbData> torrentImdbDataList;
	private List<List<AirDateData>> episodes;
	private TableDataHahdler tableDataHahdler;

	public List<List<AirDateData>> getEpisodes() {
		return episodes;
	}

	public ImdbTimerTask(NotificationHandler handler) {
		this.handler = handler;
	}

	public void setDataGetter(TableDataHahdler tableDataHahdler) {
		this.tableDataHahdler = tableDataHahdler;
	}

	private void setData(Object[][] objects) {
		torrentImdbDataList = new ArrayList<TorrentImdbData>();
		for (int i = 0; i < objects.length; i++) {
			TorrentImdbData data = new TorrentImdbData((String) objects[i][0],
					(String) objects[i][1],
					Integer.parseInt(((String) objects[i][2])),
					Integer.parseInt(((String) objects[i][3])), new Date());
			torrentImdbDataList.add(data);
		}

	}

	public void run() {
		setData(tableDataHahdler.init());
		if (torrentImdbDataList != null && !torrentImdbDataList.isEmpty()) {
			episodes = new ArrayList<List<AirDateData>>();
			for (TorrentImdbData torrentImdbData : torrentImdbDataList) {
				nextItem(torrentImdbData);
			}
		}
		handler.respond(new NotificationItem(Status.SUCCESS, "done"));
	}

	private void nextItem(final TorrentImdbData torrentImdbData) {
		if (torrentImdbData == null) {
			return;
		}
		String urlString = "http://www.imdb.com/title/"
				+ torrentImdbData.getImdbLink() + "/episodes/_ajax?year="
				+ CURRENT_YEAR;
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			BufferedReader rd = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			StringBuilder responseBuilder = new StringBuilder();
			String line;
			while ((line = rd.readLine()) != null) {
				responseBuilder.append(line);
			}
			HtmlCleaner cleaner = new HtmlCleaner();
			CleanerProperties props = cleaner.getProperties();
			props.setTranslateSpecialEntities(true);
			props.setTransResCharsToNCR(true);
			props.setOmitComments(true);
			TagNode node = cleaner.clean(responseBuilder.toString());
			final List<AirDateData> list = new ArrayList<>();
			node.traverse(new TagNodeVisitor() {

				@Override
				public boolean visit(TagNode arg0, HtmlNode htmlNode) {
					if (htmlNode instanceof TagNode) {
						TagNode tag = (TagNode) htmlNode;
						String tagName = tag.getName();
						if ("div".equals(tagName)) {
							String src = tag.getAttributeByName("class");
							if ("airdate".equals(src)) {
								String text = tag.getText().toString().trim();
								AirDateData airDateData = list.get(list.size() - 1);
								if (!text.isEmpty() && text.length() > 4) {
									SimpleDateFormat formatter = new SimpleDateFormat(
											MMM_DD_YYYY);
									if (airDateData.getDate() == null) {
										try {
											airDateData
													.setDate((Date) formatter
															.parse(text));
											if (airDateData.getDate().after(
													torrentImdbData.getDate())) {
												airDateData.setValid(false);
											}
										} catch (ParseException e) {
											e.printStackTrace();
										}
									}

								} else {
									airDateData.setValid(false);
								}

							} else if (src != null
									&& src.indexOf("zero-z-index") >= 0) {
								String text = tag.getText().toString().trim();
								if (!text.isEmpty()) {
									AirDateData airDateData = new AirDateData(
											torrentImdbData.getTitle(), null,
											-1, -1, torrentImdbData
													.getImdbLink());
									Pattern p = Pattern.compile("\\d+");
									Matcher m = p.matcher(text);
									while (m.find()) {
										if (airDateData.getSeason() < 0) {
											airDateData.setSeason(Integer
													.parseInt(m.group()));
											if (airDateData.getSeason() < torrentImdbData
													.getSeason()) {
												airDateData.setValid(false);
											}
										} else if (airDateData.getEpisode() < 0) {
											airDateData.setEpisode(Integer
													.parseInt(m.group()));
											if (airDateData.getEpisode() <= torrentImdbData
													.getEpisode()) {
												airDateData.setValid(false);
											}
										}
									}
									list.add(airDateData);
								}
							}

						}
					}
					return true;
				}

			});
			episodes.add(list);

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
