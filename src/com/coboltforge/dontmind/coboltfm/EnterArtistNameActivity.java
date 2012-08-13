package com.coboltforge.dontmind.coboltfm;

import java.util.List;

import com.coboltforge.dontmind.coboltfm.R;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;

public class EnterArtistNameActivity extends EnterNameActivity {
	
	public EnterArtistNameActivity() {
		super(R.layout.enter_station, "station");
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	SharedPreferences settings = getSharedPreferences(Constants.PREFSNAME, 0);		
        Uri stationUri = PlayerActivity.getStationUri(settings);
        
        if (stationUri != null)
        {
        	List<String> path = stationUri.getPathSegments();
        	if (stationUri.getScheme().equals("lastfm")) {
    			if (stationUri.getAuthority().equals("artist") && path.size() > 0)
        			super.setDefaultName(path.get(0));
        	}
        }
		
		super.onCreate(savedInstanceState);
		
		setHint(getApplicationContext().getString(R.string.artistmissing));
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
