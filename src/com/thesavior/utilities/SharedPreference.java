package com.thesavior.utilities;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreference {
	
	public static void putdata(Context ct , String key , String value)
	{
		SharedPreferences myPrefs = ct.getSharedPreferences("myPrefs", 1);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putString(key, value);
        prefsEditor.commit();
	}
	
	public static String getdata(Context ct , String key)
	{
		SharedPreferences myPrefs = ct.getSharedPreferences("myPrefs", 1);
        String prefName = myPrefs.getString(key, "");
        return prefName;                         
	}
	
	public static void putInt(Context ct , String key , int value)
	{
		SharedPreferences myPrefs = ct.getSharedPreferences("myPrefs", 1);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putInt(key, value);
        prefsEditor.commit();
	}
	
	public static int getInt(Context ct , String key)
	{
		SharedPreferences myPrefs = ct.getSharedPreferences("myPrefs", 1);
        int prefName = myPrefs.getInt(key, 0);
        return prefName;                          
	}

	public static void putBoolean(Context ct , String key , boolean value)
	{
		SharedPreferences myPrefs = ct.getSharedPreferences("myPrefs", 1);
        SharedPreferences.Editor prefsEditor = myPrefs.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.commit();
	}
		
	public static Boolean getBoolean(Context ct , String key)
	{
		SharedPreferences myPrefs = ct.getSharedPreferences("myPrefs", 1);
        boolean prefName = myPrefs.getBoolean(key, false);
        return prefName;                          
	}
}
