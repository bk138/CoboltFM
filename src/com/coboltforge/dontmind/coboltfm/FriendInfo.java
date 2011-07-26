package com.coboltforge.dontmind.coboltfm;

import org.w3c.dom.Element;

import com.coboltforge.dontmind.coboltfm.Utils.ParseException;

public class FriendInfo {
	
	private String mName;
	private String mProfileUrl;
	private String mAvatarUrl;

	public String getName() {
		return mName;
	}
	
	public String getAvatarUrl() {
		return mAvatarUrl;
	}

	public String getProfileUrl() {
		return mProfileUrl;
	}
	
	// important for AutoCompleteTextViews
	@Override 
	public String toString() {
		if (mName != null)
			return mName;
		else
			return "<null>";
	}

	public FriendInfo(Element element) throws ParseException{
		mName = element.getAttribute("username");
		mProfileUrl = Utils.getChildElement(element, "url");		
		mAvatarUrl = Utils.getChildElement(element, "image");		
}
}
