package com.dreaminsteam.rpbot.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;

import javax.swing.text.DateFormatter;

import com.dreaminsteam.rpbot.db.DatabaseUtil;
import com.dreaminsteam.rpbot.db.models.Player;
import com.j256.ormlite.dao.Dao;

import it.sauronsoftware.cron4j.Scheduler;

/**
 *  This class schedules a timer at a configurable time, every 24 hours.
 *  
 *  The config file is located in <user home directory>/DiscordRPBot/DestinyPointReset.properties.
 *  
 *  The format of the file is as follows (with defaults):
 *  
 *  TIMEZONE : America/New_York
 *  HOUR : 5
 *  MINUTE : 0
 *  
 *  If at any point the program is unable to read the file, it will revert back to using these defaults.
 */
public class DestinyPointResetHandler {
	
	
	
	private static final String path =  "DiscordRPBot/DestinyPointReset.properties";

	public static void setupResetHandler(){
		Properties properties = new Properties();
		
		try (FileInputStream inputStream = new FileInputStream(new File(System.getProperty("user.home"), path))){
			properties.load(inputStream);
		} catch (IOException e) {}
		
		String timeZone = properties.getProperty("TIMEZONE", "America/New_York");
		int hour = Integer.parseInt(properties.getProperty("HOUR", "5"));
		int minute = Integer.parseInt(properties.getProperty("MINUTE", "0"));
		
		if (hour < 0 || hour >= 24){
			hour = 5;
		}
		
		if(minute < 0 || minute >= 60){
			minute = 0;
		}
		
		TimeZone timezone;
		try {
			timezone = TimeZone.getTimeZone(timeZone);
		} catch (Exception e){
			timezone = null;
		}
		
		Scheduler sched = new Scheduler();
		
		if (timezone != null){
			sched.setTimeZone(timezone);
		}
		
		String cronTab = minute + " " + hour + " * * *";
		
		sched.schedule(cronTab, () -> {
			resetAllDestinyPoints();
		});
		
		sched.start();
	}

	
	
	
	
	public static void resetAllDestinyPoints(){
		Dao<Player, String> dao = DatabaseUtil.getPlayerDao();
		
		// there might be a better way to do this that doesn't involve raw db commands.
		// the problem is that I don't think SQlite supports dropping a single column like this at all, 
		// and ormlite is probably trying to cater to the lowest common denominator.
		// (though maybe it's hidden away somewhere in some h2 specific subclass and I just couldn't find it??)
		
		// I suppose I could just iterate through each Player, but that sounds horribly inefficient.
		try {
			dao.executeRawNoArgs("ALTER TABLE players DROP COLUMN usedDestiny");
			dao.executeRawNoArgs("ALTER TABLE players ADD usedDestiny INT");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	
}