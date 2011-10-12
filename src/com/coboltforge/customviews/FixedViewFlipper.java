package com.coboltforge.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ViewFlipper;

public class FixedViewFlipper extends ViewFlipper {

	public FixedViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	// http://code.google.com/p/android/issues/detail?id=6191
	@Override
	protected void onDetachedFromWindow() {
		try {
			super.onDetachedFromWindow();
		}
		catch (IllegalArgumentException e) {
			// Call stopFlipping() in order to kick off updateRunning()
			stopFlipping();
		}
	}
	
}