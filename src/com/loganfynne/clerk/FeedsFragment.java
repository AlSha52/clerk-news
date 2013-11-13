package com.loganfynne.clerk;

import java.util.ArrayList;

import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

	private static FeedAdapter adapter = null;

	public FeedsFragment() {
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getListView().setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Toast.makeText(getActivity(), "On long click listener", Toast.LENGTH_SHORT).show();
				return false;
			}
		});

		getListView().setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Intent toArticle = new Intent(view.getContext(), ArticleActivity.class);

				Article selected = (Article) (getListView().getItemAtPosition(position));

				Bundle extras = new Bundle();
				extras.putString("title", selected.title);
				extras.putString("author", selected.author);
				extras.putString("content", selected.content);
				extras.putInt("published", selected.published);
				
				toArticle.putExtras(extras);

				startActivity(toArticle);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		adapter = new FeedAdapter(inflater.getContext(), articles);
		setListAdapter(adapter);
		
		Bundle bundle = this.getArguments();
		String access = bundle.getString("access");
		String url = bundle.getString("url");
		
		Log.d("Clerk","feedfragment" + access);

		new FeedlyActions.getSubscriptions(Clerk.getInstance(), url, access, adapter).execute();
		
		View view = inflater.inflate(R.layout.list, container, false);
		return view;
	}
}