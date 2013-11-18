package com.loganfynne.clerk;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.provider.BaseColumns;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Clerk.db";
	private static DatabaseHelper sInstance = null;

	public SQLiteDatabase writeDB = this.getWritableDatabase();
	public SQLiteDatabase readDB = this.getReadableDatabase();
	
	private ArrayList<Article> read = readArticles();

	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	private static final String BLOB_TYPE = " BLOB";
	private static final String SQL_CREATE_ARTICLES =
			"CREATE TABLE IF NOT EXISTS " + ArticleEntry.TABLE_NAME + " (" +
					ArticleEntry._ID + " INTEGER PRIMARY KEY," +
					ArticleEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_AUTHOR + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_CONTENT + TEXT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_PUBLISHED + INT_TYPE + "," +
					ArticleEntry.COLUMN_NAME_ENTRYID + TEXT_TYPE + "," +
					//ArticleEntry.COLUMN_NAME_COVER + BLOB_TYPE + "," +
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
		read = readArticles();
		JSONObject j = null;
		ContentValues values;
		String title = null;
		String author = null;
		String entryid = null;
		boolean match = false;
		//JSONArray categories;
		//JSONObject c;
		//String label = null;
		boolean unread = false;
		Long published = null;
		/*byte[] bitmapdata = null;
		URL newurl = null;
		Bitmap bitmap = null;*/
		
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
						entryid = j.getString("id");

						published = j.getLong("published");
						unread = j.optBoolean("unread");
					} catch (JSONException e) {}
					
					try {
						content = j.getJSONObject("content").getString("content");
					} catch (JSONException e) {
						try {
							Log.d("Clerk",j.toString(4));
							content = j.getJSONObject("summary").getString("content");
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
						int z = 0;
						while (z < read.size()) {
							if ((read.get(z).title).equals(title)) {
								match = true;
								z = read.size();
							}
							z++;
						}
						if (!match) {
							Document doc = Jsoup.parse(content, "UTF-8");
							
							Elements images = doc.select("img[src]");
							Elements divs = doc.select("div");
							if (divs.size() > 0) {
								divs.first().attr("style","margin-top:15px;");
							}

							Element titlehead = doc.createElement("div");
							titlehead.html("<h1>" + title + "</h1><h2>By: " + author + "</h2>");

							if (images.size() > 0) {
								titlehead.attr("style","position:absolute; right:10px; top:-5px; height:170px; width:215px;");
								images.first().attr("style", "float:left; margin:30px 0 15px -1.5%; border-radius:999px; background: url(" + images.first().attr("src") + ") center center; width:120px; height:120px;");
								images.first().tagName("div");
								images.first().after(titlehead);
								//titlehead.after(author);
								titlehead.after("<div id='clearing' style=\"clear:both; margin-top:0px; margin-bottom:30px;\"></div>");

								/*try {
									URL url = new URL(image.first().attr("src"));
									Log.d("Image",image.first().attr("src"));

									HttpURLConnection connection = url.openConnection();
									connection.setRequestProperty("User-agent", "Mozilla/4.0");
									connection.setConnectTimeout(30000);
						            connection.setReadTimeout(30000);
									connection.connect();

									InputStream input = connection.getInputStream();
									Options options = new BitmapFactory.Options();
									options.inJustDecodeBounds = true;

									bitmap = BitmapFactory.decodeStream(input, null, options);
									ByteArrayOutputStream stream = new ByteArrayOutputStream();
									bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
									bitmapdata = stream.toByteArray();
								} catch (MalformedURLException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}*/
							} else {
								titlehead.attr("style","position:absolute; left:2%; top:-5px; height:170px;");
								doc.children().first().before(titlehead);
								//titlehead.after(author);
								titlehead.after("<div id='clearing' style=\"clear:both; margin-top:185px; margin-bottom:25px;\"></div>");
							}

							doc.select("a[href*=databeat2013.com]").remove();
							doc.select("img[alt*=DataBeat 2013]").remove();
							doc.select("a[href*=feedburner.com]").remove();
							doc.select("table").remove();
							doc.select("iframe").remove();
							doc.select("script").remove();
							doc.select("audio").remove();

							content = doc.toString();
							values = new ContentValues();
							values.put(ArticleEntry.COLUMN_NAME_TITLE, title);
							values.put(ArticleEntry.COLUMN_NAME_AUTHOR, author);
							values.put(ArticleEntry.COLUMN_NAME_CONTENT, content);
							values.put(ArticleEntry.COLUMN_NAME_ENTRYID, entryid);
							values.put(ArticleEntry.COLUMN_NAME_PUBLISHED, published);
							//values.put(ArticleEntry.COLUMN_NAME_COVER, bitmapdata);
							values.put(ArticleEntry.COLUMN_NAME_UNREAD, ((unread)? 1 : 0));
							writeDB.insert(ArticleEntry.TABLE_NAME, null, values);
						} else {
							match = false;
						}
					}

				}
		}
	}

	public void deleteArticle(String entryId) {
		Log.d("Deleting!",entryId);
		writeDB.delete(ArticleEntry.TABLE_NAME, ArticleEntry.COLUMN_NAME_ENTRYID + "=" + entryId, null);
	}

	public ArrayList<Article> readArticles() {
		ArrayList<Article> articles = new ArrayList<Article>();
		Article article;

		Cursor c = readDB.rawQuery("select * from " +  ArticleEntry.TABLE_NAME + " order by "+ ArticleEntry.COLUMN_NAME_PUBLISHED +" desc", null);
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
		//Bitmap bitmap = BitmapFactory.decodeByteArray(c.getBlob(1), 0, c.getBlob(1).length);
		return new Article(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getInt(1), null, c.getInt(2));
	}

	public static abstract class ArticleEntry implements BaseColumns {
		public static final String TABLE_NAME = "articles";

		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_PUBLISHED = "published";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_CONTENT = "content";
		public static final String COLUMN_NAME_ENTRYID = "entryid";
		//public static final String COLUMN_NAME_COVER = "cover";
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
		boolean match = false;
		
		Cursor c = readDB.rawQuery("select * from " + SubscriptionEntry.TABLE_NAME, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			newsub[0] = c.getString(1);
			newsub[1] = c.getString(2);
			newsub[2] = c.getString(3);
			Log.d("Clerk", "Sub " + newsub[0] + " " + newsub[1] + " " + newsub[2]);
			//TODO fix this
			for (int x=0; x < sub.size(); x++) {
				if (newsub[0].equals(sub.get(x)[0])) {
					match = true;
				}
			}
			if (!match) {
				sub.add(newsub);
			} else {
				match = false;
			}
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