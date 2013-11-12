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

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.d("Clerk", "onStartCommand");
		access = intent.getStringExtra("access");
		refresh = intent.getStringExtra("refresh");
		if (access != null && access != "" && refresh != null && refresh != "") {
			new getProfile(refresh, access).execute();
		}
		if (user != null) {
			Log.d("User", "user " + user);
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
			Log.d("Clerk","Service gotten");
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
				Log.d("Refresh", "Wrote refresh!");
			} else if (this.mRefresh == null && this.mUserId == null) {
				Log.d("Refresh","Get token!");
				token = DbHelper.readToken();
				if (token[0] != null && token[0] != "" && token[1] != null && token[1] != "") {
					finished = true;
				}
				Log.d("Token", "Tokens" + token[0] + token[1]);
				return token;
			} else {
				Log.d("Clerk","One is null, other is not.");
			}

			return null;
		}

		@Override
		protected void onPostExecute(String[] token) {
			Log.d("Token","Token");
			if (finished == false) {
				Log.d("Token","finished not false");
				if (token[0] != "" && token[1] != "" && access != null && access != "") {
					Log.d("Token","token[0] is also not null");
					Log.d("Token", token[0]);
					Log.d("Token", token[1]);
					user = token[1];
					new postToken(refresh).execute();
				} else {
					Log.d("Token","Called webview");
					Intent intent = new Intent("com.loganfynne.clerk.AuthCall");
					me.sendBroadcast(intent);
				}
			} else {
				Log.d("Finished","Sending to Auth Finished!");
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
			if (response != null) {
				String access_token = null;
				try {
					access_token = response.getString("access_token");
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
				Log.d("HTTP","About to execute request");
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
			Log.d("Trying","result");
			if (result != null) {
				try {
					user = result.getString("id");
					Log.d("Refresh","Refresh" + refresh);
					new authTask(getBaseContext(), refresh, user).execute();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
	}
}