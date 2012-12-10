package com.todotxt.todotxttouch;

import java.io.IOException;
import java.nio.charset.Charset;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class NfcActivity extends Activity{

 	private static final String TAG = "TEST";
 	
 	private boolean mWriteMode;
 	private IntentFilter[] mWriteTagFilters;
 	private NfcAdapter mNfcAdapter;
 	private PendingIntent mNfcPendingIntent;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);

		enableTagWriteMode();
		 
		new AlertDialog.Builder(NfcActivity.this).setTitle("Touch tag to write")
		    .setOnCancelListener(new DialogInterface.OnCancelListener() {
		        @Override
		        public void onCancel(DialogInterface dialog) {
		            //disableTagWriteMode();
		        }
		    }).create().show();
		
		//Create a PendingIntent to wait for NFC tag.
		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    	try {
    	    ndef.addDataType("*/*");
    	}
    	catch (MalformedMimeTypeException e) {
    	    throw new RuntimeException("fail", e);
    	}
    	
   		IntentFilter[] intentFiltersArray = new IntentFilter[] {ndef, };
   		System.out.println(intentFiltersArray.toString());
	}
	
	//Add an intent filter to discover NFC tags for writing data.
	private void enableTagWriteMode() {
	    mWriteMode = true;
	    IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
	    mWriteTagFilters = new IntentFilter[] { tagDetected };
	    mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
	}
	
	/*@Override
	protected void onNewIntent(Intent intent) {
	    // Tag writing mode
	    if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
	        Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
	        if (NfcUtils.writeTag(NfcUtils.getPlaceidAsNdef(placeidToWrite), detectedTag)) {
	            Toast.makeText(this, "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
	                .show();
	            NfcUtils.soundNotify(this);
	        } else {
	            Toast.makeText(this, "Write failed", Toast.LENGTH_LONG).show();
	        }
	    }
	}*/

	public void createTag(){
		//return NdefRecord.createMime("application/vnd.com.example.android.beam", "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));
	}

    public void writeTag(Tag tag, String tagText) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            ultralight.writePage(4, "abcd".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(5, "efgh".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(6, "ijkl".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(7, "mnop".getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while closing MifareUltralight: ", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight: ", e);
            }
        }
    }

    public String readTag(Tag tag) {
        MifareUltralight mifare = MifareUltralight.get(tag);
        try {
            mifare.connect();
            byte[] payload = mifare.readPages(4);
            return new String(payload, Charset.forName("US-ASCII"));
        } catch (IOException e) {
            Log.e(TAG, "IOException while writing MifareUltralight message:", e);
        } finally {
            if (mifare != null) {
               try {
                   mifare.close();
               }
               catch (IOException e) {
                   Log.e(TAG, "Error closing tag:", e);
               }
            }
        }
        return null;
    }
}