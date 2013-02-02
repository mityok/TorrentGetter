package com.mityok.model;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TorrentData {
	private String title;
	private String url;
	private String date;
	private int seeds;
	private int size;
	private Date dateObj;

	public TorrentData(String title, String url, String date, int seeds,
			int size) {
		super();
		this.title = title;
		this.url = url;
		this.date = date;
		this.seeds = seeds;
		this.size = size;
		try {
			dateObj = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.ENGLISH).parse(date);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "{title:"+title+", size:"+size+", date:"+dateObj+", seeds:"+seeds+"}";
	}

	public String getUrl() {
		// TODO Auto-generated method stub
		return url;
	}

}
