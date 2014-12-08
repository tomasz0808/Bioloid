package com.example.bioloid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Random;

import android.app.Activity;
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
import android.widget.Toast;

public class Start extends Activity {

	// shared preferences
	private SharedPreferences sharedPreferences;
	private Editor editor;

	public Socket pcSocket;
	// public BluetoothSocket btSocket;
	public static DataOutputStream pcOutput;

	private Intent recognizeSpeechService;
	private Intent speekService;
	public static TextView methodText;
	public static TextView resultsText;
	private static Messenger mServiceMessenger;
	// Timer timer = new Timer();
	MyReceiver myReceiver;

	protected static final boolean DEBUG = false;
	public boolean conversationThreadStop = false;
	public boolean isConnectedToPC = false;
	public boolean lostConnection = false;
	private boolean tutorialStart;
	private boolean textRecognized;
	public boolean isReportingToPC = false;
	public boolean isFinished = false;
	public boolean sleepThreadStop = true;
	
	
	protected static final String TAG = null;
	public String datapassed;
	public String stringForWaitUser;
	public static String waitForTTStoFinishString;
	public static String waitForTTSandUser;
	public int intervalTime;
	private int mBindFlag;
	public int sleepTimeInMS;

	public TutorialThread tutorialThread;
	public ConversationThread conversationThread;
	public SleepThread	sleepThread;
	public MonitorPCConnection monitorPcConnection;
	
	
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

		sharedPreferences = getApplicationContext().getSharedPreferences("ConnectToPCSharedPrefs", MODE_PRIVATE);
		editor = sharedPreferences.edit();
		intervalTime = sharedPreferences.getInt("INTERVAL_TIME", 5);
		lostConnection = sharedPreferences.getBoolean("LOST_CONNECTION", false);
		tutorialStart = sharedPreferences.getBoolean("TUTORIAL_START", false);

		conversationThread = new ConversationThread();
		tutorialThread = new TutorialThread();
		sleepThread = new SleepThread();
		
//		sleepTimeInMS = intervalTime*60*1000;
		sleepTimeInMS = 10000;
		
		
		methodText = (TextView) findViewById(R.id.deviceFound);
		resultsText = (TextView) findViewById(R.id.TextView2);
		mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;

		myReceiver = new MyReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(SpechRecognition.MY_ACTION);
		registerReceiver(myReceiver, intentFilter);
	}

	public int getRandom(int range) {
		return rand.nextInt(range);
	}

	public void sayText(String say) {
		speekService.putExtra("Text", say);
		startService(speekService);
	}

	public void sayTextFromResources(String say) {
		int getResID = getResources().getIdentifier(say, "string", getPackageName());
		say = getString(getResID);
		speekService.putExtra("Text", say);
		startService(speekService);
	}

	public void sayTextFromResourcesRandom(String say, int range) {
		StringBuilder sb = new StringBuilder(String.valueOf(say));
		sb.append(getRandom(range));
		say = sb.toString();

		int getResID = getResources().getIdentifier(say, "string", getPackageName());
		say = getString(getResID);
		speekService.putExtra("Text", say);
		startService(speekService);
	}

	public void setBackground(String string) {
		int resourceId = getResources().getIdentifier(string, "drawable", getPackageName());
		getWindow().setBackgroundDrawableResource(resourceId);

	}

	@Override
	protected void onStart() {
		super.onStart();
		
		pcSocket = ConnectToPC.socketOut;
		if (pcSocket != null && pcSocket.isConnected()) {
			try {
				pcOutput = new DataOutputStream(pcSocket.getOutputStream());
				monitorPcConnection = new MonitorPCConnection();				
				isConnectedToPC = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(isConnectedToPC){
			monitorPcConnection.start();
		}
		tutorialThread.start();

		startService(recognizeSpeechService);
		bindService(new Intent(this, SpechRecognition.class), mServiceConnection, mBindFlag);
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(myReceiver);
		if (mServiceMessenger != null) {
			unbindService(mServiceConnection);
			mServiceMessenger = null;
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		onDestroy();
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			if (DEBUG) {
				Log.d(TAG, "onServiceConnected");} //$NON-NLS-1$
			mServiceMessenger = new Messenger(service);
			// sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			if (DEBUG) {
				Log.d(TAG, "onServiceDisconnected");} //$NON-NLS-1$
			mServiceMessenger = null;
		}
	};

	public static void sendMessage(int type) {
		Message msg = new Message();
		msg.what = type;
		try {
			mServiceMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		conversationThreadStop = true;
		lostConnection = true;
		stopService(recognizeSpeechService);
		stopService(speekService);

	}

	public boolean isConnectionLost() {
		boolean isLost = sharedPreferences.getBoolean("LOST_CONNECTION", false);
		return isLost;
	}


	// public void waitForUser(){
	// sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
	// synchronized (stringForWaitUser) {
	// try {
	// stringForWaitUser.wait();
	//
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	//
	// }
	// }
	public void waitForTTStoFinish() {
		synchronized (waitForTTStoFinishString) {
			try {
				waitForTTStoFinishString.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	public void waitForTtsAndUser() {
		synchronized (waitForTTStoFinishString) {
			try {
				waitForTTStoFinishString.wait();
				stringForWaitUser.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	public void sendToPC(String s){
		if(isConnectedToPC){
			try {
				pcOutput.writeUTF(s);
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Unable to send message to PC", Toast.LENGTH_LONG).show();
			}
		}else{
			Toast.makeText(getApplicationContext(), "Not connected to PC", Toast.LENGTH_SHORT).show();
		}
	}
	
	public boolean ifYes(String string) {
		return string.contains("yes");
	}

	public boolean ifNo(String string) {
		return string.contains("no");
	}
	public boolean ifHelp(String string) {
		return string.contains("help");
	}

	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			// TODO Auto-generated method stub

			datapassed = arg1.getStringExtra("DATAPASSED");
			if (!datapassed.equalsIgnoreCase("")){
				if(sleepThreadStop==true){
					synchronized (stringForWaitUser) {
						stringForWaitUser.notify();
					}
				}else{
					sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
				}
			}

		}
	}

	

	private class TutorialThread extends Thread {
		boolean nameRecognized = false;
		String name;

		@Override
		public void run() {

			
			super.run();
//			if (tutorialStart) {
				sendToPC("Robot started with tutorial");
//			} else {
//				sendToPC("Robot started");
//			}
			try {
				Thread.sleep(2000);
				sayText("Hello");
				waitForTTStoFinish();
			} catch (InterruptedException e) {
			}
			while (!nameRecognized) {
				sayTextFromResourcesRandom("whatIsYourNameText", 5);
				waitForTtsAndUser();
				sayTextFromResourcesRandom("confirmName", 3);
				waitForTTStoFinish();
				sayText(datapassed);
				name = datapassed;
				waitForTtsAndUser();
				if (ifYes(datapassed)) {
					nameRecognized = true;
					editor.putString("USER_NAME", name);
					editor.commit();
					sendToPC("Patient name: "+name+". Interval set to: "+intervalTime+" minutes.");
				}
			}
			
			sayText("Ok, " + name);
			waitForTTStoFinish();
			if (tutorialStart) {
				sayTextFromResources("tutorialText");
				waitForTTStoFinish();
				
			} 
				sayText("I will check on you every");
				waitForTTStoFinish();
				sayText(String.valueOf(intervalTime) + ",minutes");
				waitForTTStoFinish();
								

			datapassed = "";
			sleepThreadStop=false;
			sleepThread.start();
			
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sleepThreadStop = true;		
			
			
//			sleepThread.in
		} 
		
	}
	
	private class SleepThread extends Thread {

	@Override
	public void run() {
		super.run();
		sayText("Ok, I'm going to sleep now  for 10 seconds ");
		waitForTTStoFinish();
		sendToPC("Robot went to sleep mode for "+intervalTime+" minutes.");
		while (!sleepThreadStop) {
			
			if(ifHelp(datapassed))
			{				
				sendToPC("Alert! Robot has been woken up by patient.");
				sayText("What's wrong!?!");
				waitForTTStoFinish();
				sleepThreadStop = true;				
			}
			
			}
			conversationThread.start();
		}
	}

	private class ConversationThread extends Thread {

		@Override
		public void run() {
			 
			super.run();
			sendToPC("Robot woke up after sleep time. Checking patient status");
			sayText("Conversation");
			waitForTTStoFinish();

			}

		}
	
	private class MonitorPCConnection extends Thread {
		
		boolean lostConnection = false;
		public void run() {			 
			super.run();
	
			while(!lostConnection){
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
				lostConnection = sharedPreferences.getBoolean("LOST_CONNECTION", false);
			}
			isConnectedToPC = false;
			try {
				pcOutput.close();
				pcSocket.close();
			} catch (IOException e) {

			}
		}

	}
	
}

