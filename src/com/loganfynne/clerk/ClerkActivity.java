package com.loganfynne.clerk;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
/*import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;*/
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ClerkActivity extends Activity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;

	private String[] mNavigationTitles;
	
	/*boolean connected = false;
	
	BroadcastReceiver receiver = new connectionReceiver();
	
	private class connectionReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {

			ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		}
	};*/

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

		mNavigationTitles = getResources().getStringArray(R.array.navigation_array);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// set up the drawer's list view with items and click listener
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mNavigationTitles));
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		if (savedInstanceState == null) {
			selectItem(0);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		//this.unregisterReceiver(receiver);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	/* The click listener for ListView in the navigation drawer */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	private void selectItem(int position) {

		// update the main content by replacing fragments
		Fragment fragment = new FeedsFragment();
		Bundle args = new Bundle();

		args.putInt(SubjectsFragment.ARG_DRAWER_NUMBER, position);
		fragment.setArguments(args);

		FragmentManager fragmentManager = getFragmentManager();
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();

		// update selected item and title, then close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mNavigationTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}
}