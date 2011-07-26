package com.coboltforge.dontmind.coboltfm;

import java.util.List;

import com.coboltforge.dontmind.coboltfm.R;


import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

public class EnterArtistNameActivity extends EnterNameActivity {
	
	public EnterArtistNameActivity() {
		super(R.layout.enter_station, "station", "Please enter some artist name");				
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	SharedPreferences settings = getSharedPreferences(PlayerActivity.PREFS_NAME, 0);		
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
	}

}
