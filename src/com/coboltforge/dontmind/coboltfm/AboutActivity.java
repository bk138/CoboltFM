package com.coboltforge.dontmind.coboltfm;

import java.math.BigDecimal;

import com.coboltforge.dontmind.coboltfm.R;
import com.paypal.android.MEP.CheckoutButton;
import com.paypal.android.MEP.PayPal;
import com.paypal.android.MEP.PayPalActivity;
import com.paypal.android.MEP.PayPalPayment;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AboutActivity extends Activity {

	private static final int PAYPAL_RETURNCODE = 666;
	CheckoutButton donateButton;
	
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
			
			// just in case this failed in the main activity
			PlayerActivity.initPaypal(getApplicationContext());
			
			if (PayPal.getInstance() != null && PayPal.getInstance().isLibraryInitialized()) {
				PayPal pp = PayPal.getInstance();
				// Get the CheckoutButton. There are five different sizes. 
				// The text on the button can either be of type TEXT_PAY or TEXT_DONATE.
				donateButton = pp.getCheckoutButton(this, PayPal.BUTTON_194x37, CheckoutButton.TEXT_DONATE);
				donateButton.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
				donateButton.setGravity(Gravity.CENTER_HORIZONTAL);
				donateButton.setOnClickListener(new View.OnClickListener() {
					public void onClick(View v) {
						PayPalPayment donation = new PayPalPayment();
						donation.setSubtotal(new BigDecimal("1.0"));
						donation.setCurrencyType("EUR");
						donation.setRecipient("dontmind@freeshell.org");
						donation.setMerchantName(getString(R.string.app_name));
						Intent paypalIntent = PayPal.getInstance().checkout(donation, getApplicationContext());
						startActivityForResult(paypalIntent, PAYPAL_RETURNCODE);
					}
				});
				LinearLayout donateButtonContainer = new LinearLayout(this);
				donateButtonContainer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				donateButtonContainer.setGravity(Gravity.CENTER_HORIZONTAL);
				donateButtonContainer.setOrientation(LinearLayout.HORIZONTAL);
				donateButtonContainer.addView(donateButton);
				LinearLayout l = (LinearLayout) findViewById(R.id.about_layout);
				l.addView(donateButtonContainer);
			}
	   }

	   public void onActivityResult(int requestCode, int resultCode, Intent data) {

		   if(requestCode == PAYPAL_RETURNCODE)
		   {	    	
			   /**
			    * If you choose not to implement the PayPalResultDelegate, then you will receive the transaction results here.
			    * The resultCode will tell you how the transaction ended and other information can be pulled
			    * from the Intent using getStringExtra.
			    */
			   switch(resultCode)
			   {
			   case Activity.RESULT_OK:
				   Toast.makeText(this, getString(R.string.donation_successful), Toast.LENGTH_LONG).show();
				   break;
			   case Activity.RESULT_CANCELED:
				   Toast.makeText(this, getString(R.string.donation_cancelled), Toast.LENGTH_LONG).show();
				   break;
			   case PayPalActivity.RESULT_FAILURE:
				   Toast.makeText(this, data.getStringExtra(PayPalActivity.EXTRA_ERROR_MESSAGE), Toast.LENGTH_LONG).show();
			   }
			   
			   // important, otherwise button won't work a second time
			   donateButton.updateButton();

		   }
	   }

		
}
