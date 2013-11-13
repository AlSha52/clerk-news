package com.loganfynne.clerk;

import java.util.ArrayList;

import com.aphidmobile.flip.FlipViewController;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.BaseAdapter;
import android.graphics.Bitmap;

public class ArticleActivity extends Activity {
	//DatabaseHelper db = Clerk.getDatabase();
	private FlipViewController flipView;
	DatabaseHelper dh = DatabaseHelper.getInstance(Clerk.getInstance());
	
	String title = null;
	String author = null;
	String content = null;
	String entryid = null;
	int published = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		flipView = new FlipViewController(this, FlipViewController.VERTICAL);

		Intent i = getIntent();
		title = i.getStringExtra("title");
		author = i.getStringExtra("author");
		content = i.getStringExtra("content");
		entryid = i.getStringExtra("entryid");
		
		articleAdapter articleadapt = new articleAdapter(this, flipView, content);

		flipView.setAdapter(articleadapt);

		setContentView(flipView);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		dh.deleteArticle(entryid);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		flipView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		flipView.onPause();
		
		dh.deleteArticle(entryid);
	}

	private static class articleAdapter extends BaseAdapter {

		ArrayList<String> page = new ArrayList<String>();
		FlipViewController controller;
		Activity activity;
		int activeLoadingCount = 0;

		private articleAdapter(Activity activity, FlipViewController controller, String content) {
			page.add(content);
			page.add(content);
			
			this.activity = activity;
			this.controller = controller;
		}

		@Override
		public int getCount() {
			return page.size();
		}

		@Override
		public Object getItem(int position) {
			return page.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			WebView webView = new WebView(controller.getContext());
			webView.setHorizontalScrollBarEnabled(false);
			webView.setVerticalScrollBarEnabled(false);
			webView.setPadding(0, 0, 0, 0);
			
			webView.setWebViewClient(new WebViewClient() {
				@Override
				public void onPageStarted(WebView view, String url, Bitmap favicon) {
					activity.setProgressBarIndeterminateVisibility(true);
					activeLoadingCount++;
				}

				@Override
				public void onPageFinished(WebView view, String url) {
					controller.refreshPage(view);
					//This works as the webView is the view for a page. Please use refreshPage(int pageIndex) if the webview is only a part of page view.

					activeLoadingCount--;
					activity.setProgressBarIndeterminateVisibility(activeLoadingCount == 0);
				}
			});

			webView.setWebChromeClient(new WebChromeClient() {
				private int lastRefreshProgress = 0;

				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					if (newProgress - lastRefreshProgress > 20) { //limit the invocation frequency of refreshPage
						controller.refreshPage(view);
						lastRefreshProgress = newProgress;
					}
				}
			});
			
			String css = "<style>@font-face {font-family: 'MyCustomFont';src: url('/assets/fonts/MaycustomFont.ttf') }; * {font-family: 'Custom';}" + 
					"html, body { font-family: ''; width:98%; padding:0 1% 0 1% !important; margin: 0 0 0 0 !important; }" +
					"</style>";
			
			webView.loadDataWithBaseURL("file:///android_asset/", css + page.get(position), "text/html", "utf-8", null);

			return webView;
		}
	}
}