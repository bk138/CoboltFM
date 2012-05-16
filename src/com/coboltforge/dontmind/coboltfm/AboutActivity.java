package com.coboltforge.dontmind.coboltfm;

import com.google.ads.*;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class AboutActivity extends Activity {
	   @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.about);
	        
			TextView versionText = (TextView)findViewById(R.id.version);

			try {
				versionText.setText("Version " + getPackageManager().getPackageInfo(this.getPackageName(), 0).versionName);
			} catch (NameNotFoundException e) {
				versionText.setText("Invalid version -- please check for update");
			}
			
			ImageButton donateButton = (ImageButton) findViewById(R.id.paypal_button);
			donateButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=HKRTWKNKBKPKN"));
			    	startActivity(browserIntent);
				}
			});
			
		    // Look up the AdView as a resource and load a request.
			AdView adView = (AdView)this.findViewById(R.id.ad);
			adView.loadAd(new AdRequest());

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
