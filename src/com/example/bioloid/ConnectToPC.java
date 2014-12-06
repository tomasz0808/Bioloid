package com.example.bioloid;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;


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
	
	
	
	
	
	public static InetSocketAddress myServerPort = new InetSocketAddress( 10006);
	public Thread connectToServer;
	public static boolean isConnected;
	public String dataIn;
	public String text;	
	int message;

	//defined new layout components
	private EditText serverIpAddrText;	
	private EditText intervalEditText;

	private TextView statusTextView;
	private TextView statusTextView2;
	private TextView ipTextView;

	private Button saveChangesButton;
	private ToggleButton statusToggleButton;
	private Switch connectToPcSwitch;


//	private Toast informToast;
	
	
	//shared preferences
	private SharedPreferences sharedPreferences;
	private Editor editor;
	
	//defined new variables
	static final int PORT_NUMBER = 10006;
	public static SocketAddress  serverAddress;
	private WifiManager wifiManager;
	boolean wifiEnabled;
	boolean lostConnection;
	boolean isReportingEnabled;
	private String serverIpAddresString;
	private String textFromInterval;
	int intervalValue;
	private Intent speekIntent;
	
	
	
	
	 /** Called when the activity is first created. */
	 @Override
	 public void onCreate(Bundle savedInstanceState) {
		 super.onCreate(savedInstanceState);
	     setContentView(R.layout.activity_connect_to_pc);
	     speekIntent = new Intent(getApplicationContext(), Speek.class);
	     StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	     StrictMode.setThreadPolicy(policy);
	     wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
	     sharedPreferences = getApplicationContext().getSharedPreferences("ConnectToPCSharedPrefs", MODE_PRIVATE);
	     editor = sharedPreferences.edit();
	    
	     
	     
	     serverIpAddrText 	= (EditText)findViewById(R.id.serverIpAddress);
	     intervalEditText 	= (EditText)findViewById(R.id.intervelMinutesEditText);
	     statusTextView 	= (TextView)findViewById(R.id.statusTextView);
	     statusTextView2	= (TextView)findViewById(R.id.statusTextViewStatus2);
	     ipTextView			= (TextView)findViewById(R.id.textView3);	     
	     	          
	     statusToggleButton = (ToggleButton)findViewById(R.id.connectionStatusToggleButton);
	     saveChangesButton 	= (Button)findViewById(R.id.saveChangesButton);
	     connectToPcSwitch 	= (Switch)findViewById(R.id.switch1);
	     
	     if(sharedPreferences.getBoolean("IS_REPORTING", true))
	    	 connectToPcSwitch.setChecked(true);
	     else{
	    	 connectToPcSwitch.setChecked(false);
	    	 hideIcons(true);
	     }
	     
	     
	     serverIpAddrText.setSingleLine(true);
	     serverIpAddrText.setText(sharedPreferences.getString("SERVER_IP_ADDRESS", ""));
	     
	     intervalEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
	     intervalValue = sharedPreferences.getInt("INTERVAL_TIME", 0); 
	     
	     boolean lostConnectionOnStart = sharedPreferences.getBoolean("LOST_CONNECTION", true);
	     
	     if(intervalValue !=0){
	    	 textFromInterval = Integer.toString(intervalValue);
	    	 intervalEditText.setText(textFromInterval);
	     } else
	    	 intervalEditText.setText("30");
	     
	     if(socketOut != null && !lostConnectionOnStart){
	    	 if(socketOut.isConnected()){
	    		 	connect(true);
	    	 }
	     }else{
		     connect(false);
	     }
	     
	     
	     //listener for switch
		 connectToPcSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					if(isChecked) 										
						hideIcons(false);
					else
						hideIcons(true);					
				}			
			});	
		 
		 statusToggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {				
					if(isChecked){
						serverIpAddresString = getIpFromEditText();
						wifiEnabled = wifiManager.isWifiEnabled(); 
						if(!wifiManager.isWifiEnabled()){
							Toast.makeText(getApplicationContext(), "Please turn on Wi-fi, and connect to network.", Toast.LENGTH_LONG).show();
							statusToggleButton.setChecked(false);
						}else if(serverIpAddresString.isEmpty() || !isIpCorrect(serverIpAddresString)){
							Toast.makeText(getApplicationContext(), "IP address in invalid.", Toast.LENGTH_LONG).show();
							statusToggleButton.setChecked(false);						
						}else{
							serverAddress = new InetSocketAddress(serverIpAddresString, PORT_NUMBER);
							socketOut = new Socket();
							 try {
								socketOut.connect(serverAddress, 2000);
								if(socketOut.isConnected()){
									
									editor.putString("SERVER_IP_ADDRESS", serverIpAddresString);
									editor.commit();
									datainputStream = new DataInputStream(socketOut.getInputStream());
									
							        new Thread(new Runnable() { 
							            public void run(){
							            	try {
												if(datainputStream.read()==-1 && !lostConnection)	{																													
													lostConnection = true;													
												}
											} catch (IOException e){
												lostConnection = true;
												
											}
							            	if(lostConnection){							            	
							            		ConnectToPC.this.runOnUiThread(new Runnable(){
							                        public void run(){							                        																					
																connect(false);	
																editor.putBoolean("LOST_CONNECTION", lostConnection);
																editor.commit();	
																lostConnection = false;		
																speekIntent.putExtra("Text", "Warning. Connection to the server has been lost.");																	
																startService(speekIntent);
																Toast.makeText(getApplicationContext(), "Connection to server Lost", Toast.LENGTH_LONG).show();
															}
							            		});
							            	}
							            }
							        }).start();
							        
							        connect(true);
									
								}								
							} catch (IOException e) {
								statusToggleButton.setChecked(false);
								Toast.makeText(getApplicationContext(), "Unable to connect, check if IP is correct", Toast.LENGTH_LONG).show();															
							}
						}						
					} else{
						if(socketOut.isConnected()){
							try {
								socketOut.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						connect(false);
					}
				}
			});
		 
		 saveChangesButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				textFromInterval = intervalEditText.getText().toString();
				intervalValue = Integer.parseInt(textFromInterval);
				if(intervalValue==0){
					Toast.makeText(getApplicationContext(), "Please enter correct interval value", Toast.LENGTH_LONG).show();
				} else{
					editor.putInt("INTERVAL_TIME", intervalValue);
					editor.commit();
					intervalEditText.clearFocus();
					Toast.makeText(getApplicationContext(), "Changes saved.", Toast.LENGTH_SHORT).show();
				}
			}
		});
	 }
	 
	 
	 @Override
		public void onBackPressed() {
			super.onBackPressed();
			if(connectToPcSwitch.isChecked()){
				isReportingEnabled=true;
				editor.putBoolean("IS_REPORTING", true);
			}else
				editor.putBoolean("IS_REPORTING", false);
			editor.commit();
		}

	     
//	     isConnected = false;
//	     connectToRobot = Toast.makeText(getApplicationContext(), "Please Connect to Robot first", Toast.LENGTH_LONG);
//	     textOut = (EditText)findViewById(R.id.textout);

//	     Button buttonSend = (Button)findViewById(R.id.send);
//	     textIn = (TextView)findViewById(R.id.textin);
	     
//	     connectToServer(serverAddress);
//	     connectToServer = new Thread(new ListenForMessage());
//	     connectToServer.start();

//	     if(isConnected == true){
//	    	 Toast.makeText(getApplicationContext(), "Succesfully connected to server", Toast.LENGTH_LONG).show();
//	    	 connectToServer = new Thread(new ListenForMessage());
//	    	 connectToServer.start();
//	     }
//	     if(!dataIn.isEmpty()){
//	    	 textIn.setText(dataIn);
//	     }	     
//	     buttonSend.setOnClickListener(new OnClickListener() {
//			
//			@SuppressWarnings("deprecation")
//			@Override
//			public void onClick(View v) {
//				if(socketOut.isConnected())
//					try {
//						dataOutputStream.writeUTF("Gowno");
//					} catch (IOException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					} 
//				
//			}
//		});	     	     
//	     super.onCreate(savedInstanceState);	   
//	 }
	 public void connect(boolean b){
		 if(b){
			 	Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_LONG).show();
			 	serverIpAddrText.setEnabled(false);
			 	statusToggleButton.setChecked(true);
				statusTextView.setText("Connected");
				statusTextView.setTextColor(Color.parseColor("#24D91A"));		 
		 } else{
			 	serverIpAddrText.setEnabled(true);
				statusToggleButton.setChecked(false);
				statusTextView.setText("Disconnected");
				statusTextView.setTextColor(Color.parseColor("#F03835"));
			 
		 }
	 }

	public String getIpFromEditText(){
		 return serverIpAddrText.getText().toString();
	 }
	
	private Boolean isIpCorrect(String ip){
		 boolean isIPv4;
		    try {
		    	final InetAddress inet = InetAddress.getByName(ip);
		    	isIPv4 = inet.getHostAddress().equals(ip) && inet instanceof Inet4Address;
		    } catch (final UnknownHostException e){
		    	isIPv4 = false;
		    }
		    return isIPv4;
	}
	
	
	
	
	public void hideIcons(Boolean b){
		if(b){
			ipTextView.setVisibility(View.GONE);
			serverIpAddrText.setVisibility(View.GONE);
			statusToggleButton.setVisibility(View.GONE);
			statusTextView.setVisibility(View.GONE);
			statusTextView2.setVisibility(View.GONE);

		} else{
			ipTextView.setVisibility(View.VISIBLE);
			serverIpAddrText.setVisibility(View.VISIBLE);
			statusToggleButton.setVisibility(View.VISIBLE);
			statusTextView.setVisibility(View.VISIBLE);
			statusTextView2.setVisibility(View.VISIBLE);
			statusTextView2.setVisibility(View.VISIBLE);
		}		
	}
	 

	 
	 
	private boolean connectToServer(SocketAddress socketAddress) {
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
}
	
	