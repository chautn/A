package com.example.netconnect;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.sip.SipSession.State;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CheckNetworkActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		TextView view_wifi = new TextView(this);
		view_wifi.setText("Wifi: " + checkWifi());
		
		TextView view_mobile = new TextView(this);
		view_mobile.setText("Mobile: " + checkMobile());
		
		layout.addView(view_wifi);
		layout.addView(view_mobile);
		setContentView(layout);
	}
	
	public String checkWifi() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return(netInfo.getState().toString());
	}
	
	public String checkMobile() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		return(netInfo.getState().toString());
	}

}
