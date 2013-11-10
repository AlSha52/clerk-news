package com.loganfynne.clerk;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;

public class Database extends AsyncTask<String, Void, Collection<Article>> {
	Context context;
	JSONObject articles = null;
	FeedAdapter adapter = null;
	
    public Database(Context mContext, JSONObject mArticles, FeedAdapter mAdapter) {
    	context = mContext;
    	articles = mArticles;
    	adapter = mAdapter;
    }
    
    public static abstract class SetsEntry implements BaseColumns {
        public static final String TABLE_NAME = "sets";
        public static final String COLUMN_NAME_TAGS = "tags";
    }
    
    @Override
    protected Collection<Article> doInBackground(String... arg0) {
    	DatabaseHelper DbHelper = DatabaseHelper.getInstance(context);
    	
    	if (articles != null) {
            DbHelper.writeArticles(articles);
            Log.d("Database","Wrote Articles!");
    	}
    	
        ArrayList<Article> articl = DbHelper.readArticles();
        Log.d("Size", Integer.toString(articl.size()));
    	
    	return articl;
    }
    
	@Override
	protected void onPostExecute(Collection<Article> articl) {
		adapter.clear();
		adapter.addAll(articl);
		adapter.notifyDataSetChanged();
	}
}