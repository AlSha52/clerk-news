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
					ArticleEntry.COLUMN_NAME_UNREAD + INT_TYPE +
					" );";

	private static final String SQL_CREATE_AUTHENTICATION = 
			"CREATE TABLE IF NOT EXISTS " + AuthEntry.TABLE_NAME + "(" +
					AuthEntry._ID + " INTEGER PRIMARY KEY," +
					AuthEntry.COLUMN_NAME_REFRESH + TEXT_TYPE + "," +
					AuthEntry.COLUMN_NAME_USERID + TEXT_TYPE + ");";

	private static final String SQL_DELETE_ARTICLES = 
			"DELETE FROM " + ArticleEntry.TABLE_NAME + " WHERE id IN (";

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
	}

	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onCreate(db);
	}

	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

	public void writeArticles(JSONObject articles) {
		JSONObject j = null;
		ContentValues values;
		String title = null;
		String author = null;
		//JSONArray categories;
		//JSONObject c;
		//String label = null;
		boolean unread = false;
		Long published = null;
		
		try {
			JSONArray items = articles.getJSONArray("items");
			for (int i = 0; i < items.length(); i++) {
				j = items.getJSONObject(i);
				title = j.getString("title");
				author = j.getString("author");
				
				published = j.getLong("published");
				unread = j.optBoolean("unread");
				//categories = j.getJSONArray("categories");

				/*for (int y = 0; y < categories.length(); y++) {
					c = categories.getJSONObject(y);
					label = c.getString("label");
				}*/
				
				values = new ContentValues();
				values.put(ArticleEntry.COLUMN_NAME_TITLE, title);
				values.put(ArticleEntry.COLUMN_NAME_AUTHOR, author);
				values.put(ArticleEntry.COLUMN_NAME_CONTENT, j.getJSONObject("content").getString("content"));
				values.put(ArticleEntry.COLUMN_NAME_PUBLISHED, published);
				values.put(ArticleEntry.COLUMN_NAME_UNREAD, ((unread)? 1 : 0));
				writeDB.insert(ArticleEntry.TABLE_NAME, null, values);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void deleteArticles(String list) {
		writeDB.execSQL(SQL_DELETE_ARTICLES + list + ");");
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
			Log.d("Article", article.toString());
			articles.add(article);
			c.moveToNext();
		}
		c.close();

		return articles;
	}

	private Article cursorToArticle(Cursor c) {
		Log.d("Database", "Title " + c.getString(1));
		Log.d("Database", "Category " + c.getString(2));
		Log.d("Database", "Author " + c.getString(3));
		Log.d("Database", "Content " + c.getString(4));
		Log.d("Database", "Published " + Long.toString(c.getInt(1)));
		Log.d("Database", "Unread " + Integer.toString(c.getInt(2)));
		return new Article(c.getString(1), c.getString(2), c.getString(3), c.getInt(1), c.getInt(2));
	}

	public static abstract class ArticleEntry implements BaseColumns {
		public static final String TABLE_NAME = "articles";

		public static final String COLUMN_NAME_TITLE = "title";
		public static final String COLUMN_NAME_PUBLISHED = "published";
		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String COLUMN_NAME_CONTENT = "content";
		public static final String COLUMN_NAME_UNREAD = "unread";
	}

	public static abstract class AuthEntry implements BaseColumns {
		public static final String TABLE_NAME = "authentication";
		
		public static final String COLUMN_NAME_REFRESH = "refresh";
		public static final String COLUMN_NAME_USERID = "userid";
	}

	public void writeToken(String refresh, String userid) {
		ContentValues values = new ContentValues();
		values.put(AuthEntry.COLUMN_NAME_REFRESH, refresh);
		values.put(AuthEntry.COLUMN_NAME_USERID, userid);
		writeDB.insert(AuthEntry.TABLE_NAME, null, values);
	}

	public String[] readToken() {
		String[] token = {"",""};

		Cursor c = readDB.rawQuery("select * from " + AuthEntry.TABLE_NAME, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			token[0] = c.getString(1);
			token[1] = c.getString(2);
			c.moveToNext();
		}
		c.close();

		return token;
	}
}