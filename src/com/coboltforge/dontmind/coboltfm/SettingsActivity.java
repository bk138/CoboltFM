package com.coboltforge.dontmind.coboltfm;

import com.coboltforge.dontmind.coboltfm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SettingsActivity extends Activity {
	protected static final int SET_USER_INFO = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		Button resetButton = (Button) findViewById(R.id.reset_button);

		resetButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsActivity.this)
						.setIcon(R.drawable.alert_dialog_icon)
						.setMessage(getString(R.string.reset_question))
						.setPositiveButton(R.string.alert_dialog_ok,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {
										SharedPreferences settings = getSharedPreferences(
												Constants.PREFSNAME, 0);
										SharedPreferences.Editor ed = settings
												.edit();
										ed.clear();
										ed.commit();
									}
								}).setNegativeButton(
								R.string.alert_dialog_cancel,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int whichButton) {

										/* User clicked Cancel so do some stuff */
									}
								}).show();
			}

		});

		Button userinfoButton = (Button) findViewById(R.id.userinfo_button);
		userinfoButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivityForResult(
						new Intent(SettingsActivity.this, UserInfoActivity.class),
						SET_USER_INFO);
			}

		});
		
		
		SeekBar preBufferSlider = (SeekBar) findViewById(R.id.prebuffer_slider);

		// connect textview
		preBufferSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
		{
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				TextView preBufferText = (TextView) findViewById(R.id.prebuffer_textview);
				preBufferText.setText(progress + " %");
			}
			

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
		});
		
		// set to saved or default value
		SharedPreferences settings = getSharedPreferences(
				Constants.PREFSNAME, 0);
		
		preBufferSlider.setProgress(settings.getInt("preBuffer", 5));
		
		CheckBox showSupportCheckBox = (CheckBox) findViewById(R.id.support_dialog_checkbox);
		showSupportCheckBox.setChecked(settings.getBoolean("showSupportDialog", true));
		
		CheckBox headphonePlugCheckBox = (CheckBox) findViewById(R.id.headphone_plug_checkbox);
		headphonePlugCheckBox.setChecked(settings.getBoolean("headphonePlugPause", true));
		
		CheckBox vibrateCheckBox = (CheckBox) findViewById(R.id.vibrate_checkbox);
		vibrateCheckBox.setChecked(settings.getBoolean("vibrateOnChange", false));
		
		CheckBox alternateConnCheckBox = (CheckBox) findViewById(R.id.alternate_conn_checkbox);
		alternateConnCheckBox.setChecked(settings.getBoolean("alternateConnMethod", false));
		
		CheckBox streamingCheckBox = (CheckBox) findViewById(R.id.streaming_checkbox);
		streamingCheckBox.setChecked(settings.getBoolean("useStreamProxy", ! StreamingMediaPlayer.isStreamingWorkingNatively()));

		CheckBox sleepTimerCheckBox = (CheckBox) findViewById(R.id.sleeptimer_checkbox);
		sleepTimerCheckBox.setChecked(settings.getBoolean("enableSleepTimer", false));
		sleepTimerCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				EditText sleepTimeEditText = (EditText) findViewById(R.id.sleep_edittext);
				sleepTimeEditText.setEnabled(isChecked);
				sleepTimeEditText.setFocusable(isChecked);
				sleepTimeEditText.setFocusableInTouchMode(isChecked);
			}
		});
	
		EditText sleepTimeEditText = (EditText) findViewById(R.id.sleep_edittext);
		sleepTimeEditText.setEnabled(sleepTimerCheckBox.isChecked());
		sleepTimeEditText.setFocusable(sleepTimerCheckBox.isChecked());
		sleepTimeEditText.setFocusableInTouchMode(sleepTimerCheckBox.isChecked());
		sleepTimeEditText.setText(Integer.toString(settings.getInt("sleepTime", 42)));
		
		CheckBox scrobblingCheckBox = (CheckBox) findViewById(R.id.scrobbling_checkbox);
		scrobblingCheckBox.setChecked(settings.getBoolean(Constants.PREFS_SCROBBLE, true));

		
	}
	
	
	protected void onPause()
	{
		super.onPause();
		
		SeekBar preBufferSlider = (SeekBar) findViewById(R.id.prebuffer_slider);
		CheckBox showSupportCheckBox = (CheckBox) findViewById(R.id.support_dialog_checkbox);
		CheckBox headphonePlugCheckBox = (CheckBox) findViewById(R.id.headphone_plug_checkbox);
		CheckBox vibrateCheckBox = (CheckBox) findViewById(R.id.vibrate_checkbox);
		CheckBox alternateConnCheckBox = (CheckBox) findViewById(R.id.alternate_conn_checkbox);
		CheckBox streamingCheckBox = (CheckBox) findViewById(R.id.streaming_checkbox);
		CheckBox sleepTimerCheckBox = (CheckBox) findViewById(R.id.sleeptimer_checkbox);
		EditText sleepTimeEditText = (EditText) findViewById(R.id.sleep_edittext);
		CheckBox scrobblingCheckBox = (CheckBox) findViewById(R.id.scrobbling_checkbox);

		SharedPreferences settings = getSharedPreferences(
				Constants.PREFSNAME, 0);
		Editor ed = settings.edit();
		ed.putInt("preBuffer", preBufferSlider.getProgress());
		ed.putBoolean("showSupportDialog", showSupportCheckBox.isChecked());
		ed.putBoolean("headphonePlugPause", headphonePlugCheckBox.isChecked());
		ed.putBoolean("vibrateOnChange", vibrateCheckBox.isChecked());
		ed.putBoolean("alternateConnMethod", alternateConnCheckBox.isChecked());
		ed.putBoolean("useStreamProxy", streamingCheckBox.isChecked());
		ed.putBoolean("enableSleepTimer", sleepTimerCheckBox.isChecked());
		try {
			ed.putInt("sleepTime", Integer.parseInt(sleepTimeEditText.getText().toString()));
		}
		catch(Exception e) {
		}
		ed.putBoolean(Constants.PREFS_SCROBBLE, scrobblingCheckBox.isChecked());


		ed.commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// app icon in action bar clicked; go home
			Intent intent = new Intent(this, PlayerActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
