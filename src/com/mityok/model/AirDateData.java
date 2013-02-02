package com.mityok.model;

import java.util.Date;

public class AirDateData {
	private String title;
	

	private Date date;
	private int season;
	private int episode;
	private String imdbLink;
	private boolean isValid;
	private boolean isLoading;

	public AirDateData(String title,Date date, int season, int episode, String imdb) {
		this.title=title;
		this.date = date;
		this.season = season;
		this.episode = episode;
		imdbLink = imdb;
		isValid = true;
	}
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
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

	@Override
	public String toString() {
		return "{title: "+title+", imdbLink: " + imdbLink + ", date: " + date + ", season: "
				+ season + ", episode: " + episode + ", valid: "+isValid+"}";
	}

	public String getImdbLink() {
		return imdbLink;
	}

	public void setImdbLink(String imdbLink) {
		this.imdbLink = imdbLink;
	}

	public boolean isValid() {
		return isValid;
	}

	public void setValid(boolean isValid) {
		this.isValid = isValid;
	}
	public boolean isLoading() {
		return isLoading;
	}
	public void setLoading(boolean isLoading) {
		this.isLoading = isLoading;
	}

}
