package com.loganfynne.clerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("AuthDatabase", "onStartCommand");
		new authTask(this, null, null).execute();
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
		refresh = mRefresh;
		access = mAccess;
		new getProfile("http://sandbox.feedly.com", access).execute();
	}

	public class authTask extends AsyncTask<String, Void, String[]> {
		Context context;
		String refresh = null;
		String userid = null;

		public authTask(Context mContext, String mRefresh, String mUserId) {
			context = mContext;
			refresh = mRefresh;
			userid = mUserId;
		}

		@Override
		protected String[] doInBackground(String... arg0) {
			String[] token = null;
			DatabaseHelper DbHelper = DatabaseHelper.getInstance(context);

			if (refresh != null && userid != null) {
				DbHelper.writeToken(refresh, userid);
				Log.d("Refresh", "Wrote refresh!");
			} else {
				token = DbHelper.readToken();
			}

			return token;
		}

		@Override
		protected void onPostExecute(String[] token) {
			Log.d("Token","Token");
			if (token != null) {
				if (token[0] != null && token[1] != null) {
					Log.d("Token",token[0]);
					new postToken(refresh).execute();
					user = token[1];
				} else {
					//TODO send intent to open web view
				}
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
		String refresh;
		public String clientId = "sandbox";
		public String clientSecret = "Z5ZSFRASVWCV3EFATRUY";

		public postToken (String mRefresh) {
			refresh = mRefresh;
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
				nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse responseBody = httpclient.execute(httppost);
				HttpEntity entity = responseBody.getEntity();
				InputStream is = entity.getContent();
				response = new JSONObject(convertStreamToString(is));

			} catch (ClientProtocolException e) {} catch (IOException e) {} catch (JSONException e) {}

			return response;
		}

		protected void onPostExecute(JSONObject result) {
			if (result != null) {
				String access_token = null;
				try {
					access_token = result.getString("access_token");
					access = access_token;
				} catch (JSONException e) {}
			}
		}
	}
	
	public class getProfile extends AsyncTask<String, Void, JSONObject> {
		String url;
		String access;

		public getProfile (String mUrl, String mAccess) {
			url = mUrl;
			access = mAccess;
		}

		protected JSONObject doInBackground(String... urls) {
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url + "/v3/profile");
			httpget.setHeader("Authorization", access);
			
			HttpResponse response;
			try {
				response = httpclient.execute(httpget);
				Log.d("HTTP", response.getStatusLine().toString());
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					String jsonstring = convertStreamToString(instream);
					Log.d("json", jsonstring);
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
				String id;
				try {
					if (user == null) {
						id = result.getString("id");
						new authTask(getBaseContext(), refresh, id).execute();
					} else {
						//TODO: send values back to ClerkActivity
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
}