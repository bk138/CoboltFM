package com.coboltforge.dontmind.coboltfm;

public class LastFMError extends Exception {
	String mMessage;
	
	public LastFMError(String message) {
		mMessage = message;
	}
	
	public String toString() {
		return "LastFM error: " + mMessage;
	}
}
