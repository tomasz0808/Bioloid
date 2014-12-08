package com.example.bioloid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import android.app.Activity;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.AudioManager;
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
	
	
	
	public Socket pcSocket;
//	public BluetoothSocket btSocket;
	public DataOutputStream pcOutput;
	
	


	
	
	private Intent recognizeSpeechService;
	private Intent speekService;
	public static TextView methodText;
	public static TextView resultsText;
	private static Messenger mServiceMessenger;
//	Timer timer = new Timer();
	MyReceiver myReceiver;
	
	
	
	

	protected static final boolean DEBUG = false;
	public 	boolean threadStop = false;
	public 	boolean isConnectedToPC = false;
	public 	boolean lostConnection = false;
	private boolean tutorialStart;
	private boolean textRecognized;
	public 	boolean isReportingToPC = false;	
	public 	boolean isFinished = false;
	
	protected static final String TAG = null;
	public String datapassed;
	public String stringForWaitUser;
	public static String waitForTTStoFinishString;
	public static String waitForTTSandUser;
	public 	int intervalTime;
	private int mBindFlag;
	
	public TutorialThread tutorialThread;
	public ConversationThread conversationThread;
	
	AudioManager audioManager;
	Random rand;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_start);  
	    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setBackground("angry");
	    stringForWaitUser = "";
	    waitForTTSandUser = "";
	    waitForTTStoFinishString = "";
	    rand = new Random();
	    speekService = new Intent(getApplicationContext(), Speek.class);
	    recognizeSpeechService = new Intent(getApplicationContext(), SpechRecognition.class);   
	    
	    	    	    
	    sharedPreferences 	= getApplicationContext().getSharedPreferences("ConnectToPCSharedPrefs", MODE_PRIVATE);
	    editor 				= sharedPreferences.edit();
	    intervalTime 		= sharedPreferences.getInt("INTERVAL_TIME", 15);
	    lostConnection 		= sharedPreferences.getBoolean("LOST_CONNECTION", false);
	    tutorialStart 		= sharedPreferences.getBoolean("TUTORIAL_START", false);
	    
	    conversationThread 	= new ConversationThread();
	    tutorialThread 		= new TutorialThread();
		
	    pcSocket 			= ConnectToPC.socketOut;
	    
	    if(pcSocket !=null && pcSocket.isConnected()){
	    	try {
	    		pcOutput = new DataOutputStream(pcSocket.getOutputStream());
	    		isConnectedToPC = true;
	    	} catch (IOException e) {
	    		e.printStackTrace();
	    	}
	    }
	    
	    
	    methodText 	= (TextView) findViewById(R.id.deviceFound);
	    resultsText = (TextView) findViewById(R.id.TextView2);
	    mBindFlag 	= Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
	 	  
	    myReceiver = new MyReceiver();
	    IntentFilter intentFilter = new IntentFilter(); 
	    intentFilter.addAction(SpechRecognition.MY_ACTION);
	    registerReceiver(myReceiver, intentFilter);
	}

	public int getRandom(int range){
		return rand.nextInt(range);
	}

	public void sayText(String say){
		speekService.putExtra("Text", say);																	
		startService(speekService);
	}
	
	public void sayTextFromResources(String say){
		int getResID = getResources().getIdentifier(say, "string", getPackageName());
		say = getString(getResID);
		speekService.putExtra("Text", say);																	
		startService(speekService);
	}
	public void sayTextFromResourcesRandom(String say, int range){
		StringBuilder sb = new StringBuilder (String.valueOf(say));
		sb.append (getRandom(range));
		say = sb.toString();

		int getResID = getResources().getIdentifier(say, "string", getPackageName());
		say = getString(getResID);
		speekService.putExtra("Text", say);																	
		startService(speekService);
	}
	
	public void setBackground(String string) {
		int resourceId= getResources().getIdentifier(string, "drawable", getPackageName());
		getWindow().setBackgroundDrawableResource(resourceId);
		
	}

	@Override
	protected void onStart() {		 
	    super.onStart();
	    if(tutorialStart){
	    	tutorialThread.start();
	    }else{
	    	conversationThread.start();
	    }
//	    speekService.putExtra("Text", "");
//	    startService(speekService);	    
	    startService(recognizeSpeechService);
	    bindService(new Intent(this, SpechRecognition.class), mServiceConnection, mBindFlag);
	}

	@Override
	protected void onStop()
	{
	    super.onStop();	    
	    unregisterReceiver(myReceiver);	    
	    if (mServiceMessenger != null){
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
//		        sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
	    }
	    @Override
	    public void onServiceDisconnected(ComponentName name){
	        if (DEBUG) {Log.d(TAG, "onServiceDisconnected");} //$NON-NLS-1$
	        	mServiceMessenger = null;
	    }
	}; 

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
	
	
//	public void waitForUser(){	  
//		sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
//		  synchronized (stringForWaitUser) {
//				try {
//					stringForWaitUser.wait();
//					
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//		  }
//	}
	public void waitForTTStoFinish(){	  
		synchronized (waitForTTStoFinishString) {
			try {
				waitForTTStoFinishString.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		  
	}
	public void waitForTtsAndUser(){	  
		synchronized (waitForTTStoFinishString) {
			try {
				waitForTTStoFinishString.wait();
				stringForWaitUser.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		  
	}
	
	public boolean ifYes(String string){
		return string.contains("yes");
	}
	public boolean ifNo(String string){
		return string.contains("no");
	}
	
	
	private class MyReceiver extends BroadcastReceiver{
		 
		 @Override
		 public void onReceive(Context arg0, Intent arg1) {
		  // TODO Auto-generated method stub
		  
		  datapassed = arg1.getStringExtra("DATAPASSED");
		  if(!datapassed.equalsIgnoreCase(""))
			  synchronized (stringForWaitUser) {
				  stringForWaitUser.notify();
			}
		  
		 }}
	private class ConversationThread extends Thread
	{
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			while(!threadStop){
			sayText("Conversation");
//				waitForUser();
//				if(ifYes()){
//					sayText("said yes");
//					waitForUser();}
//				else if(ifNo()){
//					sayText("said no");
//					waitForUser();}
//				else{
//					sayText("said something else");
//					waitForUser();
//				}
			}

		
		}
	}
	private class TutorialThread extends Thread
	{
		boolean nameRecognized = false;
		String name;
		@Override
		public void run() {
			super.run();
			try {
				Thread.sleep(2000);
				sayText("Hello");
				waitForTTStoFinish();
			} catch (InterruptedException e) {
			}
			while(!nameRecognized){
				sayTextFromResourcesRandom("whatIsYourNameText", 5);
				waitForTtsAndUser();
				sayTextFromResourcesRandom("confirmName", 3);
				waitForTTStoFinish();
				sayText(datapassed);
				name = datapassed;
				waitForTtsAndUser();
				if(ifYes(datapassed)){
					nameRecognized = true;
					editor.putString("USER_NAME", name);
					editor.commit();				
				}
			}			
				sayText("Ok, "+name);
				waitForTTStoFinish();
				sayTextFromResources("tutorialText");
				waitForTTStoFinish();
				sayText(String.valueOf(intervalTime)+",minutes");
		}
		

		
	}

}









