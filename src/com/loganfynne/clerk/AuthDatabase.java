package com.loganfynne.clerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Binder;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

public class AuthDatabase extends Service {
	private final IBinder mBinder = new authBinder();
	Context me = this;
	static String refresh = null;
	String access = null;
	String user = null;
	boolean finished = false;
	boolean post = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		access = intent.getStringExtra("access");
		refresh = intent.getStringExtra("refresh");
		if (access != null && access != "" && refresh != null && refresh != "") {
			new getProfile(refresh, access).execute();
		}
		
		new authTask(this, refresh, user).execute();
		return Service.START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
	
	public class authBinder extends Binder {
		AuthDatabase getService() {
			return AuthDatabase.this;
	    }
	}
	
	public String getAccess() {
		return access;
	}
	
	public String getUser() {
		return user;
	}
	
	public void setRefresh(String mRefresh, String mAccess) {
		new getProfile(mRefresh, mAccess).execute();
	}

	public class authTask extends AsyncTask<String, Void, String[]> {
		Context context;
		String mRefresh;
		String mUserId;

		public authTask(Context mContext, String mRefresh, String mUserId) {
			this.mRefresh = mRefresh;
			this.mUserId = mUserId;
			
			context = mContext;
		}

		@Override
		protected String[] doInBackground(String... arg0) {
			String[] token = null;
			DatabaseHelper DbHelper = DatabaseHelper.getInstance(context);

			if (this.mRefresh != null && this.mUserId != null) {
				DbHelper.writeToken(this.mRefresh, this.mUserId);
				finished = true;
			} else if (this.mRefresh == null && this.mUserId == null) {
				token = DbHelper.readToken();
				Log.d("Clerk","Got out of dbhelper");
				Log.d("Clerk","token0 = " + token[0]);
				Log.d("Clerk","token1 = " + token[1]);
				if (token[0] != null && token[0] != "" && token[1] != null && token[1] != "") {
					Log.d("Clerk","post is true");
					post = true;
				}
				return token;
			}

			return null;
		}

		@Override
		protected void onPostExecute(String[] token) {
			if (finished == false) {
				if (post) {
					Log.d("Clerk","About to post token");
					refresh = token[0];
					user = token[1];
					new postToken().execute();
				} else {
					Intent intent = new Intent("com.loganfynne.clerk.AuthCall");
					me.sendBroadcast(intent);
				}
			} else {
				Intent intent = new Intent("com.loganfynne.clerk.AuthFinished");
				intent.putExtra("access", access);
				intent.putExtra("userid", user);
				me.sendBroadcast(intent);
				((Service) me).stopSelf();
			}
		}
	}


	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}


	public class postToken extends AsyncTask<String, Void, JSONObject> {
		public String clientId = "sandbox";
		public String clientSecret = "Z5ZSFRASVWCV3EFATRUY";

		public postToken () {
		}

		protected JSONObject doInBackground(String... urls) {
			JSONObject response = null;
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://sandbox.feedly.com/v3/auth/token");

			try {
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
				nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
				nameValuePairs.add(new BasicNameValuePair("refresh_token", refresh));
				nameValuePairs.add(new BasicNameValuePair("grant_type", "refresh_token"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse responseBody = httpclient.execute(httppost);
				HttpEntity entity = responseBody.getEntity();
				InputStream is = entity.getContent();
				String json = convertStreamToString(is);
				Log.d("Clerk", json);
				response = new JSONObject(json);
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return response;
		}

		protected void onPostExecute(JSONObject response) {
			Log.d("Clerk","Inside postexecute");
			if (response != null) {
				String access_token = null;
				try {
					access_token = response.getString("access_token");
					Log.d("post","access" + access_token);
					access = access_token;
					Intent intent = new Intent("com.loganfynne.clerk.AuthFinished");
					intent.putExtra("access", access);
					intent.putExtra("userid", user);
					me.sendBroadcast(intent);
					((Service) me).stopSelf();
				} catch (JSONException e) {}
			}
		}
	}
	
	public class getProfile extends AsyncTask<String, Void, JSONObject> {

		public getProfile (String mRefresh, String mAccess) {
			refresh = mRefresh;
			access = mAccess;
		}

		protected JSONObject doInBackground(String... urls) {
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet("http://sandbox.feedly.com/v3/profile");
			httpget.setHeader("Authorization", access);

			HttpResponse response;
			try {
				response = httpclient.execute(httpget);
				Log.d("HTTP", response.getStatusLine().toString());
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					String jsonstring = convertStreamToString(instream);
					JSONObject result = new JSONObject(jsonstring);
					return result;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				try {
					user = result.getString("id");
					new FeedlyActions.addSubscription("http://sandbox.feedly.com", access, user, true).execute();
					new FeedlyActions.addSubscription("http://sandbox.feedly.com", access, user, false).execute();
					new authTask(getBaseContext(), refresh, user).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
}