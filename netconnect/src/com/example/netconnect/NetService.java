package com.example.netconnect;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class NetService extends IntentService {
	
	public static final String URL_EXTRA = "url";
	public static final String PENDING_RESULT_EXTRA = "pending_result";
	public static final int RESULT_CODE = 1;
	public static final int ERROR_CODE = 100;
	public static final String RESULT_EXTRA = "result";
	
	public NetService () {
		//
		super(NetService.class.getSimpleName());
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
		InputStream in = null;
		try {
			try {
				URL url = new URL(intent.getStringExtra(URL_EXTRA));
				in = url.openStream();
				Intent result = new Intent();
				reply.send(this, RESULT_CODE, result);			
			} catch (MalformedURLException e) {
				reply.send(ERROR_CODE);
			} catch (Exception e) {
				reply.send(ERROR_CODE);
			}
		} catch (PendingIntent.CanceledException e) {
			Log.v(NetService.class.toString(), "reply canceled", e);
		}
	}

}
