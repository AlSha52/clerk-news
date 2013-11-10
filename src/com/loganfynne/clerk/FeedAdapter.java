package com.loganfynne.clerk;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

class FeedAdapter extends ArrayAdapter<Article> {
	Context context;
	ArrayList<Article> items;
	String header;

	public FeedAdapter(Context context, ArrayList<Article> items) {
		super(context, R.layout.listitem, items);
		this.context = context;
		this.items = items;
	}
	
	public Article getItem(int position) {
	    return items.get(position);
	}
	
	public int getCount() {
	    return items.size();
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {
		View view = convertView;
	    
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (view == null) {
			view = inflater.inflate(R.layout.listitem, parent, false);
			//int height = 50;
			//view.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,height));
		}

		Article a = getItem(position);
		//Bitmap bm = null;

		Log.d("Items", a.title + a.author + a.published);

		//((ImageView) view.findViewById(R.id.cover)).setImageBitmap(bm);
		((TextView) view.findViewById(R.id.title)).setText(a.title);
		//((TextView) view.findViewById(R.id.second)).setText(a.author + " " + a.published);


		return view;
	}
}