package com.coboltforge.dontmind.coboltfm;

import java.util.Timer;
import java.util.TimerTask;

import com.coboltforge.dontmind.coboltfm.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ImageButton;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;
import android.widget.Gallery.LayoutParams;

public class PlayerActivity extends Activity {

	private static final int DIALOG_ERROR = 1;

	public static final String PREFS_NAME = "LastFMSettings";

	protected static final int SET_USER_INFO_AND_PLAY = 0;
	protected static final int SHARE_TRACK = 1;
	protected static final int TUNE = 2;
	protected static final int SET_USER_INFO_AND_TUNE = 3;
	protected static final int SET_USER_INFO = 4;
	protected static final int SETTINGS = 5;	

	public static final int MENU_SETTINGS_ID = Menu.FIRST;
	public static final int MENU_STATUS_ID = Menu.FIRST + 1;
	public static final int MENU_ABOUT_ID = Menu.FIRST + 2;
	

	protected static final String TAG = "PlayerActivity";

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, MENU_SETTINGS_ID, 0, R.string.menu_settings).setIcon(
				android.R.drawable.ic_menu_preferences);
		menu.add(0, MENU_STATUS_ID, 0, R.string.menu_status).setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0, MENU_ABOUT_ID, 0, R.string.menu_about).setIcon(
				android.R.drawable.ic_menu_help);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS_ID:
			startActivityForResult(
					new Intent(PlayerActivity.this, SettingsActivity.class), SETTINGS);
			return true;
		case MENU_ABOUT_ID:
			startActivity(new Intent(PlayerActivity.this, AboutActivity.class));
			return true;
	    case MENU_STATUS_ID:
	    	Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://status.last.fm"));
	    	startActivity(browserIntent);
	    	return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DIALOG_ERROR)
			return new AlertDialog.Builder(this).setIcon(
					R.drawable.alert_dialog_icon).setTitle(
					R.string.alert_dialog_single_choice).setPositiveButton(
					R.string.alert_dialog_ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {

							/* User clicked Yes so do some stuff */
						}
					}).setMessage("Error").create();

		return null;
	}

	private PlayerService mBoundService;

	public class LastFMServiceConnection implements ServiceConnection {
		public void onServiceConnected(ComponentName className, IBinder service) {
			// This is called when the connection with the service has been
			// established, giving us the service object we can use to
			// interact with the service. Because we have bound to a explicit
			// service that we know is running in our own process, we can
			// cast its IBinder to a concrete class and directly access it.
			mBoundService = ((PlayerService.LocalBinder) service).getService();
			onServiceStarted();
		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			// Because it is running in our same process, we should never
			// see this happen.
			mBoundService = null;
		}
	};

	class LastFMNotificationListener extends
			PlayerService.LastFMNotificationListener {

		@Override
		public void onStartTrack(XSPFTrackInfo trackInfo) {
			PlayerActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					final ImageButton skipButton = (ImageButton) findViewById(R.id.skip_button);
					skipButton.setEnabled(true);
				}
			});
		}

		@Override
		public void onLoved(final boolean success, final String message) {
			PlayerActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					if (success)
						Toast.makeText(PlayerActivity.this, R.string.track_loved,
								Toast.LENGTH_SHORT).show();
					else
						Toast.makeText(PlayerActivity.this,
								"Love failed: " + message, Toast.LENGTH_LONG)
								.show();
				}
			});
		}

		@Override
		public void onShared(final boolean success, final String message) {
			PlayerActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					if (success)
						Toast.makeText(PlayerActivity.this,
								R.string.track_shared, Toast.LENGTH_SHORT)
								.show();
					else
						Toast.makeText(PlayerActivity.this,
								"Share failed: " + message, Toast.LENGTH_LONG)
								.show();
				}
			});
		}

		@Override
		public void onBanned(final boolean success, final String message) {
			PlayerActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					if (success)
						Toast.makeText(PlayerActivity.this,
								R.string.track_banned, Toast.LENGTH_SHORT)
								.show();
					else
						Toast.makeText(PlayerActivity.this,
								"Ban failed: " + message, Toast.LENGTH_SHORT)
								.show();
				}
			});
		}

	}

	void resetSongInfoDisplay() {
		TextView timeText = (TextView) PlayerActivity.this
				.findViewById(R.id.time_counter);
		TextView bufferText = (TextView) PlayerActivity.this
                 .findViewById(R.id.buffer_display);
		TextView creatorText = (TextView) PlayerActivity.this
				.findViewById(R.id.creator_name_text);
		TextView albumText = (TextView) PlayerActivity.this
				.findViewById(R.id.album_name_text);
		TextView trackText = (TextView) PlayerActivity.this
				.findViewById(R.id.track_name_text);

		timeText.setText("");
		bufferText.setText("");
		creatorText.setText("");
		albumText.setText("");
		trackText.setText("");
	}

	void resetAlbumImage() {
		ImageSwitcher albumView = (ImageSwitcher) PlayerActivity.this
				.findViewById(R.id.album_view);
		albumView.setImageDrawable(null);
	}

	class StatusRefreshTask extends TimerTask {

		Bitmap prevBitmap;

		PlayerService.Status prevStatus;
		
		@Override
		public void run() {

			if (mBoundService != null) {
				final PlayerService.Status status = mBoundService
						.getCurrentStatus();

				if (status != null) {
					final String statusString = status.toString();
					PlayerActivity.this.runOnUiThread(new Runnable() {

						public void run() {
							if (mBoundService == null)
								return;
						
							TextView statusText = (TextView) PlayerActivity.this
									.findViewById(R.id.status_text);
							TextView timeText = (TextView) PlayerActivity.this
									.findViewById(R.id.time_counter);
							TextView bufferText = (TextView) PlayerActivity.this
							         .findViewById(R.id.buffer_display);
							TextView creatorText = (TextView) PlayerActivity.this
									.findViewById(R.id.creator_name_text);
							TextView albumText = (TextView) PlayerActivity.this
									.findViewById(R.id.album_name_text);
							TextView trackText = (TextView) PlayerActivity.this
									.findViewById(R.id.track_name_text);
							ImageSwitcher albumView = (ImageSwitcher) PlayerActivity.this
									.findViewById(R.id.album_view);

							TextView radioName = (TextView) PlayerActivity.this
									.findViewById(R.id.radio_name);

							if (status instanceof PlayerService.ConnectingStatus
									|| status instanceof PlayerService.LoggingInStatus)
							{
								SharedPreferences settings = getSharedPreferences(
										PREFS_NAME, 0);
								String username = settings.getString("username", null);

								Uri stationUri = getStationUri(settings);
								if (stationUri == null && username != null)
									stationUri = genDefaultUri(username);
								if (stationUri != null)
									radioName.setText(Utils.getUriDescription(stationUri));
								showLoadingBanner("Connecting..");
							}
							else
								showTimeDisplay();

							if (status instanceof PlayerService.ErrorStatus) {
								PlayerService.ErrorStatus err = (PlayerService.ErrorStatus) status;
								statusText.setText("Error");
								
								if (err.getError() instanceof PlayerThread.BadCredentialsError) {
									int badItem = ((PlayerThread.BadCredentialsError)err.getError()).getBadItem();
									
									if (badItem == PlayerThread.BadCredentialsError.BAD_USERNAME)
										showErrorMsg("Login failed: invalid username");
									else
										showErrorMsg("Login failed: invalid password");

									SharedPreferences settings = getSharedPreferences(
											PREFS_NAME, 0);
									
									if (status.getClass() != prevStatus.getClass())
									{
										SharedPreferences.Editor ed = settings
												.edit();
	
										if (badItem == PlayerThread.BadCredentialsError.BAD_USERNAME)
											ed.putBoolean("username_invalid", true);
										else
											ed.putBoolean("password_invalid", true);
										boolean res = ed.commit();
										Log.w(TAG, Boolean.toString(res));
									}
								} else 
									showErrorMsg(statusString);
							}
							
							else {
								final ImageButton loveButton = (ImageButton) findViewById(R.id.love_button);
								final ImageButton banButton = (ImageButton) findViewById(R.id.ban_button);
								final ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);

								loveButton.setEnabled(!mBoundService
										.isCurrentTrackLoved());
								banButton.setEnabled(!mBoundService
										.isCurrentTrackBanned());
								shareButton.setEnabled(true);

								statusText.setText(statusString);
							}

							if (status instanceof PlayerService.PlayingStatus) {
								int pos = ((PlayerService.PlayingStatus) status)
										.getCurrentPosition();
								int buffered = ((PlayerService.PlayingStatus) status)
										.getCurrentBuffered();
								final XSPFTrackInfo track = ((PlayerService.PlayingStatus) status)
										.getCurrentTrack();
								if (track != null) {
									int minutes = (track.getDuration() - pos)
											/ (1000 * 60);
									int seconds = ((track.getDuration() - pos) / 1000) % 60;

									timeText.setText(String.format(
											"%1$02d:%2$02d", minutes, seconds));
									
									bufferText.setText("(buffered "+ buffered + " %)");
									if(pos == 0) // pre-buffering
										bufferText.setTextColor(Color.RED);
									else 
										bufferText.setTextColor(Color.GREEN);

									creatorText.setText(track.getCreator());
									albumText.setText(track.getAlbum());
									if (prevBitmap != track.getBitmap()) {
										albumView
												.setImageDrawable(new BitmapDrawable(
														track.getBitmap()));
										prevBitmap = track.getBitmap();
									}
									trackText.setText(track.getTitle());
									radioName.setText(track.getStationName());
									
									// gets stuck sometimes when rotating
									final ImageButton skipButton = (ImageButton) findViewById(R.id.skip_button);
									skipButton.setEnabled(true);
								}
							} else {
								resetSongInfoDisplay();
								resetAlbumImage();
							}
							prevStatus = status;
						}
					});
				}
			}
		}
	}

	void bindToPlayerService() {
		if (!PlayerActivity.this.bindService(new Intent(PlayerActivity.this,
				PlayerService.class), new LastFMServiceConnection(),
				Context.BIND_AUTO_CREATE))
			PlayerActivity.this.showDialog(DIALOG_ERROR);
	}

	Timer refreshTimer;

	void showLoadingBanner(String text) {
		final TextView textView = (TextView) findViewById(R.id.loading_text);
		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.switcher);
		
		if (!text.equals(textView.getText().toString()))
		{
			textView.setText(text);
			textView.invalidate();
		}
		final ImageView img = (ImageView) findViewById(R.id.loading_image);
		if (switcher.getCurrentView().getId() != R.id.loading_container) {
			switcher.setDisplayedChild(1); // is the second child in layout!
			AnimationDrawable frameAnimation = (AnimationDrawable) img
					.getBackground();
			frameAnimation.start();
		}
	}

	void showTimeDisplay() {
		final ImageView img = (ImageView) findViewById(R.id.loading_image);
		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.switcher);
		if (switcher.getCurrentView().getId() != R.id.display_container) {
			switcher.setDisplayedChild(0); // is the first child in layout!
			AnimationDrawable frameAnimation = (AnimationDrawable) img
					.getBackground();
			frameAnimation.stop();
		}
	}
	
	void showErrorMsg(String text) {
		final TextView textView = (TextView) findViewById(R.id.error_text);
		ViewFlipper switcher = (ViewFlipper) findViewById(R.id.switcher);
		
		if (!text.equals(textView.getText().toString()))
		{
			textView.setText(text);
			textView.invalidate();
		}
		final ImageView img = (ImageView) findViewById(R.id.loading_image);
		if (switcher.getCurrentView().getId() != R.id.errormsg_container) {
			switcher.setDisplayedChild(2); // is the third child in layout!
			AnimationDrawable frameAnimation = (AnimationDrawable) img
					.getBackground();
			frameAnimation.start();
		}
	}
	
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		refreshTimer = new Timer();
		refreshTimer.scheduleAtFixedRate(new StatusRefreshTask(), 1000, 1000);

		TextView statusText = (TextView) PlayerActivity.this
				.findViewById(R.id.status_text);
		try {
			statusText.setText("version "
					+ getPackageManager().getPackageInfo(this.getPackageName(),
							0).versionName);
		} catch (NameNotFoundException e) {
			statusText.setText("Invalid version -- please check for update");
		}

		// Load the ImageView that will host the animation and
		// set its background to our AnimationDrawable XML resource.
		final ImageView img = (ImageView) findViewById(R.id.loading_image);
		img.setBackgroundResource(R.drawable.loading_animation);

		bindToPlayerService();

		final Button tuneButton = (Button) findViewById(R.id.tune_button);

		tuneButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

				String username = settings.getString("username", null);
				String password = settings.getString("password", null);
				if ((username == null || password == null)
						|| (username.length() == 0 || password.length() == 0))
					startActivityForResult(new Intent(PlayerActivity.this,
							UserInfoActivity.class), SET_USER_INFO_AND_TUNE);
				else
					startActivityForResult(new Intent(PlayerActivity.this,
							TuneActivity.class), TUNE);
			}
		});

		final ImageButton playButton = (ImageButton) findViewById(R.id.play_button);
		final View.OnClickListener onPlayClickListener = new View.OnClickListener() {
			public void onClick(View v) {
				if (mBoundService != null
						&& (mBoundService.getCurrentStatus() != null)
						&& !(mBoundService.getCurrentStatus() instanceof PlayerService.StoppedStatus)
						&& !(mBoundService.getCurrentStatus() instanceof PlayerService.ErrorStatus))
					return;

				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				String username = settings.getString("username", null);
				String password = settings.getString("password", null);
				boolean usernameInvalid = settings.getBoolean(
						"username_invalid", false);
				boolean passwordInvalid = settings.getBoolean(
						"password_invalid", false);

				if ((usernameInvalid || passwordInvalid || username == null || password == null)
						|| (username.length() == 0 || password.length() == 0))
					startActivityForResult(new Intent(PlayerActivity.this,
							UserInfoActivity.class), SET_USER_INFO_AND_PLAY);
				else {
					Uri stationUri = getStationUri(settings);
					if (stationUri == null)
						stationUri = genDefaultUri(username);

					TextView radioName = (TextView) PlayerActivity.this
						.findViewById(R.id.radio_name);
					radioName.setText(Utils.getUriDescription(stationUri));

					showLoadingBanner("Connecting..");
					Intent serviceIntent = new Intent();
					serviceIntent.setAction(Intent.ACTION_VIEW);
					serviceIntent.setClass(PlayerActivity.this,
							PlayerService.class);
					serviceIntent.setData(stationUri);

					/*
					 * bindToPlayerService();
					 * PlayerActivity.this.startService(serviceIntent);
					 */
					if (mBoundService == null) {
						if (!PlayerActivity.this.bindService(serviceIntent,
								new LastFMServiceConnection(),
								Context.BIND_AUTO_CREATE))
							PlayerActivity.this.showDialog(DIALOG_ERROR);
						PlayerActivity.this.startService(serviceIntent);
					} else {
						PlayerActivity.this.startService(serviceIntent);
						// mBoundService.startPlaying(stationUri.toString());
					}

					/*
					 * if (mBoundService != null)
					 * mBoundService.startPlaying(stationUrl);
					 */
				}
			}
		};
		playButton.setOnClickListener(onPlayClickListener);

		final ImageButton stopButton = (ImageButton) findViewById(R.id.stop_button);
		stopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mBoundService != null) {
					showLoadingBanner("Stopping..");
					mBoundService.stopPlaying();
					// PlayerActivity.this.unbindService(mConnection);
					mBoundService = null;
					Intent serviceIntent = new Intent(PlayerActivity.this,
							PlayerService.class);
					PlayerActivity.this.stopService(serviceIntent);
					resetSongInfoDisplay();
					resetAlbumImage();
					showTimeDisplay();
					TextView statusText = (TextView) PlayerActivity.this
							.findViewById(R.id.status_text);
					statusText.setText("Disconnected");
				}
			}
		});

		final ImageButton skipButton = (ImageButton) findViewById(R.id.skip_button);
		skipButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mBoundService == null)
					return;

				if ((mBoundService.getCurrentStatus() == null)
						|| !(mBoundService.getCurrentStatus() instanceof PlayerService.PlayingStatus))
					return;

				skipButton.setEnabled(false);
				mBoundService.skipCurrentTrack();
			}
		});

		final ImageButton banButton = (ImageButton) findViewById(R.id.ban_button);
		banButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mBoundService != null) {
					if (mBoundService.getCurrentStatus() instanceof PlayerService.PlayingStatus)
						if (mBoundService.banCurrentTrack())
							banButton.setEnabled(false);
				}
			}
		});

		final ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);
		shareButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mBoundService != null)
					if (mBoundService.getCurrentStatus() instanceof PlayerService.PlayingStatus)
						startActivity(new Intent(PlayerActivity.this,
								ShareTrackActivity.class));
			}
		});

		final ImageButton loveButton = (ImageButton) findViewById(R.id.love_button);
		loveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (mBoundService != null) {
					if (mBoundService.getCurrentStatus() instanceof PlayerService.PlayingStatus)
						if (mBoundService.loveCurrentTrack())
							loveButton.setEnabled(false);
				}
			}
		});

		ImageSwitcher albumView = (ImageSwitcher) PlayerActivity.this
				.findViewById(R.id.album_view);
		albumView.setFactory(new ViewSwitcher.ViewFactory() {

			public View makeView() {
				ImageView i = new ImageView(PlayerActivity.this);
				i.setBackgroundColor(0xFF000000);
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				i.setLayoutParams(new ImageSwitcher.LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				return i;
			}
		});
		albumView.setInAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_in));
		albumView.setOutAnimation(AnimationUtils.loadAnimation(this,
				android.R.anim.fade_out));

		if (savedInstanceState != null) {
			loveButton.setEnabled(savedInstanceState.getBoolean(
					"loveButton_enabled", true));
			banButton.setEnabled(savedInstanceState.getBoolean(
					"banButton_enabled", true));
			shareButton.setEnabled(savedInstanceState.getBoolean(
					"shareButton_enabled", true));
		}

	}

	protected void shareTrack(XSPFTrackInfo track) {
		startActivityForResult(new Intent(PlayerActivity.this,
				ShareTrackActivity.class), SHARE_TRACK);
	}

	public void onServiceStarted() {
		if (mBoundService != null) {
			mBoundService
					.setLastFMNotificationListener(new LastFMNotificationListener());
		}
	}

	public static Uri getStationUri(SharedPreferences settings) {
		String uriString = settings.getString("station_uri", null);
		if (uriString == null)
			return null;
		else
			return Uri.parse(uriString);
	}

	public static Uri genDefaultUri(String username) {
		Uri.Builder builder = new Uri.Builder();
		builder.scheme("lastfm");
		builder.authority("user");
		builder.appendPath(username);
		builder.appendPath("personal");
		builder.fragment("");
		builder.query("");
		return builder.build();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == SET_USER_INFO_AND_PLAY && resultCode == RESULT_OK) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String username = settings.getString("username", null);
			String password = settings.getString("password", null);
			if (username != null && password != null && username.length() != 0
					&& password.length() != 0) {

				Uri stationUri = getStationUri(settings);
				if (stationUri == null)
					stationUri = genDefaultUri(username);

				TextView radioName = (TextView) PlayerActivity.this
						.findViewById(R.id.radio_name);
				radioName.setText(Utils.getUriDescription(stationUri));
				showLoadingBanner("Connecting..");

				Intent serviceIntent = new Intent(Intent.ACTION_VIEW,
						stationUri, PlayerActivity.this, PlayerService.class);
				PlayerActivity.this.startService(serviceIntent);
			}
		}
		if (requestCode == SET_USER_INFO_AND_TUNE && resultCode == RESULT_OK) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String username = settings.getString("username", null);
			String password = settings.getString("password", null);
			if (username != null && password != null && username.length() != 0
					&& password.length() != 0)
				startActivityForResult(
						new Intent(PlayerActivity.this, TuneActivity.class), TUNE);
		}
		if (requestCode == TUNE && resultCode == RESULT_OK) {
			SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
			String username = settings.getString("username", null);
			String password = settings.getString("password", null);

			if (username != null && password != null && username.length() != 0
					&& password.length() != 0) {

				SharedPreferences.Editor ed = settings.edit();
				ed.putString("station_uri", data.getDataString());
				ed.commit();

				TextView radioName = (TextView) PlayerActivity.this
						.findViewById(R.id.radio_name);
				radioName.setText(Utils.getUriDescription(data.getData()));

				Intent serviceIntent = new Intent(Intent.ACTION_VIEW, data
						.getData(), PlayerActivity.this, PlayerService.class);

				showLoadingBanner("Connecting..");

				if (mBoundService == null) {
					if (!PlayerActivity.this.bindService(serviceIntent,
							new LastFMServiceConnection(),
							Context.BIND_AUTO_CREATE))
						PlayerActivity.this.showDialog(DIALOG_ERROR);
					PlayerActivity.this.startService(serviceIntent);
				} else {
					PlayerActivity.this.startService(serviceIntent);
				}

			}
		}
		
		// back from settings screen
		if (requestCode == SETTINGS) {
			if (mBoundService != null)  
			{
				SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
				mBoundService.setPreBuffer(settings.getInt("preBuffer", 5));
			}
		}
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		final ImageButton loveButton = (ImageButton) findViewById(R.id.love_button);
		final ImageButton banButton = (ImageButton) findViewById(R.id.ban_button);
		final ImageButton shareButton = (ImageButton) findViewById(R.id.share_button);

		outState.putBoolean("loveButton_enabled", loveButton.isEnabled());
		outState.putBoolean("banButton_enabled", banButton.isEnabled());
		outState.putBoolean("shareButton_enabled", shareButton.isEnabled());
	}

}