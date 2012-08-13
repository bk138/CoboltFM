package com.coboltforge.dontmind.coboltfm;

import com.coboltforge.dontmind.coboltfm.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class UserInfoActivity extends Activity {
	   @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.userinfo);
	        
	        final EditText usernameText = (EditText)findViewById(R.id.username);
	        final EditText passwordText = (EditText)findViewById(R.id.password);
	        
        	SharedPreferences settings = getSharedPreferences(Constants.PREFSNAME, 0);
	        usernameText.setText(settings.getString("username", ""));
	        passwordText.setText(settings.getString("password", ""));
	        
	        if (settings.getBoolean("username_invalid", false))
            	usernameText.setError("Last login attempt with this username failed");
	        
	        if (settings.getBoolean("password_invalid", false))
	        {
            	passwordText.setError("Last login attempt with this password failed");
	        }
	        	        
	        usernameText.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if (event.isPrintingKey())
						passwordText.setError(null);
					return false;
				}	   
	        });
	        
	        final Button okButton = (Button)findViewById(R.id.ok_button);
	        okButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                
	            	if (usernameText.getText().toString().equals("")) {
	                	usernameText.setError("Please enter a last.fm username");
	                	usernameText.requestFocus();
	                	return;
	                } 
	            	if (passwordText.getText().toString().equals("")) {
	            		passwordText.setError("Please enter a last.fm password");
	            		passwordText.requestFocus();
	                	return;
	                } 
	                
	            	SharedPreferences settings = getSharedPreferences(Constants.PREFSNAME, 0);
	            	SharedPreferences.Editor ed = settings.edit();
	            	ed.putString("username", usernameText.getText().toString());
	            	ed.putString("password", passwordText.getText().toString());
	            	ed.putBoolean("username_invalid", false);
	            	ed.putBoolean("password_invalid", false);
					ed.commit();	            	
	                setResult(RESULT_OK);
	                finish();
	            }
	        });      	        

	        final Button cancelButton = (Button)findViewById(R.id.cancel_button);
	        cancelButton.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	                setResult(RESULT_CANCELED);
	                finish();
	            }
	        });      	        
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
