package com.loganfynne.clerk;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.aphidmobile.flip.FlipViewController;


import android.widget.ListView;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Toast;
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
	
	DrawerLayout drawerLayout;
	ListView drawerList;
	private String[] drawerTitles;
	private ActionBarDrawerToggle drawerToggle;
	DrawerItemClickListener drawerListener;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.article_view);
		
		drawerTitles = getResources().getStringArray(R.array.drawer_titles);
		drawerLayout = (DrawerLayout) findViewById(R.id.drawer_article);
		drawerList = (ListView) findViewById(R.id.left_drawer);
		
		drawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, drawerTitles));
		
		drawerListener = new DrawerItemClickListener();
		drawerList.setOnItemClickListener(drawerListener);
		drawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                R.drawable.ic_drawer, 
                R.string.drawer_open,
                R.string.drawer_close 
                ) {
        };

        // Set the drawer toggle as the DrawerListener
        drawerLayout.setDrawerListener(drawerToggle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
		
		ActionBar actionBar = getActionBar();
		actionBar.show();
		

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = drawerLayout.isDrawerOpen(drawerList);
        menu.findItem(R.id.action_bookmark).setVisible(!drawerOpen);
        menu.findItem(R.id.action_donate).setVisible(!drawerOpen);
        menu.findItem(R.id.action_share).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// The action bar home/up action should open or close the drawer.
    	// ActionBarDrawerToggle will take care of this.
    	if (drawerToggle.onOptionsItemSelected(item)) {
    		return true;
    	}
    	// Handle action buttons
    	switch(item.getItemId()) {
    	case R.id.action_donate:
    		Toast.makeText(this, "Donated", Toast.LENGTH_SHORT).show();
    	case R.id.action_share:
    		Toast.makeText(this, "Shared", Toast.LENGTH_SHORT).show();
    	case R.id.action_bookmark:
    		Toast.makeText(this, "Bookmarked", Toast.LENGTH_SHORT).show();
    	}
    	
    	return true;
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    private void selectItem(int position) {
        /*Fragment fragment = new PlanetFragment();
        Bundle args = new Bundle();
        args.putInt(PlanetFragment.ARG_PLANET_NUMBER, position);
        fragment.setArguments(args);

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();*/

        // update selected item and title, then close the drawer
    	if (position == 0) {
    		finish();
    	}
        //drawerList.setItemChecked(position, true);
        //setTitle(drawerTitles[position]);
        drawerLayout.closeDrawer(drawerList);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToggle.onConfigurationChanged(newConfig);
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
			
			String regex = "[0-9]+";
			String imageW = null;
			Elements image = doc.select("img[src]");
			image.attr("style","");
			for (int x = 1; x < image.size(); x++) {
				imageW = image.get(x).attr("width").replace(regex,"");
				Log.d("Article",imageW);
				if (image.get(x).attr("width") != "") { 
					Log.d("Article","Width has value");
					/*if (Integer.parseInt(imageW) > width) {
						Log.d("Article","Set width!");
						image.get(x).attr("style",image.attr("style") + "width: 90% !important;height: auto !important;margin-right:auto;margin-left:auto;");
					}*/
				}
			}
			
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