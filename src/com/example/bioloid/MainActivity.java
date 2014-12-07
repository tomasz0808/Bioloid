package com.example.bioloid;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

	private Intent intent;
	private Toast toast;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);			
        createToast(true);
    }	
	
	private void createToast(boolean show ) {
		Context context = getApplicationContext();
		CharSequence startChar = "Click to start.";
		int duration = Toast.LENGTH_LONG;
		
		toast = Toast.makeText(context, startChar, duration);
		if(show)
			toast.show();
	}

	public void mainMenu (View view)
	{
		toast.cancel();
		intent = new Intent(MainActivity.this, MainMenu.class);
		startActivity(intent);
		this.finish();
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

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
	}
}
