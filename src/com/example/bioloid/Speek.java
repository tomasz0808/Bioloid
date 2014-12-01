package com.example.bioloid;

import java.util.Locale;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Speek extends Service implements OnInitListener {

	private TextToSpeech textToSpeech;
	private String str;

	
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
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
	}
}
