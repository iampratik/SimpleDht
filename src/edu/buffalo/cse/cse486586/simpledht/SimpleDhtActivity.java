package edu.buffalo.cse.cse486586.simpledht;

import android.util.Log;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SimpleDhtActivity extends Activity {

	public  final static Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");
	public static ContentResolver mContentResolver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_dht_main);
		mContentResolver = getContentResolver();
		final TextView tv = (TextView) findViewById(R.id.textView1);
		tv.setMovementMethod(new ScrollingMovementMethod());
		findViewById(R.id.button3).setOnClickListener(
				new OnTestClickListener(tv, getContentResolver()));

		Button ldump = (Button)findViewById(R.id.button1);
		ldump.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Cursor c = mContentResolver.query(mUri, null, "@", null, null);
				if (c.moveToFirst()) {
					while (!c.isAfterLast()) {
						int keyIndex = c.getColumnIndex("key");
						int valueIndex = c.getColumnIndex("value");
						String key = c.getString(keyIndex);
						String val = c.getString(valueIndex);

						tv.append("key:"+key+" val:"+val+"\n");
						c.moveToNext();
					}
				}
			}
		});
		
		Button gdump = (Button)findViewById(R.id.button2);
		gdump.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Cursor c = mContentResolver.query(mUri, null, "*", null, null);
				if (c.moveToFirst()) {
					while (!c.isAfterLast()) {
						int keyIndex = c.getColumnIndex("key");
						int valueIndex = c.getColumnIndex("value");
						String key = c.getString(keyIndex);
						String val = c.getString(valueIndex);
						Log.d("GDUMP", "key:"+key+" val:"+val+"\n");
						tv.append("key:"+key+" val:"+val+"\n");
						c.moveToNext();
					}
				}
			}
		});

		Button delete = (Button)findViewById(R.id.button4);
		delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int num = mContentResolver.delete(mUri, "@", null);
				tv.append(num+ " rows deleted");
			}
		});
		
		Button deleteAll =  (Button)findViewById(R.id.button5);
		deleteAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				int num = mContentResolver.delete(mUri, "*", null);
				tv.append("All rows deleted");
			}
		});
		
		Button clear = (Button)findViewById(R.id.button6);
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				tv.setText("");
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_simple_dht_main, menu);
		return true;
	}

	private static Uri buildUri(String scheme, String authority) {
		Uri.Builder uriBuilder = new Uri.Builder();
		uriBuilder.authority(authority);
		uriBuilder.scheme(scheme);
		return uriBuilder.build();
	}

}
