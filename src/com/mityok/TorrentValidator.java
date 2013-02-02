package com.mityok;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import com.mityok.inter.NotificationHandler;
import com.mityok.inter.NotificationItem;
import com.mityok.inter.NotificationItem.Status;
import com.mityok.inter.TableDataHahdler;
import com.mityok.inter.TorrentLoadUpdateHandler;
import com.mityok.model.AirDateData;

public class TorrentValidator {
	Timer timer;
	ImdbTimerTask imdbTimerTask;
	private TorrentLoadUpdateHandler torrentLoadUpdateHandler;
	private boolean isScheduled;

	public TorrentValidator() {
		timer = new Timer();
		imdbTimerTask = new ImdbTimerTask(new NotificationHandler() {

			@Override
			public void respond(NotificationItem message) {
				if (message.getStatus().equals(Status.SUCCESS)) {
					// list of all episodes
					List<List<AirDateData>> episodes = imdbTimerTask
							.getEpisodes();
					if (episodes == null) {
						return;
					}
					final List<AirDateData> validLinks = new ArrayList<AirDateData>();
					for (List<AirDateData> list : episodes) {
						for (AirDateData airDateData : list) {
							if (airDateData.isValid()) {
								validLinks.add(airDateData);
							}
						}
					}
					System.out.println(validLinks);
					if (validLinks.isEmpty()) {
						return;
					}
					new TorrentGetterThread(new NotificationHandler() {

						@Override
						public void respond(NotificationItem message) {
							if (message.getStatus().equals(Status.SUCCESS)) {
								torrentLoadUpdateHandler.update(validLinks);
							}
						}
					}, validLinks).start();
				}
			}
		});
	}

	public void init() {
		if (isScheduled) {
			imdbTimerTask.run();
		} else {
			isScheduled = true;
			timer.schedule(imdbTimerTask, 0, 5 * 60 * 1000);
		}

	}

	public void handleUpdate(TorrentLoadUpdateHandler torrentLoadUpdateHandler) {
		this.torrentLoadUpdateHandler = torrentLoadUpdateHandler;
	}

	public void setDataGetter(TableDataHahdler tableDataHahdler) {
		imdbTimerTask.setDataGetter(tableDataHahdler);

	}
}
