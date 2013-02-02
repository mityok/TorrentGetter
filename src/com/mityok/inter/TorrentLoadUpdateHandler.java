package com.mityok.inter;

import java.util.List;

import com.mityok.model.AirDateData;

public interface TorrentLoadUpdateHandler {
	public void update(List<AirDateData> validLinks);
}
