package me.learn.android.p1.exoavatar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity {
	
	public static final String BASE_URL = "http://10.0.2.2:8080"; // 10.0.3.2 for Genymotion.
	public static final String USERNAME = "root";
	public static final String PASSWORD = "gtn";
	public static final String PEOPLE_REST_URIS[] = {
		"/portal/rest/social/people/getPeopleInfo/",	// the rest provided by Social.
		".json",	// append this after USERNAME.
		"avatarURL"	// this is the key to get avatar url from JSON response.
	};
	public static final String UPLOAD_AVATAR_URI = "/rest/private/me/updateAvatar";
	public ImageView avatar_view;
	public static final int REQUEST_IMAGE_CAPTURE = 1;
	
	/**
	 * Constructor. Set Authenticator, CookieManager here.
	 */
	public MainActivity() {
		super();
		Authenticator.setDefault(new Authenticator() {
			@Override
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(USERNAME, PASSWORD.toCharArray());
			}
		});
	}
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Set UI Component here.
		avatar_view = (ImageView) findViewById(R.id.avatar_view);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		(new DownloadAvatarAsyncTask()).execute();
	}
	
	// Start an activity for the user picks or takes a photo.
	// The photo is passed to onActivityResult which calls the upload async task.
	public void uploadAvatar(View view) {
		takeAvatar();
		//pickAvatar();
	}
	
	public void takeAvatar() {
		Intent takeAvatarIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		if (takeAvatarIntent.resolveActivity(getPackageManager()) != null) {
			startActivityForResult(takeAvatarIntent, REQUEST_IMAGE_CAPTURE);
		}
	}
	
	public void pickAvatar() {
		//
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_IMAGE_CAPTURE) {
			Bundle extras = data.getExtras();
			Bitmap bitmap = (Bitmap) extras.get("data");
			(new UploadAvatarAsyncTask()).execute(bitmap);
		}
	}
	
	private class DownloadAvatarAsyncTask extends AsyncTask<String, Void, Bitmap> {
		@Override
		protected Bitmap doInBackground(String...strings) {
			Bitmap result = null;
			JSONObject jsonObj = null;
			String avatar_url = null;
			try {
				jsonObj = downloadJson(BASE_URL + PEOPLE_REST_URIS[0] + USERNAME + PEOPLE_REST_URIS[1]);
				avatar_url = BASE_URL + jsonObj.getString(PEOPLE_REST_URIS[2]);
			} catch (IOException | JSONException e) {
				e.printStackTrace();
			}
			try {
				result = downloadBitmap(avatar_url);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			avatar_view.setImageBitmap(result);
		}
		
		JSONObject downloadJson(String url) throws IOException, JSONException {
			JSONObject jsonObj = null;
			InputStream inStream = null;
			try {
				HttpURLConnection connection = (HttpURLConnection) (new URL(url)).openConnection();
				connection.connect();
				inStream = connection.getInputStream();
				String line = null;
				StringBuilder builder = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(inStream));
				while ((line = reader.readLine()) != null) {
					builder.append(line).append("\n");
				}
				jsonObj = new JSONObject(builder.toString());
			} finally {
				if (inStream != null) {
					inStream.close();
				}
			}
			return jsonObj;
		}
		
		Bitmap downloadBitmap(String url) throws IOException {
			Bitmap bitmap = null;
			try {
				HttpURLConnection connection = (HttpURLConnection) (new URL(url).openConnection());
				connection.connect();
				bitmap = BitmapFactory.decodeStream(connection.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return bitmap;
		}
	}
	
	private class UploadAvatarAsyncTask extends AsyncTask<Bitmap, Void, Void> {
		
		@Override
		protected Void doInBackground(Bitmap...bitmaps) {
			// convert Bitmap to Byte array.
			Bitmap bitmap = bitmaps[0];
			byte[] data = null;
			try {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
				bitmap.recycle();
				data = stream.toByteArray();
				stream.flush();
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// String constants to write multipart.
			String boundary = "*****";
			String twoHyphens = "--";
			String crlf = "\r\n";
			
			try {
				// Set up connection.
				HttpURLConnection connection = (HttpURLConnection) (new URL(BASE_URL + UPLOAD_AVATAR_URI)).openConnection();
				connection.setRequestMethod("POST");
				connection.setDoOutput(true);
				connection.setUseCaches(false);
				connection.setRequestProperty("Connection", "Keep-Alive");
				connection.setRequestProperty("Cache-Control", "no-cache");
				
				// Write to connection.
				connection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
				DataOutputStream requestStream = new DataOutputStream(connection.getOutputStream());
				requestStream.writeBytes(twoHyphens + boundary + crlf);
				requestStream.writeBytes("Content-Disposition: form-data;"
						+ "name=\"avatar\";"
						+ "filename=\"avatar.png\""
						+ crlf);
				requestStream.writeBytes("Content-Type: image/*" + crlf);
				requestStream.writeBytes(crlf);
				requestStream.write(data);
				requestStream.writeBytes(crlf);
				requestStream.writeBytes(twoHyphens + boundary + twoHyphens + crlf);
				requestStream.flush();
				requestStream.close();
				
				// End the connection.
				connection.getResponseCode();
				Log.d("me.learn.android.p1.exoavatar", Integer.toString(connection.getResponseCode()));
				connection.disconnect();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}
}
