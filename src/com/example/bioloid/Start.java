package com.example.bioloid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

public class Start extends Activity {
	
	//shared preferences
	private SharedPreferences sharedPreferences;
	private Editor editor;
	public boolean isReportingToPC = false;
	
	public boolean isFinished = false;
	
	public int intervalTime;
	public Socket pcSocket;
	public DataOutputStream pcOutput;
	
	
	protected static final boolean DEBUG = false;
	protected static final String TAG = null;
	private int mBindFlag;
	public String datapassed;
	private Intent recognizeSpeechService;
	private Intent speekService;
	public static TextView methodText;
	public static TextView resultsText;
	private static Messenger mServiceMessenger;
	Timer timer = new Timer();
	MyReceiver myReceiver;
	public ConversationThread conversation;

	
	public boolean threadStop = false;
	public boolean isConnectedToPC = false;
	public boolean lostConnection = false;
	private boolean tutorialStart;
	private boolean textRecognized;
	public String datapassedaaaa;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_start);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);   
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setBackground("angry");
	    datapassedaaaa = "";
	    speekService = new Intent(getApplicationContext(), Speek.class);
	    recognizeSpeechService = new Intent(getApplicationContext(), SpechRecognition.class);
	    startService(recognizeSpeechService);
	    conversation = new ConversationThread();
	    sharedPreferences = getApplicationContext().getSharedPreferences("ConnectToPCSharedPrefs", MODE_PRIVATE);
//	    editor = sharedPreferences.edit();
	    intervalTime = sharedPreferences.getInt("INTERVAL_TIME", 15);
	    lostConnection = sharedPreferences.getBoolean("LOST_CONNECTION", false);
	    tutorialStart = sharedPreferences.getBoolean("TUTORIAL_START", false);
//	    isReportingToPC = sharedPreferences.getBoolean("IS_REPORTING", false);
	       
	    	pcSocket = ConnectToPC.socketOut;
	    	if(pcSocket !=null && pcSocket.isConnected()){
	    		
	    		try {
	    			pcOutput = new DataOutputStream(pcSocket.getOutputStream());
	    			isConnectedToPC = true;
	    		} catch (IOException e) {
				// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
	    }
	    
	    
	    methodText 	= (TextView) findViewById(R.id.deviceFound);
	    resultsText = (TextView) findViewById(R.id.TextView2);
	    mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
	    speekService = new Intent(getApplicationContext(), Speek.class);
	    recognizeSpeechService = new Intent(getApplicationContext(), SpechRecognition.class);
	    startService(recognizeSpeechService);
	  
	    myReceiver = new MyReceiver();
	    IntentFilter intentFilter = new IntentFilter();
	    intentFilter.addAction(SpechRecognition.MY_ACTION);
	    registerReceiver(myReceiver, intentFilter);

	    
	    

	}


	public void sayText(String say){
		speekService.putExtra("Text", say);																	
		startService(speekService);
		say = "";
	}
	
	public void sayTextFromResources(String s){
		int getResID = getResources().getIdentifier(s, "string", getPackageName());
		String say = getString(getResID);
		speekService.putExtra("Text", say);																	
		startService(speekService);
	}
	
	public void setBackground(String string) {
		// TODO Auto-generated method stub
		int resourceId= getResources().getIdentifier(string, "drawable", getPackageName());
		getWindow().setBackgroundDrawableResource(resourceId);
		
	}

	@Override
	protected void onStart() {
		 
	    super.onStart();
	    speekService = new Intent(getApplicationContext(), Speek.class);
	    recognizeSpeechService = new Intent(getApplicationContext(), SpechRecognition.class);
	    startService(recognizeSpeechService);
	    conversation.start();
	    


	    bindService(new Intent(this, SpechRecognition.class), mServiceConnection, mBindFlag);
	}

	@Override
	protected void onStop()
	{
	    super.onStop();
	    unregisterReceiver(myReceiver);
	    
	    if (mServiceMessenger != null)
	    {
	        unbindService(mServiceConnection);
	        mServiceMessenger = null;
	    }
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection()
	{
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service){
	        if (DEBUG) {Log.d(TAG, "onServiceConnected");} //$NON-NLS-1$
		        mServiceMessenger = new Messenger(service);
		        sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
	    }
	    @Override
	    public void onServiceDisconnected(ComponentName name){
	        if (DEBUG) {Log.d(TAG, "onServiceDisconnected");} //$NON-NLS-1$
	        	mServiceMessenger = null;
	    }
	}; // mServiceConnection

	public static void sendMessage(int type){
	     Message msg = new Message();
	     msg.what = type; 
	     try{
	         mServiceMessenger.send(msg);
	     } 
	     catch (RemoteException e){
	         e.printStackTrace();
	     }
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy(){
	    super.onDestroy();
	    threadStop = true;
	    stopService(recognizeSpeechService);
	    stopService(speekService);

	}
	
	public boolean isConnectionLost(){
		boolean isLost = sharedPreferences.getBoolean("LOST_CONNECTION", false);
		return isLost;
	}
	
	public void waitThread() throws InterruptedException{
		conversation.wait();
	}

	
	
	private class MyReceiver extends BroadcastReceiver{
		 
		 @Override
		 public void onReceive(Context arg0, Intent arg1) {
		  // TODO Auto-generated method stub
		  
		  datapassed = arg1.getStringExtra("DATAPASSED");
		  if(datapassed.equalsIgnoreCase("Yes"))
			  synchronized (datapassedaaaa) {
				  datapassedaaaa.notifyAll();
			}
			  	
	
		 
//		  conversation.w
//		  if(datapassed.equalsIgnoreCase("hello"));
//		  	notify();
		  
//		  NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		    //Create the notification here.
//		    nm.no
		  
		  
		  
		  
		  
		  if(datapassed.equalsIgnoreCase("yes")){
			  isFinished = true;
			 
		  
		 }else if(datapassed.equalsIgnoreCase("normal")){
			 setBackground("normal");
		 }else if(datapassed.equalsIgnoreCase("angry")){
			 setBackground("normal");
		 }else if(datapassed.equalsIgnoreCase("happy")){
			 setBackground("happy");
		 }else if(datapassed.equalsIgnoreCase("sad")){
			 setBackground("sad");
		 }else if(datapassed.equalsIgnoreCase("fuck you")){
			 sayText("Fuck you too !");
		 }else if(datapassed.equalsIgnoreCase("sleep")){
			 sayText("I'm going to sleep");			
		 }
		 else if(datapassed.equalsIgnoreCase("help") && isConnectedToPC){
			 try {
				pcOutput.writeUTF("Help");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			
		 }
		 }}
	private class ConversationThread extends Thread
	{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(!threadStop){
			sayText("Thread started");
			try {
				synchronized (datapassedaaaa) {
					datapassedaaaa.wait();	
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
				sayText("Thread started again");
			
			}
			
			

			
			
//			setBackground("happy");
//			try {
//				wait();
//			} catch (InterruptedException e) {
//				 TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			sayText("notification received");
			
		}
		
		
		
	}

}
