package com.ajaxie.lastfm;

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
import android.widget.CompoundButton;
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
						new Intent(SettingsActivity.this, UserInfo.class),
						SET_USER_INFO);
			}

		});
		
		CheckBox altBufferCheckBox = (CheckBox) findViewById(R.id.alt_buffer);
		SharedPreferences settings = getSharedPreferences(
				PlayerActivity.PREFS_NAME, 0);
		
		altBufferCheckBox.setChecked(settings.getBoolean("muteOnCall", false));
		
		altBufferCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				SharedPreferences settings = getSharedPreferences(
						PlayerActivity.PREFS_NAME, 0);
				Editor ed = settings.edit();
				ed.putBoolean("altBuffer", isChecked);
				ed.commit();
			}			
		});

	}

}
