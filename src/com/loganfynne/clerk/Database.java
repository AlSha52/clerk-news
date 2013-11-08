package com.loganfynne.clerk;

import java.util.Collection;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.ArrayAdapter;

public class Database extends AsyncTask<String, Void, Collection<String>> {
	Context context;
	JSONObject articles = null;
	ArrayAdapter<String> adapter = null;
	
    public Database(Context mContext, JSONObject mArticles, ArrayAdapter<String> mAdapter) {
    	context = mContext;
    	articles = mArticles;
    	adapter = mAdapter;
    }
    
    public static abstract class SetsEntry implements BaseColumns {
        public static final String TABLE_NAME = "sets";
        public static final String COLUMN_NAME_TAGS = "tags";
    }
    
    @Override
    protected Collection<String> doInBackground(String... arg0) {
    	DatabaseHelper DbHelper = DatabaseHelper.getInstance(context);
    	
    	if (articles != null) {
            DbHelper.writeArticles(articles);
            Log.d("Database","Wrote Articles!");
    	}
    	
        Collection<String> titles = DbHelper.readTitles();
        
        for (String t : titles) {
        	Log.d("titles", t);
        }
    	
    	return titles;
    }
    
	@Override
	protected void onPostExecute(Collection<String> titles) {
		
		Log.d("Database","onPostExecute");
		adapter.clear();
		
		adapter.addAll(titles);
		
		adapter.notifyDataSetChanged();
	}
}