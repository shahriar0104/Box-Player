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
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
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

public class MainVideoActivity extends AppCompatActivity {

    private BetterVideoPlayer bvp;
    private String TAG = "BetterSample";

    private File file;
    private String sMedia="file://" , sSub="file://";
    Uri videoUri,subtitleUri;
    private String[] srtItems;
    private ListView lv_sub;
    private ArrayAdapter<String> subAdp;
    private ArrayList<File> myAV;
    private ArrayList<File> mySrt;
    private ImageView fullscreenButton;
    Bundle b,fullBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_video);

        /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }*/

        bvp = (BetterVideoPlayer) findViewById(R.id.bvp);
        fullscreenButton= (ImageView) findViewById(R.id.fulscreen_activity_button);

        Intent listVideo = getIntent();

        if (listVideo.hasExtra("videofilename")){
            b = listVideo.getExtras();
            //myAV = (ArrayList) b.getParcelableArrayList("videoList");
            //int position = b.getInt("pos",0);
            sMedia=b.getString("videofilename");
            sMedia="file://"+sMedia;
            //videoUri = Uri.parse(myAV.get(position).toString());
            //sMedia="file://"+videoUri.toString();
            bvp.setSource(Uri.parse(sMedia));
        }else if (listVideo.hasExtra("posFull")){
            fullBundle = listVideo.getExtras();
            int posFull = listVideo.getIntExtra("posFull",0);
            sMedia=listVideo.getStringExtra("mediaName");
            sSub=listVideo.getStringExtra("srtName");
            bvp.setSource(Uri.parse(sMedia));
            bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
            bvp.setInitialPosition(posFull);
        }else if (listVideo.hasExtra("uriLink")){
            sMedia = listVideo.getStringExtra("uriLink");
            bvp.setSource(Uri.parse(sMedia));
        }

        /*Intent full = getIntent();
        if (b != null){
            myAV = (ArrayList) b.getParcelableArrayList("videoList");
            int position = b.getInt("pos",0);
            videoUri = Uri.parse(myAV.get(position).toString());
            sMedia=sMedia+videoUri.toString();
            bvp.setSource(Uri.parse(sMedia));
        }

        else {
            int posFull = fullBundle.getInt("posFull",0);
            sMedia=fullBundle.getString("mediaName");
            //sSub=fullBundle.getString("srtName");
            bvp.setSource(Uri.parse(sMedia));
            //bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
            bvp.setInitialPosition(posFull);
        }*/

        //lv_sub=(ListView) findViewById(R.id.)
        /*findViewById(R.id.background_activity_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BackgroundActivity.class));
                //NotificationGenerator.customBigNotification(getApplicationContext());
            }
        });*/

        /*myAV = findAV(Environment.getExternalStorageDirectory());
        mySrt = findSrt(Environment.getExternalStorageDirectory());
        Uri localUri = Uri.parse(myAV.get(0).toString());
        Uri subtitleUri = Uri.parse(myAV.get(3).toString());
        sMedia="file:///storage/emulated/0/Download/Interstellar.2014.720p.BluRay.x264.YIFY.mp4";
        sSub="file:///storage/emulated/0/Download/Interstellar.2014.720p.BluRay.x264.YIFY.srt";*/

        /*Resources res = getResources();
        Drawable drawable = res.getDrawable(R.drawable.ic_refresh_black_24dp);
        bvp.setButtonDrawable(10, drawable);*/

        /*if (uri!=null){
            bvp.setAutoPlay(true);
            bvp.setSource(uri);
        }else {
            bvp.setAutoPlay(true);
            bvp.setSource(videoUri);
            //bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
        }*/

        /*bvp.setAutoPlay(false);
        bvp.setSource(Uri.parse(sMedia));*/
        /*if(savedInstanceState == null) {
            bvp.setAutoPlay(true);
            bvp.setSource(Uri.parse(sMedia));
            //bvp.setSource(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
            //bvp.setSource(localUri);
            //bvp.setCaptions(R.raw.sub, CaptionsView.CMime.SUBRIP);
            bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
        }*/
        mySrt = findSrt(Environment.getExternalStorageDirectory());
        srtItems =new String[mySrt.size()];
        for (int i=0; i<mySrt.size(); i++){
            //Toast.makeText(getApplicationContext(),myAV.get(i).getName().toString(),Toast.LENGTH_SHORT).show();
            srtItems[i] = mySrt.get(i).getName().toString();
        }
        subAdp = new ArrayAdapter<String>(getApplicationContext(),R.layout.subtitle_layout,R.id.textSub,srtItems);
        //lv.setAdapter(adp);

        /*findViewById(R.id.subtitle_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bvp.isPlaying()){
                    bvp.pause();
                }
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.subtitle_list);
                ListView lv = (ListView ) dialog.findViewById(R.id.lv_sub);
                lv.setAdapter(adp);
                dialog.setCancelable(true);
                dialog.setTitle("ListView");
                dialog.show();
                lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        Uri subtitleUri = Uri.parse(mySrt.get(i).toString());
                        sSub=sSub+subtitleUri.toString();
                        bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
                        bvp.start();
                        dialog.dismiss();
                    }
                });
            }
        });*/

        /*if (bvp.setOnClick()){
            startActivity(new Intent(MainActivity.this, FullscreenActivity.class).putExtra("pos",bvp.getCurrentPosition())
                    .putExtra("mediaName",sMedia).putExtra("srtName",sSub));
        }*/

        fullscreenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainActivity.this,FullscreenActivity.class));
                startActivity(new Intent(MainVideoActivity.this, FullscreenActivity.class).putExtra("posMain",bvp.getCurrentPosition())
                        .putExtra("mediaName",sMedia).putExtra("srtName",sSub));
                finish();
            }
        });

        bvp.setHideControlsOnPlay(true);

        bvp.getToolbar().inflateMenu(R.menu.menu_dizi);
        bvp.getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.action_sub:
                        if (bvp.isPlaying()){
                            bvp.pause();
                        }
                        final Dialog dialog = new Dialog(MainVideoActivity.this);
                        dialog.setContentView(R.layout.subtitle_list);
                        ListView lv = (ListView ) dialog.findViewById(R.id.lv_sub);
                        lv.setAdapter(subAdp);
                        dialog.setCancelable(true);
                        dialog.setTitle("ListView");
                        dialog.show();
                        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Uri subtitleUri = Uri.parse(mySrt.get(i).toString());
                                sSub=sSub+subtitleUri.toString();
                                bvp.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
                                bvp.start();
                                dialog.dismiss();
                            }
                        });
                        break;
                    case R.id.action_enable_swipe:
                        bvp.enableSwipeGestures();
                        break;
                    case R.id.action_disable_swipe:
                        bvp.disableSwipeGestures();
                        break;
                    case R.id.action_show_bottombar:
                        bvp.setBottomProgressBarVisibility(true);
                        break;
                    case R.id.action_hide_bottombar:
                        bvp.setBottomProgressBarVisibility(false);
                        break;
                }
                return false;
            }
        });


        bvp.enableSwipeGestures(getWindow());



        //RelativeLayout rl = (RelativeLayout) findViewById(R.id.relative1);

        /*rl.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    if (gone) {
                        fullscreenButton.setVisibility(View.VISIBLE);
                        gone = false;

                    }
                    if (gone == false){
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {

                                // hide your button here
                                fullscreenButton.setVisibility(View.GONE);
                            }
                        }, 2000);
                        gone = true;
                    }
                }
                return true;
            }
        });*/

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

    @Override
    public void onPause(){
        /*for (int i=0; i<myAV.size(); i++){
            if (sMedia.endsWith(".mp4")){
                bvp.pause();
                super.onPause();
            }else {
                super.onPause();
            }
        }*/
        bvp.pause();
        super.onPause();
    }

    public ArrayList<File> findAV(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                al.addAll(findAV(singleFile));
            }else {
                if (singleFile.getName().endsWith(".mp3") || singleFile.getName().endsWith(".mp4")){
                    al.add(singleFile);
                }
            }
        }
        return al;
    }

    public ArrayList<File> findSrt(File root) {
        ArrayList<File> srt = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                srt.addAll(findSrt(singleFile));
            }else {
                if (singleFile.getName().endsWith(".srt")){
                    srt.add(singleFile);
                }
            }
        }
        return srt;
    }
}
