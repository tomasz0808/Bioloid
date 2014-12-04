package com.example.bioloid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


	public class ConnectToPC extends Activity {

		
	EditText textOut;
	TextView textIn;
	Toast connectToRobot; 
	String textReceived;
	private BluetoothSocket btsocket;
	private OutputStream outputStream;
	public static Socket socket = null;
	public static DataOutputStream dataOutputStream = null;
	public static SocketAddress  socketAddress = new InetSocketAddress("192.168.0.104", 10006);
	public Thread connectToServer;
	public boolean isConnected;
	public String dataIn;
	
	int message;

	 /** Called when the activity is first created. */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     setContentView(R.layout.activity_connect_to_pc);
	     
	     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	     StrictMode.setThreadPolicy(policy);
//	     connectToServer = new Thread(ListenForMessage);
	     isConnected = false;
	     connectToRobot = Toast.makeText(getApplicationContext(), "Please Connect to Robot first", Toast.LENGTH_LONG);
	     textOut = (EditText)findViewById(R.id.textout);
	     Button buttonSend = (Button)findViewById(R.id.send);
	     textIn = (TextView)findViewById(R.id.textin);
	     
	     connectToSocket(socketAddress);

	     if(isConnected == true){
	    	 Toast.makeText(getApplicationContext(), "Succesfully connected to server", Toast.LENGTH_LONG).show();

	     }
//	     if(!dataIn.isEmpty()){
//	    	 textIn.setText(dataIn);
//	     }	     
	     buttonSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(socket.isConnected())
					try {
						dataOutputStream.writeUTF("Gówno");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				
			}
		});	     	     
	     super.onCreate(savedInstanceState);	   
	 }

	private boolean connectToSocket(SocketAddress socketAddress2) {
		 try {
			  socket = new Socket();
			  socket.connect(socketAddress);
			  dataOutputStream = new DataOutputStream(socket.getOutputStream());
			  
//			  textIn.setText(dataInputStream.readUTF());
			  isConnected = true;
		 } catch (UnknownHostException e) {
			Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_LONG).show();
			isConnected = false;
		 } catch (IOException e) {
			Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_LONG).show();
			isConnected = false;
		}
		 return isConnected;
	}

}
	
	