package com.coboltforge.dontmind.coboltfm;

public class LastFMError extends Exception {
	
	private static final long serialVersionUID = 3843510453119005704L;
	
	String mMessage;
	
	public LastFMError(String message) {
		mMessage = message;
	}
	
	public String toString() {
		return "LastFM error: " + mMessage;
	}
}
