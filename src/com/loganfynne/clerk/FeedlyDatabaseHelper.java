package com.loganfynne.clerk;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class FeedlyDatabaseHelper extends SQLiteOpenHelper {
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Clerk.db";
	private static FeedlyDatabaseHelper sInstance = null;
	
	public SQLiteDatabase writeDB = this.getWritableDatabase();
	public SQLiteDatabase readDB = this.getReadableDatabase();
	
	private static final String TEXT_TYPE = " TEXT";
	private static final String INT_TYPE = " INTEGER";
	//String title, String description, String link, String date, String author, String image, String categories, String favicon
	private static final String SQL_CREATE_ARTICLES =
		    "CREATE TABLE IF NOT EXISTS " + ArticleEntry.TABLE_NAME + " (" +
		    ArticleEntry._ID + " INTEGER PRIMARY KEY," +
		    ArticleEntry.COLUMN_NAME_TITLE + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_DESCRIPTION + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_LINK + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_DATE + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_AUTHOR + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_IMAGE + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_CATEGORIES + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_FAVICON + TEXT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_SETS + INT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_RANK + INT_TYPE + "," +
		    ArticleEntry.COLUMN_NAME_SAVE + INT_TYPE +
		    " );";
	
	private static final String SQL_CREATE_AUTHENTICATION = 
			"CREATE TABLE IF NOT EXISTS " + AuthEntry.TABLE_NAME + "(" +
			AuthEntry._ID + " INTEGER PRIMARY KEY," +
			AuthEntry.COLUMN_NAME_REFRESH + TEXT_TYPE + ");";

	private static final String SQL_DELETE_ARTICLES = 
		"DELETE FROM " + ArticleEntry.TABLE_NAME + " WHERE id IN (";

	public static FeedlyDatabaseHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new FeedlyDatabaseHelper(context.getApplicationContext());
		}
		
		return sInstance;
	}

	private FeedlyDatabaseHelper(Context context) {
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

	public void writeArticles(ArrayList<Article> writeArticles) {
		ContentValues values;

		for (Article a : writeArticles) {
			values = new ContentValues();
			//String title, String description, String link, String date, String author, String image, String categories, String favicon
			values.put(ArticleEntry.COLUMN_NAME_TITLE, a.title);
			values.put(ArticleEntry.COLUMN_NAME_LINK, a.link);
			values.put(ArticleEntry.COLUMN_NAME_DATE, a.date);
			values.put(ArticleEntry.COLUMN_NAME_DESCRIPTION, a.description);
			values.put(ArticleEntry.COLUMN_NAME_IMAGE, a.image);
			values.put(ArticleEntry.COLUMN_NAME_FAVICON, a.favicon);
			values.put(ArticleEntry.COLUMN_NAME_CATEGORIES, a.categories);
			values.put(ArticleEntry.COLUMN_NAME_AUTHOR, a.author);
			values.put(ArticleEntry.COLUMN_NAME_SETS, a.set);
			values.put(ArticleEntry.COLUMN_NAME_RANK, a.rank);
			values.put(ArticleEntry.COLUMN_NAME_SAVE, a.save);
			writeDB.insert(ArticleEntry.TABLE_NAME, null, values);
		}
	}
	
	public void deleteArticles(String list) {
		writeDB.execSQL(SQL_DELETE_ARTICLES + list + ");");
	}
	
	public ArrayList<String> readTitles(String selection) {
		ArrayList<String> titles = new ArrayList<String>();
		
		String[] value = {"0"};
		Cursor c = readDB.rawQuery("select * from " + ArticleEntry.TABLE_NAME + " where rank = ?", value);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			titles.add(c.getString(1));
			Log.d("add",c.getString(1));
			c.moveToNext();
		}
		c.close();
		
		return titles;
	}
	
	public Article readArticle(String[] title) {
		Article article = null;

		Cursor c = readDB.rawQuery("select * from " + ArticleEntry.TABLE_NAME + " where title = ? ", title);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			article = cursorToArticle(c);
			c.moveToNext();
		}
		c.close();

		return article;
	}

	private Article cursorToArticle(Cursor c) {
		Log.d("Database", "Title " + c.getString(1)); // Title
		Log.d("Database", "Link " + c.getString(2)); //Link
		Log.d("Database", "Published " + c.getString(3)); //Date Published
		Log.d("Database", "Content " + c.getString(3)); //Content
		Log.d("Database", "Cover Image " + c.getString(4)); //Cover Image URI
		//c.getInt(0);    //Set
		//c.getInt(1);    //Rank
		//c.getInt(2);    //Save
		//String title, String description, String link, String date, String author, String image, String categories, String favicon
		
		return new Article(c.getString(1), c.getString(2), c.getString(3), c.getString(4), c.getString(5), c.getString(6), c.getString(7), c.getString(8),
				c.getInt(1), c.getInt(2), c.getInt(3));
	}

	public void arrangeSets(ArrayList<Article> unsorted) {
		//Sorting
		//Sorting
		//Sorting
		//Sorting
		//Sorting
		//Sorting
	}

	public void rankSet(ArrayList<Article> set) {
		int rank = 0;

		//Ranking

		for (Article a : set) {
			//Fetch likes of each article
			//Fetch retweets of each article
			//Get number of Twitter followers of source
			//Get length of source
			a.rank = rank;
			rank++;
		}
	}
	
    public static abstract class ArticleEntry implements BaseColumns {

		public static final String COLUMN_NAME_AUTHOR = "author";
		public static final String TABLE_NAME = "articles";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_NAME_LINK = "link";
        public static final String COLUMN_NAME_DATE = "date";
        public static final String COLUMN_NAME_DESCRIPTION = "description";
        public static final String COLUMN_NAME_IMAGE = "image"; //URL to cover image stored in FS
        public static final String COLUMN_NAME_SETS = "sets";
		public static final String COLUMN_NAME_FAVICON = "favicon";
		public static final String COLUMN_NAME_CATEGORIES = "categories";
        public static final String COLUMN_NAME_RANK = "rank";
        public static final String COLUMN_NAME_SAVE = "save";
    }
    
    public static abstract class AuthEntry implements BaseColumns {
    	public static final String COLUMN_NAME_REFRESH = "refresh";
		public static final String TABLE_NAME = "authentication";
    }

	public void writeToken(String refresh) {
		ContentValues values = new ContentValues();
		values.put(AuthEntry.COLUMN_NAME_REFRESH, refresh);
		writeDB.insert(AuthEntry.TABLE_NAME, null, values);
	}
	
	public String readToken() {
		String token = null;

		Cursor c = readDB.rawQuery("select * from " + AuthEntry.TABLE_NAME, null);
		c.moveToFirst();
		while (!c.isAfterLast()) {
			token = c.getString(1);
			c.moveToNext();
		}
		c.close();

		return token;
	}
}