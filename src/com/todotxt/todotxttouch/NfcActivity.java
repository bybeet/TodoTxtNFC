package com.todotxt.todotxttouch;

import java.io.IOException;
import java.nio.charset.Charset;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class NfcActivity extends Activity{

	private static final String TAG = "TEST";

	private boolean mWriteMode;
	private IntentFilter[] mWriteTagFilters;
	private NfcAdapter mNfcAdapter;
	private PendingIntent mNfcPendingIntent;

	private Button add;
	private Button filter;

	private String nfcTagType;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.nfc);

		nfcTagType = new String("none");

		add = (Button)findViewById(R.id.nfc_add);
		filter = (Button)findViewById(R.id.nfc_filter);

		add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nfcTagType = "add";
				callNfcAlert();
			}
		});

		filter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				nfcTagType = "filter";
				callNfcAlert();
			}
		});

		mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);



	}

	@Override
	public void onPause(){
		mNfcAdapter.disableForegroundDispatch(this);
		super.onPause();
	}

	@Override
	public void onResume(){
		super.onResume();
	}

	private void callNfcAlert(){
		enableTagWriteMode();
		new AlertDialog.Builder(NfcActivity.this).setTitle("Touch tag to write")
		.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				disableTagWriteMode();
			}
		}).create().show();
	}

	public void disableTagWriteMode(){
		mNfcAdapter.disableForegroundDispatch(this);
	}

	//Add an intent filter to discover NFC tags for writing data.
	private void enableTagWriteMode() {
		mWriteMode = true;
		IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// Tag writing mode
		if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			if (writeTag(getKeywordAsNdef("+mmdroid"), detectedTag)) {
				Toast.makeText(this, "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
				.show();
			} else {
				Toast.makeText(this, "Write failed", Toast.LENGTH_LONG).show();
			}
		}
	}

	public NdefRecord createTag(){
		return NdefRecord.createMime("application/vnd.com.example.android.beam", "Beam me up, Android".getBytes(Charset.forName("US-ASCII")));
	}

	public static boolean writeTag(NdefMessage message, Tag tag) {
		int size = message.toByteArray().length;
		try {
			Ndef ndef = Ndef.get(tag);
			if (ndef != null) {
				ndef.connect();
				if (!ndef.isWritable()) {
					return false;
				}
				if (ndef.getMaxSize() < size) {
					return false;
				}
				ndef.writeNdefMessage(message);
				return true;
			} else {
				NdefFormatable format = NdefFormatable.get(tag);
				if (format != null) {
					try {
						format.connect();
						format.format(message);
						return true;
					} catch (IOException e) {
						return false;
					}
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	public static NdefMessage getKeywordAsNdef(String msg) {
		byte[] textBytes = msg.getBytes();
		NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,
				"application/nfc.todotxtnfc.add".getBytes(), new byte[] {}, textBytes);
		return new NdefMessage(new NdefRecord[] { textRecord });
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