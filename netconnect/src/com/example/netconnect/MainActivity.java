package com.example.netconnect;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends Activity {
	
	static final int GET_HTTP_CODE = 1728;
	static final String URL = "http://localhost";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView view = (TextView) findViewById(R.id.view_message);
		view.setText(Boolean.toString(isOnline()));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public boolean isOnline() {
		boolean status = false;
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
			status = true;
		} else {
			netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
				status = true;
			}
		}
		return(status);
	}
	
	public void checkNetwork(View view) {
		Intent intent = new Intent(this, CheckNetworkActivity.class);
		startActivity(intent);
	}
	
	public void getHttp(View view) {
		//
		PendingIntent pendingResult = createPendingResult(GET_HTTP_CODE, new Intent(), 0);
		Intent intent = new Intent(this, NetService.class);
		intent.putExtra(NetService.URL_EXTRA, URL);
		intent.putExtra(NetService.PENDING_RESULT_EXTRA, pendingResult);
		startService(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//
		TextView view = (TextView) findViewById(R.id.view_message);
		if (requestCode == GET_HTTP_CODE) {
			switch (resultCode) {
			case NetService.RESULT_CODE:
				view.setText(Integer.toString(NetService.RESULT_CODE));
				break;
			case NetService.ERROR_CODE:
				view.setText(Integer.toString(NetService.ERROR_CODE));
				break;
			}
		}
		Log.v("abc", "Received " + Integer.toString(requestCode));
	}
}
