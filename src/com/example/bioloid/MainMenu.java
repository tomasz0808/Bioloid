package com.example.bioloid;

import java.io.IOException;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainMenu extends Activity {
	
	private Button start;
	private Button connectToRobot;
	private Button connectToPC;
	private Button exit;
	private Intent intent;
	boolean doubleBackToExitPressedOnce;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_menu);
		exit = (Button)findViewById(R.id.exitAppButton);
		exit.setOnClickListener(exitApp);
	}
	public void onBackPressed() {
	    if (doubleBackToExitPressedOnce) {
	        super.onBackPressed();
	        return;
	    }

	    this.doubleBackToExitPressedOnce = true;
	    Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

	    new Handler().postDelayed(new Runnable() {

	        @Override
	        public void run() {
	            doubleBackToExitPressedOnce=false;                       
	        }
	    }, 2000);
	} 

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return true;
	}
	
//method to exit app
	private View.OnClickListener exitApp = new View.OnClickListener() {
		@Override
		public void onClick(View v) 
		{
			System.exit(0);
			BluetoothSocket socket = ConnectToRobot.getSocket();
			try {
				socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	};
	
//method to enter options
	public void connectToRobot(View v) {
		intent = new Intent(MainMenu.this, ConnectToRobot.class);
		startActivity(intent);
	}
	public void connectToPC(View v) {
		intent = new Intent(MainMenu.this, ConnectToPC.class);
		startActivity(intent);
	}
	public void startApp(View v) {
		intent = new Intent(MainMenu.this, Start.class);
		startActivity(intent);
	}
	
	
	
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
}
