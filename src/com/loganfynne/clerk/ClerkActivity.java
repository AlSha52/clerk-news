package com.loganfynne.clerk;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
/*import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;*/
import android.os.Bundle;
import android.util.Log;

public class ClerkActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		//TODO SQL statement to check if refresh token exists
		Intent intent = new Intent(this, FeedlyOAuthActivity.class);
		startActivityForResult(intent, 0);
		
		//this.registerReceiver(receiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("result", "Received Refresh: " + data.getStringExtra("refresh"));
		Log.d("result", "Received Access: " + data.getStringExtra("access"));
		Fragment fragment = new FeedsFragment();
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
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
}