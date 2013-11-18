package com.loganfynne.clerk;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FeedsFragment extends ListFragment {
	DatabaseHelper dh = Clerk.getDatabase();
	//private ArrayList<String> url = dh.readSources();
	private ArrayList<Article> articles = new ArrayList<Article>();
	Context context;
	String access;
	String url;
	String userid;
	boolean longtouch = false;

	private static FeedAdapter adapter = null;

	public FeedsFragment() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int position, long id) {
				Article selected = (Article) (getListView().getItemAtPosition(position));
				
				new FeedlyActions.postMarkers(url, access, selected.entryid);
				
				Toast.makeText(getActivity(), "Marked as read", Toast.LENGTH_SHORT).show();
				longtouch = true;
				
				return false;
			}
		});

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (!longtouch) {
					Intent toArticle = new Intent(view.getContext(), ArticleActivity.class);

					Article selected = (Article) (getListView().getItemAtPosition(position));

					Bundle extras = new Bundle();
					extras.putString("title", selected.title);
					extras.putString("author", selected.author);
					extras.putString("content", selected.content);
					extras.putString("entryid", selected.entryid);
					extras.putInt("published", selected.published);

					toArticle.putExtras(extras);

					startActivity(toArticle);

					new FeedlyActions.postMarkers(url, access, selected.entryid).execute();
				} else {
					longtouch = false;
				}
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		adapter = new FeedAdapter(inflater.getContext(), articles);
		setListAdapter(adapter);
		
		Bundle bundle = this.getArguments();
		access = bundle.getString("access");
		userid = bundle.getString("userid");
		url = bundle.getString("url");

		new FeedlyActions.getSubscriptions(Clerk.getInstance(), url, access, userid, adapter).execute();
		
		View view = inflater.inflate(R.layout.list, container, false);
		return view;
	}
}