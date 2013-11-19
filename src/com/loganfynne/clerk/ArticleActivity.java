package com.loganfynne.clerk;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aphidmobile.flip.FlipViewController;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
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
	
	static String title = null;
	static String author = null;
	static String content = null;
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
		
		public ArrayList<String> splitStringEvery(String s, int interval) {
		    ArrayList<String> result = new ArrayList<String>();

		    int j;
		    //int a;
		    for (j = 0; j < s.length()-interval; j++) {
		    	if (j == 0) {
		    		result.add(s.substring(j, j + (2*interval)));
		    		j += (2*interval);
		    	} else {
		    		result.add(s.substring(j, j + interval));
		    		j += interval;
		    		/*if (s.substring(j+interval, j+interval-2).equals("/>")) {
		    			Log.d("Subs",s.substring(j+interval, j+interval-2));
		    			result.add(s.substring(j, j + interval));
			    		j += interval;
		    		} else {
		    			
		    		}*/
		    		
		    		/*int i = j+interval-2;
		    		Log.d("string",s.substring(i-1,i));
	    			while (i > j) {
	    				if (s.substring(i-1, i).equals(">")) {
	    					result.add(s.substring(j,i-1));
	    					j += i-j;
	    					i = j;
	    				} else {
	    					i--;
	    				}
	    			}*/
		    		
		    	}
		    }
		    result.add(s.substring(j));

		    return result;
		}

		private articleAdapter(Activity activity, FlipViewController controller, String content) {
			Document doc = Jsoup.parse(content, "UTF-8");
			
			Elements first_image = doc.select("img[src]");
			
			Element titlehead = doc.createElement("h1");
			titlehead.html(ArticleActivity.title);
			titlehead.attr("style","position:absolute; right:10px; top:-5px; height:170px; width:215px; font-size:1.15em;");
			
			if (first_image.size() > 0) {
				first_image.first().attr("style", "float:left; margin:30px 0 0 -1.5%; border-radius:999px; background: url(" + first_image.first().attr("src") + ") center center; width:120px; height:120px;");
				first_image.first().tagName("div");
				first_image.first().after(titlehead);
				titlehead.after("<div style=\"clear:both; margin-top:0px; margin-bottom:30px;\"></div>");
			} else {
				doc.children().first().before(titlehead);
				titlehead.after("<div style=\"clear:both; margin-top:185px; margin-bottom:30px;\"></div>");
			}
			
			doc.select("a[href*=feedburner.com").remove();
			
			content = doc.toString();
			
			Log.d("Content",content);
			
			//Spanned text = Html.fromHtml(content);
			
			/*String first = image + content.substring(0,375);
			content = content.substring(375,content.length());
			content = text.toString();*/
			page = splitStringEvery(content, 700);
			
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
				
				public boolean shouldOverrideUrlLoading(WebView view, String url) {
					if ((url != null && url.startsWith("http")) || (url != null && url.startsWith("ftp"))) {
						view.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
						return true;
					} else {
						return false;
					}
				}
			});

			webView.setWebChromeClient(new WebChromeClient() {
				private int lastRefreshProgress = 0;

				@Override
				public void onProgressChanged(WebView view, int newProgress) {
					if (newProgress - lastRefreshProgress > 20) {
						controller.refreshPage(view);
						lastRefreshProgress = newProgress;
					}
				}
			});
			
			String css =
					"<style>" + 
							"@font-face {font-family: 'Tisa'; src:url('fonts/TisaOT.otf');}" + 
							"@font-face {font-family: 'Gotham'; src:url('fonts/Gotham-Bold.otf');}" +
							"@font-face {font-family: 'GothamItalic'; src:url('fonts/Gotham-BookItalic.otf');}" +
							"*{font-family: Tisa; color:rgb(24,24,24); background-color:rgb(247,247,247);}" + 
							"html, body {width:96%; padding:0 2% 0 2% !important; margin: 0 0 0 0 !important; font-size: 1.05em !important;}" +
							"h1 {font-family:Gotham !important; font-size:1.2em !important; margin-bottom:-18px;}" +
							"h2 {font-family:GothamItalic !important; font-size:1.05em !important}" +
							"img {text-align:center; margin: 0 auto 0 auto !important;}" +
							"</style>";
			
			webView.loadDataWithBaseURL("file:///android_asset/", css + page.get(position), "text/html", "utf-8", null);

			return webView;
		}
	}
}