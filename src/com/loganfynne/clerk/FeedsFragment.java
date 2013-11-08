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
import android.widget.ArrayAdapter;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class FeedsFragment extends ListFragment {
	public static final String ARG_DRAWER_NUMBER = "drawer_number";
	//private String[] url = {"http://www.engadget.com/rss.xml", "http://www.theverge.com/rss/frontpage", "http://planet.mozilla.org/atom.xml"};
	DatabaseHelper dh = Clerk.getDatabase();
	//private ArrayList<String> url = dh.readSources();
	private ArrayList<String> titles = new ArrayList<String>();
	Context context;

	private static ArrayAdapter<String> adapter = null;

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

				String selected = (String) (getListView().getItemAtPosition(position));

				toArticle.putExtra("title", selected);

				startActivity(toArticle);
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		adapter = new ArrayAdapter<String>(inflater.getContext(), android.R.layout.simple_list_item_1, titles);
		Bundle bundle = this.getArguments();
		String access = bundle.getString("access");
		String url = bundle.getString("url");
		String id = bundle.getString("id");

		//new Database(context, null, adapter).execute();
		//new xmlParse(Clerk.getInstance(), adapter).execute(url);
		new FeedlyActions.getStream(url, access, id, Clerk.getInstance(), adapter).execute();

		setListAdapter(adapter);

		return super.onCreateView(inflater, container, savedInstanceState);
	}
}