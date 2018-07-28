package com.halilibo.sample;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.halilibo.bettervideoplayer.BetterVideoCallback;
import com.halilibo.bettervideoplayer.BetterVideoPlayer;
import com.halilibo.bettervideoplayer.subtitle.CaptionsView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Button videoActivity,audioActivity;
    private ImageView imageVideo,imageAudio;
    private BetterVideoPlayer bvp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        //getActionBar().hide();
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        videoActivity=(Button) findViewById(R.id.videoActivity);
        audioActivity=(Button) findViewById(R.id.audioActivity);
        imageVideo=(ImageView) findViewById(R.id.imageVideo);
        imageAudio=(ImageView) findViewById(R.id.imageAudio);

        videoActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,VideoList.class));
            }
        });
        audioActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,MainMusicActivity.class));
            }
        });
        imageVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,VideoList.class));
            }
        });
        imageAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,MainMusicActivity.class));
            }
        });


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
            String checker= uri.toString();
            /*if (checker.endsWith(".mp3") ||checker.endsWith(".MP3") || checker.endsWith(".wav") || checker.endsWith(".WAV")){
                startActivity(new Intent(getApplicationContext(),MainMusicActivity.class).putExtra("uriLink",uri.toString()));
                finish();
                //Intent ir = new Intent(getApplicationContext(),SongService.class).putExtra("uriLink",uri.toString());
            }else{
                startActivity(new Intent(MainActivity.this,MainVideoActivity.class).putExtra("uriLink",uri.toString()));
                //finish();
            }*/
            //if (!checker.endsWith(".mp3") || !checker.endsWith(".MP3") || !checker.endsWith(".wav") || !checker.endsWith(".WAV"))
            boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
            if (isServiceRunning){
                Intent stopIntent = new Intent(getApplicationContext(),SongService.class);
                stopService(stopIntent);
            }
            startActivity(new Intent(MainActivity.this,MainVideoActivity.class).putExtra("uriLink",uri.toString()));
            //bvp.setAutoPlay(true);
            //bvp.setSource(uri);
        }/*else {
            bvp.setAutoPlay(true);
            bvp.setSource(videoUri);
            //bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
        }*/

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                Toast.makeText(this, "please grant read permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.settings:
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }*/
}
