package org.zirco.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.zirco.R;

import android.content.Context;
import android.util.Log;

public class DateUtils {
	
	private static String getDefaultFormat(Context context) {
		return context.getResources().getString(R.string.DATE_FORMAT_ISO8601);
	}
	
	public static String getNow(Context context) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(getDefaultFormat(context));				
		
		return sdf.format(c.getTime());
	} 
	
	public static String getHistoryLimit(Context context) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DAY_OF_MONTH, -5);
		
		SimpleDateFormat sdf = new SimpleDateFormat(getDefaultFormat(context));
		
		return sdf.format(c.getTime());		
	}
	
	public static Date getDateAtMidnight(int roll) {
		Calendar c = Calendar.getInstance();
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		
		c.roll(Calendar.DAY_OF_MONTH, roll);
		
		return c.getTime();
	}
	
	public static String getDateAsUniversalString(Context context, Date date) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		SimpleDateFormat sdf = new SimpleDateFormat(getDefaultFormat(context));				
		
		return sdf.format(c.getTime());
	}
	
	public static String getDisplayDate(Context context, Date date) {
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
	}
	
	public static Date convertFromDatabase(Context context, String date) {
		SimpleDateFormat sdf = new SimpleDateFormat(getDefaultFormat(context));
		
		try {
			
			return sdf.parse(date);
			
		} catch (ParseException e) {
			Log.w(DateUtils.class.toString(), "Error parsing date (" + date + "): " + e.getMessage());
			
			return new Date();
		}
	}

}
