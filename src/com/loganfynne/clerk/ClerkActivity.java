package com.loganfynne.clerk;

//import com.loganfynne.clerk.AuthDatabase.OAuthStarter;
import com.loganfynne.clerk.AuthDatabase.authBinder;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class ClerkActivity extends Activity { //implements OAuthStarter {
	String access = null;
	String refresh = null;
	String userId = null;
	String feedId = null;
	String url = "http://sandbox.feedly.com";
	AuthDatabase mService;
	boolean mBound = false;
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	if (intent.getStringExtra("") != null) {
        		
        	}
        	//updateUI(intent);       
        }
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d("ClerkActivity","onCreate");
		registerReceiver(broadcastReceiver, new IntentFilter("com.loganfynne.clerk.AuthCall"));
		//startOAuth();
	}
	
	@Override
    protected void onStart() {
        super.onStart();
        Log.d("ClerkActivity","onStart");
        Intent servei = new Intent(this, AuthDatabase.class);
		bindService(servei, mConnection, Context.BIND_AUTO_CREATE);
    }
	
	private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            authBinder binder = (authBinder) service;
            mService = binder.getService();
            mBound = true;
            //mService.setServiceClient(ClerkActivity.this);
            //onAccessToken(mService.getAccess(),mService.getUser());
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        	//mService.setServiceClient(null);
            mBound = false;
        }
    };
    
    public void startOAuth() {
    	Intent feedi = new Intent(this, FeedlyOAuthActivity.class);
		startActivityForResult(feedi, 0);
    }
	
	public void onAccessToken(String access, String userid) {
		new FeedlyActions.getProfile(url, access).execute();
		new FeedlyActions.addSubscription(url, access, userid).execute();
		new FeedlyActions.getSubscriptions(url, access).execute();
		//new FeedlyActions.getCategories(url, access).execute();
		
		Fragment fragment = new FeedsFragment();
		Bundle bundle = new Bundle();
		bundle.putString("access", access);
		bundle.putString("url", url);
		bundle.putString("userid", userid);
		fragment.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		access = data.getStringExtra("access");
		Log.d("access", access);
		mService.setRefresh(data.getStringExtra("refresh"), data.getStringExtra("access"));
		if (access != null) {
			onAccessToken(access,null);
		} else {
			Intent intent = new Intent(this, FeedlyOAuthActivity.class);
			startActivityForResult(intent, 0);
		}
	}

	@Override
	protected void onStop() {
        super.onStop();

        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
	}
}