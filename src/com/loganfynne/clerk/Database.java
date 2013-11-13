package com.loganfynne.clerk;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class Database extends AsyncTask<String, Void, Collection<Article>> {
	Context context;
	ArrayList<JSONObject> articles = null;
	FeedAdapter adapter = null;
	
    public Database(Context mContext, ArrayList<JSONObject> mArticles, FeedAdapter mAdapter) {
    	context = mContext;
    	articles = mArticles;
    	adapter = mAdapter;
    }
    
    @Override
    protected Collection<Article> doInBackground(String... arg0) {
    	DatabaseHelper DbHelper = DatabaseHelper.getInstance(context);
    	
    	if (articles != null) {
            DbHelper.writeArticles(articles);
            Log.d("Database","Wrote Articles!");
    	}
    	
        ArrayList<Article> result = DbHelper.readArticles();
        Log.d("Size", Integer.toString(result.size()));
    	
    	return result;
    }
    
	@Override
	protected void onPostExecute(Collection<Article> result) {
		adapter.clear();
		adapter.addAll(result);
		adapter.notifyDataSetChanged();
	}
}