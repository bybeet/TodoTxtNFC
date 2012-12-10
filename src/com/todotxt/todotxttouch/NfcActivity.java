package com.todotxt.todotxttouch;

import android.app.Activity;
import android.*;
import android.os.Bundle;

public class NfcActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);
	}

	public void createTag(){
		NdefRecord mimeRecord = NdefRecord.createMime("application/vnd.com.example.android.beam",
	    "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));
	}

}