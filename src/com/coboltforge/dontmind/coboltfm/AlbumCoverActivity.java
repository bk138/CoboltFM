package com.coboltforge.dontmind.coboltfm;

import com.coboltforge.dontmind.coboltfm.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class AlbumCoverActivity extends Activity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.albumcover);

		ImageView albumCover = (ImageView) findViewById(R.id.albumcoverview);
		Bitmap bitmap = (Bitmap)this.getIntent().getParcelableExtra("AlbumCover");
		albumCover.setImageBitmap(bitmap);
	}
}
