package com.loganfynne.clerk;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class AuthDatabase extends AsyncTask<String, Void, String> {
	String selection = "rank";
	Context context;
	String refresh = null;
	
    public AuthDatabase(Context mContext, String mRefresh) {
    	context = mContext;
    	refresh = mRefresh;
    }
    
    @Override
    protected String doInBackground(String... arg0) {
    	String token = null;
    	DatabaseHelper DbHelper = DatabaseHelper.getInstance(context);
    	
    	if (refresh != null) {
    		DbHelper.writeToken(refresh);
    		Log.d("Refresh", "Wrote refresh!");
    	} else {
    		token = DbHelper.readToken();
    	}
    	
    	return token;
    }
    
	@Override
	protected void onPostExecute(String token) {
		Log.d("Database","onPostExecute");
		//TODO Have refresh token now, call Feedly to get Access token, then set it in Clerk object.
	}
}