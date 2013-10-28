package com.loganfynne.clerk;

import java.util.ArrayList;

import android.app.Application;

public class Clerk extends Application {
	static Clerk singleton;
	static ArrayList<Article> articles = null;
	static DatabaseHelper db = null;
	
	public Clerk getInstance() {
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