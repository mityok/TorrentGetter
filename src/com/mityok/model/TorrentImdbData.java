package com.mityok.model;

import java.util.Date;

public class TorrentImdbData {
	private String title;
	private String imdbLink;
	private int season;
	private int episode;
	private Date date;

	public TorrentImdbData(String title, String imdbLink, int season,
			int episode, Date date) {

		this.title = title;
		this.imdbLink = imdbLink;
		this.season = season;
		this.episode = episode;
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getImdbLink() {
		return imdbLink;
	}

	public void setImdbLink(String imdbLink) {
		this.imdbLink = imdbLink;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public int getEpisode() {
		return episode;
	}

	public void setEpisode(int episode) {
		this.episode = episode;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}
}
