package com.todotxt.todotxttouch;

import android.app.Activity;
import android.*;
import android.os.Bundle;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.util.Log;
import java.io.IOException;
import java.nio.charset.Charset;
import android.app.*;

public class NfcActivity extends Activity{

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);

		//Create a PendingIntent to wait for NFC tag.
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

		IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
    	try {
    	    ndef.addDataType("*/*");
    	}
    	catch (MalformedMimeTypeException e) {
    	    throw new RuntimeException("fail", e);
    	}
   		intentFiltersArray = new IntentFilter[] {ndef, };
	}

	public void createTag(){
		NdefRecord mimeRecord = NdefRecord.createMime("application/vnd.com.example.android.beam",
	    "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));
	}

    private static final String TAG = MifareUltralightTagTester.class.getSimpleName();

    public void writeTag(Tag tag, String tagText) {
        MifareUltralight ultralight = MifareUltralight.get(tag);
        try {
            ultralight.connect();
            ultralight.writePage(4, "abcd".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(5, "efgh".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(6, "ijkl".getBytes(Charset.forName("US-ASCII")));
            ultralight.writePage(7, "mnop".getBytes(Charset.forName("US-ASCII")));
        } catch (IOException e) {
            Log.e(TAG, "IOException while closing MifareUltralight...", e);
        } finally {
            try {
                ultralight.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException while closing MifareUltralight...", e);
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
            Log.e(TAG, "IOException while writing MifareUltralight
            message...", e);
        } finally {
            if (mifare != null) {
               try {
                   mifare.close();
               }
               catch (IOException e) {
                   Log.e(TAG, "Error closing tag...", e);
               }
            }
        }
        return null;
    }
}

}