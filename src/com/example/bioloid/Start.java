package com.example.bioloid;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class Start extends Activity {
	
	protected static final boolean DEBUG = false;
	protected static final String TAG = null;
	private int mBindFlag;
	private Start activityContext;
	private Intent speechService;
	public static TextView methodText;
	public static TextView resultsText;
	private static Messenger mServiceMessenger;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_start);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    
	    
	    setBackground("angry");
	    
	    
	    methodText = (TextView) findViewById(R.id.deviceFound);
	    resultsText = (TextView) findViewById(R.id.TextView2);
	    activityContext = this;
	    mBindFlag = Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH ? 0 : Context.BIND_ABOVE_CLIENT;
	    speechService = new Intent(getApplicationContext(), SpechRecognition.class);
	    activityContext.startService(speechService);
	    
	}
	
	private void setBackground(String string) {
		// TODO Auto-generated method stub
		int resourceId= getResources().getIdentifier(string, "drawable", getPackageName());
		getWindow().setBackgroundDrawableResource(resourceId);
		
	}

	@Override
	protected void onStart() {
	    super.onStart();
	    bindService(new Intent(this, SpechRecognition.class), mServiceConnection, mBindFlag);
	}

	@Override
	protected void onStop()
	{
	    super.onStop();

	    if (mServiceMessenger != null)
	    {
	        unbindService(mServiceConnection);
	        mServiceMessenger = null;
	    }
	}

	private final ServiceConnection mServiceConnection = new ServiceConnection()
	{
	    @Override
	    public void onServiceConnected(ComponentName name, IBinder service)
	    {
	        if (DEBUG) {Log.d(TAG, "onServiceConnected");} //$NON-NLS-1$

	        mServiceMessenger = new Messenger(service);
	       sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
	    }

	    @Override
	    public void onServiceDisconnected(ComponentName name)
	    {
	        if (DEBUG) {Log.d(TAG, "onServiceDisconnected");} //$NON-NLS-1$
	        mServiceMessenger = null;
	    }

	}; // mServiceConnection

	public static void sendMessage(int type){

	     Message msg = new Message();
	     msg.what = type; 

	     try
	     {
	         mServiceMessenger.send(msg);
	     } 
	     catch (RemoteException e)
	     {
	         e.printStackTrace();
	     }
	}

	@Override
	protected void onDestroy(){
	    super.onDestroy();
	    activityContext.stopService(speechService);
	}
	}
