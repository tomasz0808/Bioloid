package com.example.bioloid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
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


	 /** Called when the activity is first created. */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
	     setContentView(R.layout.activity_connect_to_pc);
	     
	     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	     StrictMode.setThreadPolicy(policy);
	    
	     connectToRobot = Toast.makeText(getApplicationContext(), "Please Connect to Robot first", Toast.LENGTH_LONG);
	     textOut = (EditText)findViewById(R.id.textout);
	     Button buttonSend = (Button)findViewById(R.id.send);
	     textIn = (TextView)findViewById(R.id.textin);
	     buttonSend.setOnClickListener(buttonSendOnClickListener);
		 btsocket = ConnectToRobot.getSocket();
		 if(btsocket == null)
			 connectToRobot.show();
		 else {
		   	try {
		   			outputStream = btsocket.getOutputStream();
		   	}catch (IOException e) {
		    		e.printStackTrace();
		   		}	    	 
		     }
	     super.onCreate(savedInstanceState);
	   
	 }
	 
	 
	 Button.OnClickListener buttonSendOnClickListener
	 = new Button.OnClickListener(){

	@Override
	public void onClick(View arg0) 
	{
		 // TODO Auto-generated method stub
		 Socket socket = null;
		 DataOutputStream dataOutputStream = null;
		 DataInputStream dataInputStream = null;

	 try 
	 {
		  socket = new Socket("192.168.0.103", 8865 );
		  dataOutputStream = new DataOutputStream(socket.getOutputStream());
		  dataInputStream = new DataInputStream(socket.getInputStream());
		  dataOutputStream.writeUTF(textOut.getText().toString());
		  textIn.setText(dataInputStream.readUTF());
		  textReceived = textIn.getText().toString();
		  if(outputStream!=null && !textReceived.isEmpty())
			  outputStream.write(ConnectToRobot.sendMessageToRobot(2));
		  else
			  connectToRobot.show();
			  
	 } catch (UnknownHostException e) {
	  // TODO Auto-generated catch block
		 e.printStackTrace();
	 } catch (IOException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	 }
	 finally{
	  if (socket != null){
	   try {
	    socket.close();
	   } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	  }

	  if (dataOutputStream != null){
	   try {
	    dataOutputStream.close();
	   } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	  }

	  if (dataInputStream != null){
	   try {
	    dataInputStream.close();
	   } catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	   }
	  }
	 }
	}};
	}