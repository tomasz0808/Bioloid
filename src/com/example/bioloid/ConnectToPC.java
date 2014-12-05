package com.example.bioloid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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
	public static Socket socketOut = null;
	public static Socket socketIn = null;
	public static DataOutputStream dataOutputStream = null;
	public static DataInputStream datainputStream = null;
	public static SocketAddress  serverAddress = new InetSocketAddress("192.168.0.104", 10006);
	public static InetSocketAddress myServerPort = new InetSocketAddress( 10006);
	public Thread connectToServer;
	public static boolean isConnected;
	public String dataIn;
	public String text;
	
	int message;

	 /** Called when the activity is first created. */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     setContentView(R.layout.activity_connect_to_pc);
	     
	     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	     StrictMode.setThreadPolicy(policy);
	     
	     isConnected = false;
	     connectToRobot = Toast.makeText(getApplicationContext(), "Please Connect to Robot first", Toast.LENGTH_LONG);
	     textOut = (EditText)findViewById(R.id.textout);

	     Button buttonSend = (Button)findViewById(R.id.send);
	     textIn = (TextView)findViewById(R.id.textin);
	     
	     connectToServer(serverAddress);
	     connectToServer = new Thread(new ListenForMessage());
	     connectToServer.start();

	     if(isConnected == true){
	    	 Toast.makeText(getApplicationContext(), "Succesfully connected to server", Toast.LENGTH_LONG).show();
//	    	 connectToServer = new Thread(new ListenForMessage());
//	    	 connectToServer.start();
	     }
//	     if(!dataIn.isEmpty()){
//	    	 textIn.setText(dataIn);
//	     }	     
	     buttonSend.setOnClickListener(new OnClickListener() {
			
			@SuppressWarnings("deprecation")
			@Override
			public void onClick(View v) {
				if(socketOut.isConnected())
					try {
						dataOutputStream.writeUTF("Gowno");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				
			}
		});	     	     
	     super.onCreate(savedInstanceState);	   
	 }

	private boolean connectToServer(SocketAddress socketAddress2) {
		 try {
			 socketOut = new Socket();
			 socketOut.connect(serverAddress);
			 dataOutputStream = new DataOutputStream(socketOut.getOutputStream());
			 datainputStream = new DataInputStream(socketOut.getInputStream());
			 textIn.setText(text);
			 
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
	private class ListenForMessage extends Thread{
		
		
		@Override
		public void run() {
			while(socketOut.isConnected()){
				
				try {
				     
					text = datainputStream.readUTF();
					
					runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        	textOut.setText(text);
                        }
                    });
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}
		

	}
}
	
	