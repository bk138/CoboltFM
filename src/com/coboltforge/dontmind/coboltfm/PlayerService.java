package com.coboltforge.dontmind.coboltfm;

import java.util.ArrayList;

import com.coboltforge.dontmind.coboltfm.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Binder;
import android.os.IBinder;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PlayerService extends Service {
	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		PlayerService getService() {
			return PlayerService.this;
		}

		public boolean startPlaying(String stationUrl) {
			return PlayerService.this.startPlaying(stationUrl);
		}
	}

	public static class LastFMNotificationListener {
		public void onStartTrack(XSPFTrackInfo track) {
		}

		public void onBuffer(int percent) {
		}

		public void onLoved(boolean success, String message) {
		}

		public void onBanned(boolean success, String message) {
		}

		public void onShared(boolean success, String message) {
		}
	}

	private static final String TAG = "PlayerService";

	private final IBinder mBinder = new LocalBinder();

	private NotificationManager mNM;
	
	private BroadcastReceiver headsetPlugReceiver;

	private LastFMNotificationListener mLastFMNotificationListener = null;
	
	public void setLastFMNotificationListener(
			LastFMNotificationListener listener) {
		this.mLastFMNotificationListener = listener;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// handleIntent(intent);
		return mBinder;
	}

	private static int PLAYER_NOTIFICATIONS = 1;

	@Override
	public void onCreate() {
		mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	

		// listen for headphone plugs/unplugs
		headsetPlugReceiver = new BroadcastReceiver() {
			//@Override
			public void onReceive(Context context, Intent intent)
			{
				int state = intent.getIntExtra("state", 0);
				String name = intent.getStringExtra("name");
				Log.d(TAG, "Detected headphone '" + name  + (state == 0 ? "' unplug" : "' plug"));

				final SharedPreferences settings = getSharedPreferences(PlayerActivity.PREFS_NAME, 0);

				if(settings.getBoolean("headphonePlugPause", true)) {
					if(state == 0) //unplug
						pausePlaying(true);
					else // plug. also sent anew on rotation
						pausePlaying(false);
				}
			}
		};
		registerReceiver(headsetPlugReceiver, new IntentFilter(Intent.ACTION_HEADSET_PLUG));
	}

	@Override
	public void onDestroy() {
		stopPlaying();
		mNM.cancel(PLAYER_NOTIFICATIONS);
		
		unregisterReceiver(headsetPlugReceiver);
	}

	private void updateNotification(String text) {
		Notification notification = new Notification(R.drawable.play_small, null,
				System.currentTimeMillis());
		Intent startIntent = new Intent(this, PlayerActivity.class);
		startIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, startIntent, 0);

		notification.setLatestEventInfo(this, "CoboltFM Player", text,
				contentIntent);

		mNM.notify(PLAYER_NOTIFICATIONS, notification);
	}

	boolean mCurrentTrackLoved = false;
	boolean mCurrentTrackBanned = false;

	public boolean isCurrentTrackLoved() {
		return mCurrentTrackLoved;
	}

	public boolean isCurrentTrackBanned() {
		return mCurrentTrackBanned;
	}

	public class ServiceNotificationListener extends LastFMNotificationListener {

		LastFMNotificationListener mUserListener;

		public ServiceNotificationListener(
				LastFMNotificationListener userListener) {
			mUserListener = userListener;
		}

		@Override
		public void onStartTrack(XSPFTrackInfo track) {
			updateNotification(track.getTitle() + " by " + track.getCreator());
			mCurrentTrackLoved = false;
			mCurrentTrackBanned = false;
			mCurrentStatus = new PlayingStatus(0, track);
			if (mUserListener != null)
				mUserListener.onStartTrack(track);
		}

		@Override
		public void onBanned(boolean success, String message) {
			if (mUserListener != null)
				mUserListener.onBanned(success, message);
		}

		@Override
		public void onBuffer(int percent) {
			if (mCurrentStatus instanceof LoggingInStatus){
				mCurrentStatus = new ConnectingStatus();
			}
		}

		@Override
		public void onLoved(boolean success, String message) {
			if (mUserListener != null)
				mUserListener.onLoved(success, message);
		}

		@Override
		public void onShared(boolean success, String message) {
			if (mUserListener != null)
				mUserListener.onShared(success, message);
		}
	}

	public interface Status {
		public String toString();
	}

	public class PlayingStatus implements Status {
		int buffered;
		int next_buffered;
		int position;
		boolean is_paused;
		XSPFTrackInfo trackInfo;

		public PlayingStatus(int currentPosition, XSPFTrackInfo currentTrack) {
			this.position = currentPosition;
			this.trackInfo = currentTrack;
		}
		
		public String toString() {
			return "playing";
		}

		public int getCurrentPosition() {
			return position;
		}
		
		public int getCurrentBuffered() {
			return buffered;
		}
		
		public int getNextBuffered() {
			return next_buffered;
		}

		public boolean getIsActuallyPaused() {
			return is_paused;
		}
		
		public XSPFTrackInfo getCurrentTrack() {
			return trackInfo;
		}

		public void setCurrentPosition(int currentPosition) {
			position = currentPosition;
		}
		
		public void setCurrentBuffered(int currentBuffered) {
			buffered = currentBuffered;
		}
		
		public void setNextBuffered(int nextBuffered) {
			next_buffered = nextBuffered;
		}
		public void setPause(boolean pause) {
			is_paused = pause;
		}
	}

	public class StoppedStatus implements Status {
		public String toString() {
			return "stopped";
		}
	}

	public class ConnectingStatus implements Status {
		public String toString() {
			return "connecting..";
		}
	}

	public class LoggingInStatus implements Status {
		public String toString() {
			return "connecting..";
		}
	}

	public class ErrorStatus implements Status {
		LastFMError mErr = null;

		public ErrorStatus(LastFMError err) {
			mErr = err;
		}

		public String toString() {
			if (mErr == null)
				return "Unknown error";
			else
				return mErr.toString();
		}
		
		public LastFMError getError() {
			return mErr;
		}
	}

	Status mCurrentStatus;

	private PlayerThread mPlayerThread;

	public void setCurrentStatus(Status status) {
		mCurrentStatus = status;
	}

	public Status getCurrentStatus() {
		try {
			if (mPlayerThread.getError() != null)
				mCurrentStatus = new ErrorStatus(mPlayerThread.getError());
			else {
				Status curStatus = mCurrentStatus;
				if (curStatus instanceof PlayingStatus)
				{
					((PlayingStatus) curStatus)
					.setCurrentPosition(mPlayerThread
							.getCurrentPosition());
					((PlayingStatus) curStatus)
					.setCurrentBuffered(mPlayerThread
							.getCurrentBuffered());
					((PlayingStatus) curStatus)
					.setNextBuffered(mPlayerThread
							.getNextBuffered());
					((PlayingStatus) curStatus)
					.setPause(mPlayerThread
							.getIsPaused());
				}
			}
		}
		catch(NullPointerException e) {
		}

		return mCurrentStatus;
	}

	public boolean stopPlaying() {
		try {
			Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_STOP).sendToTarget();
			mPlayerThread.interrupt();
			mPlayerThread = null;
			mCurrentStatus = new StoppedStatus();
			updateNotification("Stopped");
			return true;
		}
		catch(NullPointerException e) {
			return true;
		}
	}

	public boolean pausePlaying(boolean pause) {
		try {
			if(pause)
				Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_PAUSE).sendToTarget();
			else
				Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_UNPAUSE).sendToTarget();
			return true;
		}
		catch(NullPointerException e) {
			return false;
		}
	}
	
	public boolean setPreBuffer(int percent)
	{
		try	{
			mPlayerThread.setPreBuffer(percent);
			return true;
		}
		catch(NullPointerException e) {
			return false;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		handleIntent(intent);
	}

	void handleIntent(Intent intent) {
		if (intent.getAction() == null)
			return;
		if (intent.getAction().equals(Intent.ACTION_VIEW)) {
			startPlaying(intent.getDataString());
		} else
			Log.e(TAG, "Invalid service intent action: " + intent.getAction());
	}

	PhoneStateListener mPhoneStateListener = new PhoneStateListener()
	{
		public void onCallStateChanged(int state, String incomingNumber) {					
			if (state == TelephonyManager.CALL_STATE_IDLE)
			{
				try {
					mPlayerThread.unmute();
				}
				catch(NullPointerException e) {
				}
			} 
			else
			{
				try {
					mPlayerThread.mute();						
				}
				catch(NullPointerException e) {
				}
			}
		}
	}
	;
	public boolean startPlaying(String url) {
			if (mPlayerThread != null)
				stopPlaying();

			SharedPreferences settings = getSharedPreferences(
					PlayerActivity.PREFS_NAME, 0);
			
			TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
			tm.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
			
			String username = settings.getString("username", null);
			String password = settings.getString("password", null);
			int preBuffer = settings.getInt("preBuffer", 5);
			
			mPlayerThread = new PlayerThread(getApplicationContext(), username, password, preBuffer);
			try {
				mPlayerThread.setVersionString(getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
			}
			
			mPlayerThread
					.setLastFMNotificationListener(new ServiceNotificationListener(
							mLastFMNotificationListener));
			mPlayerThread.start();
			mPlayerThread.mInitLock.block();
			Message m = Message.obtain(mPlayerThread.mHandler,
					PlayerThread.MESSAGE_ADJUST, url);
			m.sendToTarget();

			Message.obtain(mPlayerThread.mHandler,
					PlayerThread.MESSAGE_CACHE_FRIENDS_LIST).sendToTarget();
			updateNotification("Starting playback");
			mCurrentStatus = new LoggingInStatus();
			return true;
	}

	public boolean skipCurrentTrack() {
		try {
			Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_SKIP).sendToTarget();
			return true;
		} 
		catch(NullPointerException e) {
			return false;
		}
	}

	public boolean loveCurrentTrack() {
		try {
			mCurrentTrackLoved = true;
			Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_LOVE).sendToTarget();
			return true;
		} 
		catch(NullPointerException e) {
			return false;
		}
	}

	public final ArrayList<FriendInfo> getFriendsList() {
		try {
			return mPlayerThread.getFriendsList();
		}
		catch(NullPointerException e) {
			return null;
		}
	}

	public boolean shareTrack(XSPFTrackInfo track, String recipient, String message) {
		try {
			PlayerThread.TrackShareParams msgParams = new PlayerThread.TrackShareParams(
					track, recipient, message, "en");
			Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_SHARE,
					msgParams).sendToTarget();
			return true;
		}
		catch(NullPointerException e) {
			return false;
		}
	}

	public boolean banCurrentTrack() {
		try {
			mCurrentTrackBanned = true;
			Message.obtain(mPlayerThread.mHandler, PlayerThread.MESSAGE_BAN)
			.sendToTarget();
			return true;
		}
		catch(NullPointerException e) {
			return false;
		}
	}

}
