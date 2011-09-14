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
						.setTitle(
								"Are you really want to reset all settings, including your saved last.fm username and password?")
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
	}
	
	
	protected void onPause()
	{
		super.onPause();
		
		SeekBar preBufferSlider = (SeekBar) findViewById(R.id.prebuffer_slider);
		SharedPreferences settings = getSharedPreferences(
				PlayerActivity.PREFS_NAME, 0);
		Editor ed = settings.edit();
		ed.putInt("preBuffer", preBufferSlider.getProgress());
		ed.commit();
	}

}
