package com.halilibo.sample;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.SeekBar;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class SongService extends Service implements AudioManager.OnAudioFocusChangeListener, SeekBar.OnSeekBarChangeListener{
	String LOG_CLASS = "SongService";
	public static MediaPlayer mp;
	int NOTIFICATION_ID = 1111;
	public static final String NOTIFY_PREVIOUS = "com.tutorialsface.audioplayer.previous";
	public static final String NOTIFY_DELETE = "com.tutorialsface.audioplayer.delete";
	public static final String NOTIFY_PAUSE = "com.tutorialsface.audioplayer.pause";
	public static final String NOTIFY_PLAY = "com.tutorialsface.audioplayer.play";
	public static final String NOTIFY_NEXT = "com.tutorialsface.audioplayer.next";
	
	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	AudioManager audioManager;
	Bitmap mDummyAlbumArt;
	private static Timer timer; 
	private static boolean currentVersionSupportBigNotification = false;
	private static boolean currentVersionSupportLockScreenControls = false;
	String songPath,songName=null,albumName;
	int progressFromSeek=0,progress;
	float speed = 0.5f;
	String musicPlay=null;
	public static String sMedia=null;
	public boolean checkValue = false;

	public static WeakReference<SeekBar> songProgressBar;
	public static WeakReference<SeekBar> songAudioBar;
	static Handler progressBarHandler = new Handler();
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        currentVersionSupportBigNotification = UtilFunctions.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = UtilFunctions.currentVersionSupportLockScreenControls();
        timer = new Timer();
        mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Controls.nextControl(getApplicationContext());		
			}
		});
		super.onCreate();
	}

	/**
	 * Send message from timer
	 * @author jonty.ankit
	 */
	private class MainTask extends TimerTask{ 
        public void run(){
            handler.sendEmptyMessage(0);
        }
    } 
	
	 private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
        	if(mp != null){
        		progress = (mp.getCurrentPosition()*100) / mp.getDuration();
        		if (progressFromSeek>progress){
        			progress=progressFromSeek;
				}else {
        			progress=progress;
				}
        		Integer i[] = new Integer[3];
				i[0] = mp.getCurrentPosition();
        		i[1] = mp.getDuration();
        		i[2] = progress;
        		progressFromSeek=0;
        		try{
        			PlayerConstants.PROGRESSBAR_HANDLER.sendMessage(PlayerConstants.PROGRESSBAR_HANDLER.obtainMessage(0, i));
        		}catch(Exception e){}
        	}
    	}
    };

	public void updateProgressBar(){
		try{
			progressBarHandler.postDelayed(mUpdateTimeTask, 100);
		}catch(Exception e){

		}
	}

	static Runnable mUpdateTimeTask = new Runnable() {
		public void run(){
			long totalDuration = 0;
			long currentDuration = 0;

			try {
				totalDuration = mp.getDuration();
				currentDuration = mp.getCurrentPosition();
				int progress = (int)(getProgressPercentage(currentDuration, totalDuration));
				songProgressBar.get().setProgress(progress); /* Running this thread after 100 milliseconds */
				//songAudioBar.get().setProgress(progress);
				progressBarHandler.postDelayed(this, 100);

			} catch(Exception e){
				e.printStackTrace();
			}

		}
	};
	    
    @SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
    	initUI();
    	if (intent.hasExtra("uriLink")){
			songName =(String) intent.getExtras().get("uriLink");
		}
		try {
			if(PlayerConstants.SONGS_LIST.size() <= 0){
				PlayerConstants.SONGS_LIST = UtilFunctions.listOfSongs(getApplicationContext());
			}
			MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
			if(currentVersionSupportLockScreenControls){
				RegisterRemoteClient();
			}
			/*musicPlay = "file://"+(String) intent.getExtras().get("uriLink");
			if (musicPlay!=null)
				songPath=musicPlay;
			else {
				songPath = data.getPath();
			}*/
			if (songName!=null){
				songPath=songName;
				playSD(songPath);
			}else {
				songPath = data.getPath();
				playSong(songPath, data);
			}
			newNotification();
			PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					MediaItem data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
					songPath = data.getPath();
					newNotification();
					try{
						playSong(songPath, data);
						MainMusicActivity.changeUI();
						AudioPlayerActivity.changeUI();
					}catch(Exception e){
						e.printStackTrace();
					}
					return false;
				}
			});
			
			PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					String message = (String)msg.obj;
					if(mp == null)
						return false;
					if(message.equalsIgnoreCase(getResources().getString(R.string.play))){
						PlayerConstants.SONG_PAUSED = false;
						if(currentVersionSupportLockScreenControls){
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
						}
						mp.start();
					}else if(message.equalsIgnoreCase(getResources().getString(R.string.pause))){
						PlayerConstants.SONG_PAUSED = true;
						if(currentVersionSupportLockScreenControls){
							remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
						}
						mp.pause();
					}
					newNotification();
					try{
						MainMusicActivity.changeButton();
						AudioPlayerActivity.changeButton();
					}catch(Exception e){}
					Log.d("TAG", "TAG Pressed: " + message);
					return false;
				}
			});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return START_STICKY;
	}

	public void initUI() {
		songProgressBar = new WeakReference<>(MainMusicActivity.seekBar);
		songAudioBar = new WeakReference<>(AudioPlayerActivity.seekBar);
		songProgressBar.get().setOnSeekBarChangeListener(this);
		//songAudioBar.get().setOnSeekBarChangeListener(this);
	}
	public static int progressToTimer(int progress, int totalDuration) {
		int currentDuration = 0;
		totalDuration = mp.getDuration()/1000;
		currentDuration = (int) ((((double) progress) / 100) * totalDuration);

		// return current duration in milliseconds
		return currentDuration * 1000;
	}
	public static int getProgressPercentage(long currentDuration, long totalDuration) {
		Double percentage = (double) 0;
		long currentSeconds = (int) (currentDuration / 1000);
		long totalSeconds = (int) (totalDuration / 1000);

		// calculating percentage
		percentage = (((double) currentSeconds) / totalSeconds) * 100;

		// return percentage
		return percentage.intValue();
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
    	/*if (seekBar.equals(songProgressBar))
			progressBarHandler.removeCallbacks(mUpdateTimeTask);
    	else if (seekBar.equals(songAudioBar))*/
			progressBarHandler.removeCallbacks(mUpdateTimeTask);
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
    	/*if (seekBar.equals(songProgressBar)){
			progressBarHandler.removeCallbacks(mUpdateTimeTask);
			int totalDuration = mp.getDuration();
			int currentPosition = progressToTimer(seekBar.getProgress(),totalDuration);
			mp.seekTo(currentPosition);
			updateProgressBar();
		}else if (seekBar.equals(songAudioBar)){*/
			progressBarHandler.removeCallbacks(mUpdateTimeTask);
			int totalDuration = mp.getDuration();
			int currentPosition = progressToTimer(seekBar.getProgress(),totalDuration);
			mp.seekTo(currentPosition);
			updateProgressBar();
		//}
		/*int total = mp.getDuration();
		int currPosition =progressToTimer(seekBar.getProgress(),total);
		mp.seekTo(currPosition);
		songProgressBar.get().setProgress(currPosition);
		progressFromSeek=currPosition;*/
	}


	/**
	 * Notification
	 * Custom Bignotification is available from API 16
	 */
	@SuppressLint("NewApi")
	private void newNotification() {
		if (songName!=null){
		}else {
			songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getTitle();
			albumName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbum();
		}
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);

		Notification notification = new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(songName).build();

		setListeners(simpleContentView);
		setListeners(expandedView);
		
		notification.contentView = simpleContentView;
		if(currentVersionSupportBigNotification){
			notification.bigContentView = expandedView;
		}
		
		try{
			long albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getAlbumId();
			Bitmap albumArt = UtilFunctions.getAlbumart(getApplicationContext(), albumId);
			if(albumArt != null){
				notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				if(currentVersionSupportBigNotification){
					notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, albumArt);
				}
			}else{
				notification.contentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
				if(currentVersionSupportBigNotification){
					notification.bigContentView.setImageViewResource(R.id.imageViewAlbumArt, R.drawable.default_album_art);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		if(PlayerConstants.SONG_PAUSED){
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		}else{
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}

		notification.contentView.setTextViewText(R.id.textSongName, songName);
		notification.contentView.setTextViewText(R.id.textAlbumName, albumName);
		if(currentVersionSupportBigNotification){
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
			notification.bigContentView.setTextViewText(R.id.textAlbumName, albumName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}
	
	/**
	 * Notification click listeners
	 * @param view
	 */
	public void setListeners(RemoteViews view) {
		Intent previous = new Intent(NOTIFY_PREVIOUS);
		Intent delete = new Intent(NOTIFY_DELETE);
		Intent pause = new Intent(NOTIFY_PAUSE);
		Intent next = new Intent(NOTIFY_NEXT);
		Intent play = new Intent(NOTIFY_PLAY);
		
		PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

		PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, pDelete);
		
		PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pPause);
		
		PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, pNext);
		
		PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

	}
	
	@Override
	public void onDestroy() {
		if(mp != null){
			mp.stop();
			mp = null;
		}
		super.onDestroy();
	}

	/**
	 * Play song, Update Lockscreen fields
	 * @param songPath
	 * @param data
	 */
	@SuppressLint("NewApi")
	private void playSong(String songPath, MediaItem data) {
		try {
			if(currentVersionSupportLockScreenControls){
				if (songName!=null){
					audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
				}else {
					UpdateMetadata(data);
				}
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
			mp.reset();
			mp.setDataSource(songPath);
			mp.prepare();
			mp.start();
			updateProgressBar();
			/*int sdkVersion = android.os.Build.VERSION.SDK_INT;
			if(sdkVersion >= Build.VERSION_CODES.M){
				mp.setPlaybackParams(mp.getPlaybackParams().setSpeed(speed));
			}*/
			timer.scheduleAtFixedRate(new MainTask(), 0, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void playSD(String songpathSD){
		Uri u = Uri.parse(songpathSD);
		mp = MediaPlayer.create(getApplicationContext(),u);
		mp.start();
		updateProgressBar();
		timer.scheduleAtFixedRate(new MainTask(), 0, 100);
		songName=null;
	}
	@SuppressLint("NewApi")
	private void RegisterRemoteClient(){
		remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
		 try {
		   if(remoteControlClient == null) {
			   audioManager.registerMediaButtonEventReceiver(remoteComponentName);
			   Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			   mediaButtonIntent.setComponent(remoteComponentName);
			   PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
			   remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			   audioManager.registerRemoteControlClient(remoteControlClient);
		   }
		   remoteControlClient.setTransportControlFlags(
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
				   RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_STOP |
				   RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
				   RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
	  }catch(Exception ex) {
	  }
	}
	
	@SuppressLint("NewApi")
	private void UpdateMetadata(MediaItem data){
		if (remoteControlClient == null)
			return;
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.getAlbum());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.getArtist());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.getTitle());
		mDummyAlbumArt = UtilFunctions.getAlbumart(getApplicationContext(), data.getAlbumId());
		if(mDummyAlbumArt == null){
			mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
		}
		metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
		metadataEditor.apply();
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {}
}