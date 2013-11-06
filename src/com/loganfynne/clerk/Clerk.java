package com.loganfynne.clerk;

import java.util.ArrayList;

import android.app.Application;

public class Clerk extends Application {
	private static Clerk singleton;
	static ArrayList<Article> articles = null;
	static DatabaseHelper db = null;
	static String access = null;
	
	public String getAccess() {
		return access;
	}
	
	public static Clerk getInstance() {
		return singleton;
	}
	
	public static DatabaseHelper getDatabase() {
		db = DatabaseHelper.getInstance(singleton);
		return db;
	}
	
	public void onCreate() {
		super.onCreate();
		singleton = this;
	}
	
	public void onTerminate() {
		db.readDB.close();
		db.writeDB.close();
	}
	
}