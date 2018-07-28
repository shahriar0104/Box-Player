package com.halilibo.sample;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static com.halilibo.sample.SongService.mUpdateTimeTask;

public class MainMusicActivity extends Activity {
	String LOG_CLASS = "MainMusicActivity";
	CustomAdapter customAdapter = null;
	static TextView playingSong;
	Button btnPlayer;
	static Button btnPause, btnPlay, btnNext, btnPrevious;
	Button btnStop;
	LinearLayout mediaLayout;
	static LinearLayout linearLayoutPlayingSong;
	ListView mediaListView;
	public static SeekBar seekBar;
	public static TextView textBufferDuration, textDuration;
	static ImageView imageViewAlbumArt;
	static Context context;
	public static String sMedia=null,chksMedia;
	static Handler progressBarHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getActionBar().hide();
		setContentView(R.layout.activity_music_main);
		/*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
		}*/
		context = MainMusicActivity.this;
		Uri uri = null;
		if (getIntent().getData() != null) {
			// The intent-filter probably caught an url, open it...
			uri = getIntent().getData();
		} else {
			String savedUriString = PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
					.getString("lastUri", "");
			if(!"".equals(savedUriString)) {
				uri = Uri.parse(savedUriString);
			}
		}
		if (uri!=null){
			sMedia=uri.toString();
		}
		//Intent fromMain = getIntent();
		//sMedia=fromMain.getStringExtra("uriLink");
		init();
	}

	/*@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == 1) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				// Permission Granted
			} else {
				Toast.makeText(this, "please grant read permission", Toast.LENGTH_SHORT).show();
			}
		}
	}*/

	private void init() {
		getViews();
		setListeners();
		playingSong.setSelected(true);
		seekBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_IN);
		if(PlayerConstants.SONGS_LIST.size() <= 0){
			PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
		}
		setListItems();
    }

	private void setListItems() {
		customAdapter = new CustomAdapter(this,R.layout.custom_list, PlayerConstants.SONGS_LIST);
		mediaListView.setAdapter(customAdapter);
		mediaListView.setFastScrollEnabled(true);
	}

	private void getViews() {
		playingSong = (TextView) findViewById(R.id.textNowPlaying);
		btnPlayer = (Button) findViewById(R.id.btnMusicPlayer);
		mediaListView = (ListView) findViewById(R.id.listViewMusic);
		mediaLayout = (LinearLayout) findViewById(R.id.linearLayoutMusicList);
		btnPause = (Button) findViewById(R.id.btnPause);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		linearLayoutPlayingSong = (LinearLayout) findViewById(R.id.linearLayoutPlayingSong);
		seekBar = (SeekBar) findViewById(R.id.progressBar);
		btnStop = (Button) findViewById(R.id.btnStop);
		textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
		textDuration = (TextView) findViewById(R.id.textDuration);
     	imageViewAlbumArt = (ImageView) findViewById(R.id.imageViewAlbumArt);
		btnNext = (Button) findViewById(R.id.btnNext);
		btnPrevious = (Button) findViewById(R.id.btnPrevious);
	}

	private void setListeners() {
		if (sMedia!=null){
			PlayerConstants.SONG_PAUSED=false;
			boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
			if (isServiceRunning){
				Intent i = new Intent(getApplicationContext(), SongService.class);
				stopService(i);
				Intent ir = new Intent(getApplicationContext(),SongService.class).putExtra("uriLink",sMedia);
				startService(ir);
			}else {
				Intent ir = new Intent(getApplicationContext(),SongService.class).putExtra("uriLink",sMedia);
				startService(ir);
			}
			changeUI();
			//sMedia=null;
		}
		 mediaListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View item, int position, long id){
            	Log.d("TAG", "TAG Tapped INOUT(IN)");
         		PlayerConstants.SONG_PAUSED = false;
         		PlayerConstants.SONG_NUMBER = position;
 				boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
 				if (!isServiceRunning) {
 					Intent i = new Intent(getApplicationContext(),SongService.class);
 					startService(i);
 				} else {
 					PlayerConstants.SONG_CHANGE_HANDLER.sendMessage(PlayerConstants.SONG_CHANGE_HANDLER.obtainMessage());
 				}
 				updateUI();
 				changeButton();
            	Log.d("TAG", "TAG Tapped INOUT(OUT)");
            }
        });

		btnPlayer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainMusicActivity.this,AudioPlayerActivity.class);
				startActivity(i);
			}
		});
		btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.playControl(getApplicationContext());
			}
		});
		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.pauseControl(getApplicationContext());
			}
		});
		btnNext.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Controls.nextControl(getApplicationContext());
			}
		});
		btnPrevious.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Controls.previousControl(getApplicationContext());
			}
		});
		btnStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getApplicationContext(), SongService.class);
				stopService(i);
				linearLayoutPlayingSong.setVisibility(View.GONE);
			}
		});
		imageViewAlbumArt.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(MainMusicActivity.this,AudioPlayerActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		try{
	    	boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
			if (isServiceRunning) {
				updateUI();
			}else{
 				linearLayoutPlayingSong.setVisibility(View.GONE);
			}
			changeButton();
			PlayerConstants.PROGRESSBAR_HANDLER = new Handler(){
				 @Override
			      public void handleMessage(Message msg){
					 Integer i[] = (Integer[])msg.obj;
					 textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
					 textDuration.setText(UtilFunctions.getDuration(i[1]));
					 //seekBar.setProgress(i[2]);
			    }
			};
			SongService service = new SongService();
			service.initUI();
		 }catch(Exception e){}
	}

	@SuppressWarnings("deprecation")
	public static void updateUI() {
		if (sMedia!=null){
			linearLayoutPlayingSong.setVisibility(View.VISIBLE);
			playingSong.setText(sMedia);
			imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
			sMedia=null;
			//changeUI();
		}
		else {
			try{
				MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
				playingSong.setText(data.getTitle() + " " + data.getArtist() + "-" + data.getAlbum());
				Bitmap albumArt = UtilFunctions.getAlbumart(context, data.getAlbumId());
				if(albumArt != null){
					imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(albumArt));
				}else{
					imageViewAlbumArt.setBackgroundDrawable(new BitmapDrawable(UtilFunctions.getDefaultAlbumArt(context)));
				}
				linearLayoutPlayingSong.setVisibility(View.VISIBLE);
			}catch(Exception e){}
		}
	}

	public static void changeButton() {
		if(PlayerConstants.SONG_PAUSED){
			btnPause.setVisibility(View.GONE);
			btnPlay.setVisibility(View.VISIBLE);
		}else{
			btnPause.setVisibility(View.VISIBLE);
			btnPlay.setVisibility(View.GONE);
		}
	}

	public static void changeUI(){
		updateUI();
		changeButton();
	}
	@Override
	protected void onUserLeaveHint()
	{
		//Log.d("onUserLeaveHint","Home button pressed");
		super.onBackPressed();
	}
}