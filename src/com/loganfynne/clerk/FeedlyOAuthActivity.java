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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class FeedlyOAuthActivity extends Activity {

	public static String clientId = "sandbox";
	public static String clientSecret = "Z5ZSFRASVWCV3EFATRUY";
	public static String redirectUri = "urn:ietf:wg:oauth:2.0:oob";
	public static String scope = "https://cloud.feedly.com/subscriptions";
	public static String code = null;
	public static String refreshToken = null;
	public static String accessToken = null;
	static WebView webview;

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

	private class accessTokenProcess extends AsyncTask<String, Void, JSONObject> {
		String code;

		public accessTokenProcess (String mCode) {
			code = mCode;
		}

		protected JSONObject doInBackground(String... urls) {
			JSONObject response = null;
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("https://sandbox.feedly.com/v3/auth/token");

			try {
				// Add your data
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
				nameValuePairs.add(new BasicNameValuePair("code", this.code));
				nameValuePairs.add(new BasicNameValuePair("client_id", clientId));
				nameValuePairs.add(new BasicNameValuePair("client_secret", clientSecret));
				nameValuePairs.add(new BasicNameValuePair("redirect_uri", redirectUri));
				nameValuePairs.add(new BasicNameValuePair("grant_type", "authorization_code"));
				nameValuePairs.add(new BasicNameValuePair("state", "clerk"));
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

				HttpResponse responseBody = httpclient.execute(httppost);
				HttpEntity entity = responseBody.getEntity();
				InputStream is = entity.getContent();
				response = new JSONObject(convertStreamToString(is));

			} catch (ClientProtocolException e) {} catch (IOException e) {} catch (JSONException e) {}

			return response;
		}

		protected void onPostExecute(JSONObject response) {
			try {
				
				Intent resultIntent = new Intent();
				resultIntent.putExtra("access", response.getString("access_token"));
				resultIntent.putExtra("refresh", response.getString("refresh_token"));
				setResult(Activity.RESULT_OK, resultIntent);
				finish();
			} catch (JSONException e) {}

		}
	}

	@SuppressLint("SetJavaScriptEnabled")
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.auth_view);
		webview = (WebView) findViewById(R.id.authWeb);
		final ProgressBar pBar = (ProgressBar) findViewById(R.id.progress);
		pBar.setVisibility(ProgressBar.VISIBLE);

		webview.getSettings().setJavaScriptEnabled(true);
		this.setProgressBarVisibility(true);

		webview.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				if(progress < 100 && pBar.getVisibility() == ProgressBar.GONE){
					pBar.setVisibility(ProgressBar.VISIBLE);
				}
				pBar.setProgress(progress);
				if(progress == 100) {
					pBar.setVisibility(ProgressBar.GONE);
				}
			}
		});

		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				webview.loadUrl(url);
				return true;
			}

			@Override
			public void onLoadResource(WebView view, String url) {
				if (url.indexOf("urn:ietf:wg:oauth:2.0:oob") == 0) {
					view.setVisibility(View.GONE);
					if (url != null && code == null) {
						url = url.replace(redirectUri,"http://getclerk.com/");
						Uri uri = Uri.parse(url);
						code = uri.getQueryParameter("code");
						new accessTokenProcess(code).execute();
					}
				}
			}
		});

		webview.loadUrl("http://sandbox.feedly.com/v3/auth/auth?client_id=" + clientId + "&redirect_uri=" + redirectUri + 
				"&response_type=code" + "&scope=" + scope);
	}
}