package com.loganfynne.clerk;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Clerk.db";
	private static DatabaseHelper sInstance = null;

	public SQLiteDatabase writeDB = this.getWritableDatabase();
	public SQLiteDatabase readDB = this.getReadableDatabase();

	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String SQL_CREATE_ARTICLES =
			"CREATE TABLE IF NOT EXISTS " + ArticleEntry.TABLE_NAME + " (" +
					ArticleEntry._ID + " INTEGER PRIMARY KEY," +
					ArticleEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_AUTHOR + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_CONTENT + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_PUBLISHED + INT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_ENTRYID + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_UNREAD + INT_TYPE +
					" );";

	private static final String SQL_CREATE_AUTHENTICATION = 
			"CREATE TABLE IF NOT EXISTS " + AuthEntry.TABLE_NAME + "(" +
					AuthEntry._ID + " INTEGER PRIMARY KEY," +
					AuthEntry.COLUMN_NAME_REFRESH + TEXT_TYPE + "," +
					AuthEntry.COLUMN_NAME_USERID + TEXT_TYPE + ");";
	
	private static final String SQL_CREATE_SUBSCRIPTION = 
			"CREATE TABLE IF NOT EXISTS " + SubscriptionEntry.TABLE_NAME + "(" +
					SubscriptionEntry._ID + " INTEGER PRIMARY KEY," +
					SubscriptionEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
					SubscriptionEntry.COLUMN_NAME_TIMESTAMP + TEXT_TYPE + "," +
					SubscriptionEntry.COLUMN_NAME_FEEDID + TEXT_TYPE + ");";

	public static DatabaseHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new DatabaseHelper(context.getApplicationContext());
		}

		return sInstance;
	}

	private DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ARTICLES);
		db.execSQL(SQL_CREATE_AUTHENTICATION);
		db.execSQL(SQL_CREATE_SUBSCRIPTION);
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public void writeArticles(ArrayList<JSONObject> articles) {
		JSONObject j = null;
		ContentValues values;
		String title = null;
		String author = null;
		String id = null;
		//JSONArray categories;
		//JSONObject c;
		//String label = null;
		boolean unread = false;
		Long published = null;
		for (int x = 0; x < articles.size(); x++) {
			JSONArray items = null;
			String content = null;
			try {
				items = articles.get(x).getJSONArray("items");
			} catch (JSONException e) {
				e.printStackTrace();
			}
				for (int i = 0; i < items.length(); i++) {
					try {
						j = items.getJSONObject(i);
						title = j.getString("title");
						author = j.getString("author");
						id = j.getString("id");

						published = j.getLong("published");
						unread = j.optBoolean("unread");
					} catch (JSONException e) {}
					
					try {
						content = j.getJSONObject("content").getString("content");
					} catch (JSONException e) {
						try {
							Log.d("Clerk",j.toString(4));
							content = j.getJSONObject("content").getString("summary");
						} catch (JSONException e1) {
							content = "";
						}
					}
					//categories = j.getJSONArray("categories");

					/*for (int y = 0; y < categories.length(); y++) {
					c = categories.getJSONObject(y);
					label = c.getString("label");
				}*/
					if (content != "") {
						values = new ContentValues();
						values.put(ArticleEntry.COLUMN_NAME_TITLE, title);
						values.put(ArticleEntry.COLUMN_NAME_AUTHOR, author);
						values.put(ArticleEntry.COLUMN_NAME_CONTENT, content);
						values.put(ArticleEntry.COLUMN_NAME_ENTRYID, id);
						values.put(ArticleEntry.COLUMN_NAME_PUBLISHED, published);
						values.put(ArticleEntry.COLUMN_NAME_UNREAD, ((unread)? 1 : 0));
						writeDB.insert(ArticleEntry.TABLE_NAME, null, values);
					}

				}
		}
	}

	public void deleteArticle(String entryId) {
		Log.d("Deleting!",entryId);
		writeDB.delete(ArticleEntry.TABLE_NAME, ArticleEntry.COLUMN_NAME_ENTRYID + "=" + entryId, null);
	}

	public ArrayList<String> readTitles() {
		ArrayList<String> titles = new ArrayList<String>();

		Cursor c = readDB.rawQuery("select * from " + ArticleEntry.TABLE_NAME, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			titles.add(c.getString(1));
			Log.d("add",c.getString(1));
			c.moveToNext();
		}
		c.close();

		return titles;
	}

	public ArrayList<Article> readArticles() {
		ArrayList<Article> articles = new ArrayList<Article>();
		Article article;

		Cursor c = readDB.rawQuery("select * from " + ArticleEntry.TABLE_NAME, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			article = cursorToArticle(c);
			articles.add(article);
			c.moveToNext();
		}
		c.close();

		return articles;
	}

	private Article cursorToArticle(Cursor c) {
		return new Article(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(1), c.getInt(2));
	}

	public static abstract class ArticleEntry implements BaseColumns {
		public static final String TABLE_NAME = "articles";

		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_PUBLISHED = "published";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_CONTENT = "content";
		public static final String COLUMN_NAME_ENTRYID = "entryid";
		public static final String COLUMN_NAME_UNREAD = "unread";
	}

	public static abstract class AuthEntry implements BaseColumns {
		public static final String TABLE_NAME = "authentication";
		
		public static final String COLUMN_NAME_REFRESH = "refresh";
		public static final String COLUMN_NAME_USERID = "userid";
	}
	
	public static abstract class SubscriptionEntry implements BaseColumns {
		public static final String TABLE_NAME = "subscriptions";
		
		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
		public static final String COLUMN_NAME_FEEDID = "feedid";
	}
	
	public void writeSubscription(String title, Long timestamp, String feedid) {
		ContentValues values = new ContentValues();
		Log.d("Writing", "subscription" + title);
		values.put(SubscriptionEntry.COLUMN_NAME_TITLE, title);
		values.put(SubscriptionEntry.COLUMN_NAME_TIMESTAMP, Long.toString(timestamp));
		values.put(SubscriptionEntry.COLUMN_NAME_FEEDID, feedid);
		writeDB.insert(SubscriptionEntry.TABLE_NAME, null, values);
	}
	
	public ArrayList<String[]> readSubscription() {
		ArrayList<String[]> sub = new ArrayList<String[]>();
		String[] newsub = {"","",""};
		
		Cursor c = readDB.rawQuery("select * from " + SubscriptionEntry.TABLE_NAME, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			newsub[0] = c.getString(1);
			newsub[1] = c.getString(2);
			newsub[2] = c.getString(3);
			Log.d("Clerk", "Sub " + newsub[0] + " " + newsub[1] + " " + newsub[2]);
			sub.add(newsub);
			c.moveToNext();
		}
		c.close();
		
		return sub;
	}
	
	public void writeToken(String refresh, String userid) {
		ContentValues values = new ContentValues();
		Log.d("Writing", "refresh" + refresh + " userid " + userid);
		values.put(AuthEntry.COLUMN_NAME_REFRESH, refresh);
		values.put(AuthEntry.COLUMN_NAME_USERID, userid);
		writeDB.insert(AuthEntry.TABLE_NAME, null, values);
	}

	public String[] readToken() {
		String[] token = {"",""};

		Cursor c = readDB.rawQuery("select * from " + AuthEntry.TABLE_NAME, null);
		
		Log.d("Database", "readToken");
		c.moveToFirst();
		while (!c.isAfterLast()) {
			token[0] = c.getString(1);
			token[1] = c.getString(2);
			Log.d("token0","token" + token[0]);
			Log.d("token1", "token" + token[1]);
			c.moveToNext();
		}
		c.close();
		
		Log.d("Clerk","No break");

		return token;
	}
}