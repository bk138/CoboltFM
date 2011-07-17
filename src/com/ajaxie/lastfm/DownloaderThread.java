package com.ajaxie.lastfm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import android.util.Log;


public class DownloaderThread extends Thread {

	String url;
	File dst;
	int totalKbDownloaded;


	public DownloaderThread(String url, File dst) {
		super();
		this.url = url;
		this.dst = dst;
	}

	
	public void run() {
		
		try {
			downloadData(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
    private void downloadData(String mediaUrl) throws IOException {
    	
    	URLConnection cn = new URL(mediaUrl).openConnection();   
        cn.connect();   
        InputStream stream = cn.getInputStream();
        if (stream == null) {
        	Log.e(getClass().getName(), "Unable to create InputStream for mediaUrl:" + mediaUrl);
        }
        
		// Just in case a prior deletion failed because our code crashed or something, we also delete any previously 
		// downloaded file to ensure we start fresh.  If you use this code, always delete 
		// no longer used downloads else you'll quickly fill up your hard disk memory.  Of course, you can also 
		// store any previously downloaded file in a separate data cache for instant replay if you wanted as well.
		if (dst.exists()) {
			dst.delete();
		}

		Log.d(getClass().getName(), "download of " + mediaUrl + " starting");
		
        FileOutputStream out = new FileOutputStream(dst);   
        byte buf[] = new byte[16384];
        int totalBytesRead = 0, incrementalBytesRead = 0;
        do {
        	int numread = stream.read(buf);   
            if (numread <= 0)   
                break;   
            out.write(buf, 0, numread);
            totalBytesRead += numread;
            incrementalBytesRead += numread;
            totalKbDownloaded = totalBytesRead/1000;
            
            Log.d(getClass().getName(), "downloaded " + totalKbDownloaded + " KB");
            
        } while (! isInterrupted());   
        
        stream.close();
        
        if(! isInterrupted())
        	Log.d(getClass().getName(), "download of " + mediaUrl + " done");
        else 
        	Log.d(getClass().getName(), "download of " + mediaUrl + " canceled");

    }  

  

    
	
}