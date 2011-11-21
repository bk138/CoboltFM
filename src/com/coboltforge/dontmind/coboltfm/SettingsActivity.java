package com.coboltforge.dontmind.coboltfm;

import com.coboltforge.dontmind.coboltfm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.TextView;

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
												PlayerActivity.PREFS_NAME, 0);
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
		
		// set do saved or default value
		SharedPreferences settings = getSharedPreferences(
				PlayerActivity.PREFS_NAME, 0);
		
		preBufferSlider.setProgress(settings.getInt("preBuffer", 5));
		
		CheckBox showSupportCheckBox = (CheckBox) findViewById(R.id.support_dialog_checkbox);
		showSupportCheckBox.setChecked(settings.getBoolean("showSupportDialog", true));
		
		CheckBox headphonePlugCheckBox = (CheckBox) findViewById(R.id.headphone_plug_checkbox);
		headphonePlugCheckBox.setChecked(settings.getBoolean("headphonePlugPause", true));
		
		CheckBox vibrateCheckBox = (CheckBox) findViewById(R.id.vibrate_checkbox);
		vibrateCheckBox.setChecked(settings.getBoolean("vibrateOnChange", false));
	}
	
	
	protected void onPause()
	{
		super.onPause();
		
		SeekBar preBufferSlider = (SeekBar) findViewById(R.id.prebuffer_slider);
		CheckBox showSupportCheckBox = (CheckBox) findViewById(R.id.support_dialog_checkbox);
		CheckBox headphonePlugCheckBox = (CheckBox) findViewById(R.id.headphone_plug_checkbox);
		CheckBox vibrateCheckBox = (CheckBox) findViewById(R.id.vibrate_checkbox);
		
		SharedPreferences settings = getSharedPreferences(
				PlayerActivity.PREFS_NAME, 0);
		Editor ed = settings.edit();
		ed.putInt("preBuffer", preBufferSlider.getProgress());
		ed.putBoolean("showSupportDialog", showSupportCheckBox.isChecked());
		ed.putBoolean("headphonePlugPause", headphonePlugCheckBox.isChecked());
		ed.putBoolean("vibrateOnChange", vibrateCheckBox.isChecked());

		ed.commit();
	}

}
