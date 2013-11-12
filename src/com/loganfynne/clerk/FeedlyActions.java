package com.loganfynne.clerk;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class FeedlyActions {

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

	public static class getProfile extends AsyncTask<String, Void, JSONObject> {
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
			}
		}
	}
	
	public static class getCategories extends AsyncTask<String, Void, JSONArray> {
		String url;
		String access;

		public getCategories (String mUrl, String mAccess) {
			url = mUrl;
			access = mAccess;
		}

		protected JSONArray doInBackground(String... urls) {
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url + "/v3/categories");
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
					JSONArray result = new JSONArray(jsonstring);
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

		protected void onPostExecute(JSONArray result) {
			if (result != null) {
			}
		}
	}
	
	public static class getStream extends AsyncTask<String, Void, JSONObject> {
		String url;
		String access;
		String id;
		Context context;
		FeedAdapter adapter;

		public getStream (String mUrl, String mAccess, String mId, Context mContext, FeedAdapter mAdapter) {
			url = mUrl;
			access = mAccess;
			id = mId;
			context = mContext;
			adapter = mAdapter;
		}

		protected JSONObject doInBackground(String... urls) {
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url + "/v3/streams/contents?streamId=" + id);
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
				new Database(context, result, adapter).execute();
			}
		}
	}
	
	
	public static class getSubscriptions extends AsyncTask<String, Void, JSONArray> {
		String url;
		String access;

		public getSubscriptions (String mUrl, String mAccess) {
			url = mUrl;
			access = mAccess;
		}

		protected JSONArray doInBackground(String... urls) {
			
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet httpget = new HttpGet(url + "/v3/subscriptions");
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
					JSONArray result = new JSONArray(jsonstring);
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
		
		protected void onPostExecute(JSONArray result) {
			if (result != null) {
				//JSONObject j = null;
				//String id;
				//Long timestamp;
				
				for (int i = 0; i < result.length(); i++) {
					//try {
						//j = result.getJSONObject(i);
						//id = j.getString("id");
						//timestamp = j.getLong("updated");
					//} catch (JSONException e) {}
				}
			}
		}
	}
	
	
	public static class postCategory extends AsyncTask<String, Void, JSONObject> {
		String url;
		String access;
		String label;

		public postCategory (String mUrl, String mAccess, String mLabel) {
			url = mUrl;
			access = mAccess;
			label = mLabel;
		}
		
		protected JSONObject doInBackground(String... urls) {
			JSONObject response = null;
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url +"/v3/subscriptions");
			httppost.setHeader("Authorization", access);

			try {
				String jsonData = "{\"\"label\": \"" + label + "\"}";
				StringEntity stringentity = new StringEntity(jsonData);
				httppost.setEntity(stringentity);

				HttpResponse responseBody = httpclient.execute(httppost);
				HttpEntity entity = responseBody.getEntity();
				InputStream is = entity.getContent();
				response = new JSONObject(convertStreamToString(is));
				Log.d("subscribe", response.toString());

			} catch (ClientProtocolException e) {} catch (IOException e) {} catch (JSONException e) {}

			return response;
		}

		protected void onPostExecute(JSONObject result) {
			if (result != null) {
			}
		}
	}
	
	public static class addSubscription extends AsyncTask<String, Void, JSONObject> {
		String url;
		String access;
		String userid;

		public addSubscription (String mUrl, String mAccess, String mUserId) {
			url = mUrl;
			access = mAccess;
			userid = mUserId;
		}
		
		protected JSONObject doInBackground(String... urls) {
			JSONObject response = null;
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(url +"/v3/subscriptions");
			httppost.setHeader("Authorization", access);

			try {
				// Add your data
				String jsonData = "{\"id\": \"feed/https://medium.com/feed/@kylry/\",\"title\": \"Kyle Ryan on Medium\",\"categories\": [{" +
						"\"id\": \"user/" + userid + "/category/design\"," +
						"\"label\": \"design\"}]}";
				StringEntity stringentity = new StringEntity(jsonData);
				httppost.setEntity(stringentity);

				// Execute HTTP Post Request
				//ResponseHandler<String> responseHandler = new BasicResponseHandler();
				HttpResponse responseBody = httpclient.execute(httppost); //, responseHandler);
				HttpEntity entity = responseBody.getEntity();
				InputStream is = entity.getContent();
				response = new JSONObject(convertStreamToString(is));
				Log.d("subscribe", response.toString());

			} catch (ClientProtocolException e) {} catch (IOException e) {} catch (JSONException e) {}

			return response;
		}

		protected void onPostExecute(JSONObject result) {
			if (result != null) {
			}
		}
	}
}
