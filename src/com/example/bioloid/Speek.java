package com.example.bioloid;

import java.util.Locale;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Speek extends Service implements OnInitListener {

	public static TextToSpeech textToSpeech;
	private String str;
	AudioManager aMenager;
	
	

	
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		aMenager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		textToSpeech = new TextToSpeech(this, this);
		str =(String) intent.getExtras().get("Text");
		return START_STICKY;
	}

	@Override
	public void onInit(int status) {
		 if (status == TextToSpeech.SUCCESS) {
	            int result = textToSpeech.setLanguage(Locale.ENGLISH);
	            if (result == TextToSpeech.LANG_MISSING_DATA ||
	                result == TextToSpeech.LANG_NOT_SUPPORTED) {	       
	            } else {
	                sayHello(str);
	            }
	        }
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDestroy() {
		if (textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
        }
		super.onDestroy();
	}



	@SuppressWarnings("deprecation")
	private void sayHello(String str) {
	      textToSpeech.speak(str,
	                TextToSpeech.QUEUE_FLUSH, 
	                null);
	      aMenager.setMicrophoneMute(true);
	      while(textToSpeech.isSpeaking()){
	    	  
				
			}	     	     
	      aMenager.setMicrophoneMute(false);
	      synchronized (Start.waitForTTStoFinishString) {
	    	  Start.waitForTTStoFinishString.notify();
		}
	      Start.sendMessage(SpechRecognition.MSG_RECOGNIZER_START_LISTENING);
			
	}
}
