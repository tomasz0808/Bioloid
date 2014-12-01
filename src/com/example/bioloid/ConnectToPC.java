package com.example.bioloid;

import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class ConnectToPC extends Activity {
	
	private BluetoothSocket socket;
	private OutputStream outputStream;
	private Intent	intent;
	private byte[] message;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		intent = new Intent(getApplicationContext(), Speek.class);
		setContentView(R.layout.activity_connect_to_pc);
		socket = ConnectToRobot.getSocket();
		message = ConnectToRobot.sendMessageToRobot(2);
		
		
		
		if(socket==null)
		{
			Toast.makeText(getApplicationContext(), "ConnetctToRobotFirst", Toast.LENGTH_LONG).show();
		}else{
		
		if(socket.isConnected()){	
			intent.putExtra("Text", "Slavery gets shit done");	
			try {
				outputStream = socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				outputStream.write(message);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			intent.putExtra("Text", "Slavery gets shit done");	
		}
		startService(intent);
		}
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.connect_to_pc, menu);
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
}
