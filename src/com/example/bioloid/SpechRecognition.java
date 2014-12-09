package com.example.bioloid;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

public class SpechRecognition extends Service
{
	protected static AudioManager mAudioManager; 
	protected SpeechRecognizer mSpeechRecognizer;
	protected Intent mSpeechRecognizerIntent;
	protected final Messenger mServerMessenger = new Messenger(new IncomingHandler(this));

	protected boolean mIsListening;
	protected volatile boolean mIsCountDownOn;
	private static boolean mIsStreamSolo;
	public static boolean isSpeechRecognized;

	static final int MSG_RECOGNIZER_START_LISTENING = 1;
	static final int MSG_RECOGNIZER_CANCEL = 2;
	private static final String TAG = null;
	
	final static String MY_ACTION = "MY_ACTION";

	@Override
	public void onCreate()
	{
	    super.onCreate();

//	    mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE); 
	    mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
	    mSpeechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
	    mSpeechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
	    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
	                                     RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
	    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
	    mSpeechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
	                                     this.getPackageName());
	}

	protected static class IncomingHandler extends Handler
	{
	    private WeakReference<SpechRecognition> mtarget;

	    IncomingHandler(SpechRecognition target)
	    {
	        mtarget = new WeakReference<SpechRecognition>(target);
	    }

	    @Override
	    public void handleMessage(Message msg)
	    {
	    	Start.methodText.setText("handleMessage");

	        final SpechRecognition target = mtarget.get();

	        switch (msg.what)
	        {
	            case MSG_RECOGNIZER_START_LISTENING:

//	                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
//	                {
	                    // turn off beep sound  
//	                    if (!mIsStreamSolo)
//	                    {
//	                        mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, true);
	                        mIsStreamSolo = true;
//	                    }
//	                }
	                 if (!target.mIsListening)
	                 {
	                     target.mSpeechRecognizer.startListening(target.mSpeechRecognizerIntent);
	                     target.mIsListening = true;
	                    //Log.d(TAG, "message start listening"); //$NON-NLS-1$
	                 }
	                 break;

	             case MSG_RECOGNIZER_CANCEL:
//	                if (mIsStreamSolo)
//	               {
//	                    mAudioManager.setStreamSolo(AudioManager.STREAM_VOICE_CALL, false);
//	                    mIsStreamSolo = false;
//	               }
	                  target.mSpeechRecognizer.cancel();
	                  target.mIsListening = false;
	                  //Log.d(TAG, "message canceled recognizer"); //$NON-NLS-1$
	                  break;
	         }
	   } 
	} 

	// Count down timer for Jelly Bean work around
	protected CountDownTimer mNoSpeechCountDown = new CountDownTimer(5000, 5000)
	{

	    @Override
	    public void onTick(long millisUntilFinished)
	    {
	    	Start.methodText.setText("onTick");
	        // TODO Auto-generated method stub

	    }

	    @Override
	    public void onFinish()
	    {

	    	Start.methodText.setText("onFinish");

	        mIsCountDownOn = false;
	        Message message = Message.obtain(null, MSG_RECOGNIZER_CANCEL);
	        try
	        {
	            mServerMessenger.send(message);
	            message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
	            mServerMessenger.send(message);
	        }
	        catch (RemoteException e)
	        {

	        }
	    }
	};

	@Override
	public void onDestroy()
	{
	    super.onDestroy();

	    Start.methodText.setText("onDestroy");

	    if (mIsCountDownOn)
	    {
	        mNoSpeechCountDown.cancel();
	    }
	    if (mSpeechRecognizer != null)
	    {
	        mSpeechRecognizer.destroy();
	    }
	}

	protected class SpeechRecognitionListener implements RecognitionListener
	{
	    private static final String TAG = "SpeechApp";

	    @Override
	    public void onBeginningOfSpeech()
	    {
	    	Start.methodText.setText("onBeginningOfSpeech");
	        // speech input will be processed, so there is no need for count down anymore
//	        if (mIsCountDownOn)
//	        {
//	            mIsCountDownOn = false;
//	            mNoSpeechCountDown.cancel();
//	        }               
	        //Log.d(TAG, "onBeginingOfSpeech"); //$NON-NLS-1$
	    }

	    @Override
	    public void onBufferReceived(byte[] buffer)
	    {
	    	Start.methodText.setText("onBufferReceived");
	    }

	    @Override
	    public void onEndOfSpeech()
	    {
	        //Log.d(TAG, "onEndOfSpeech"); //$NON-NLS-1$
	    	Start.methodText.setText("onEndOfSpeech");
	     }

	    @Override
	    public void onError(int error)
	    {
	    	Start.methodText.setText("onError");

	        if (mIsCountDownOn)
	        {
	            mIsCountDownOn = false;
	            mNoSpeechCountDown.cancel();
	        }
	         mIsListening = false;
	         Message message = Message.obtain(null, MSG_RECOGNIZER_START_LISTENING);
	         try
	         {
	                mServerMessenger.send(message);
	         }
	         catch (RemoteException e)
	         {

	         }
	        //Log.d(TAG, "error = " + error); //$NON-NLS-1$
	    }

	    @Override
	    public void onEvent(int eventType, Bundle params)
	    {
	        Start.methodText.setText("onEvent");
	    }

	    @Override
	    public void onPartialResults(Bundle partialResults)
	    {
	    	Start.methodText.setText("onPartialResults");
	    }

	    @Override
	    public void onReadyForSpeech(Bundle params)
	    {
	    	Start.methodText.setText("onReadyForSpeech");

	        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
	        {
	            mIsCountDownOn = true;
	            mNoSpeechCountDown.start();

	        }
	        Log.d(TAG, "onReadyForSpeech"); //$NON-NLS-1$
	    }

	    @Override
	    public void onResults(Bundle results)
	    {
	    	ArrayList<String> answer = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
	    	Start.methodText.setText("onResults");
	    	Intent intent = new Intent();
	    	intent.setAction(MY_ACTION);	        
	        intent.putExtra("DATAPASSED", answer.get(0));	       
	        sendBroadcast(intent);
	    	
	        Start.resultsText.setText(answer.get(0));
//	        Start.sendTextRecognized(answer.get(0));
	        mIsListening = false;
//	        Start.sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
	    }
//	    public static String waitForRespond(String recognizedText)
//		{
//		 return	recognizedText;
//			
//		}

	    @Override
	    public void onRmsChanged(float rmsdB)
	    {
	    	Start.methodText.setText("onRmsChanged");
	    }

	}

	@Override
	public IBinder onBind(Intent intent) {

		Start.methodText.setText("onBind");

	    // TODO Auto-generated method stub
	    Log.d(TAG, "onBind");  //$NON-NLS-1$
	    return mServerMessenger.getBinder();
	}
	}