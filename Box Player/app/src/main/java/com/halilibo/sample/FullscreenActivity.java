package com.halilibo.sample;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;

import com.halilibo.bettervideoplayer.BetterVideoPlayer;
import com.halilibo.bettervideoplayer.subtitle.CaptionsView;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private ImageView mainButton;
    private String sMedia="file://",sSub="file://",matchWord,s;
    private String[] srtItems;
    private ListView lv_sub;
    private ArrayAdapter<String> subAdp;
    private ArrayList<File> myAV;
    private ArrayList<File> mySrt;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private BetterVideoPlayer mBetterVideoPlayer;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mBetterVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        Intent i = getIntent();
        sMedia = i.getStringExtra("mediaName");
        int position =i.getIntExtra("posMain",0);
        sSub = i.getStringExtra("srtName");

        mVisible = true;
        mBetterVideoPlayer = (BetterVideoPlayer) findViewById(R.id.bvp);
        mBetterVideoPlayer.setInitialPosition(position);
        mBetterVideoPlayer.setSource(Uri.parse(sMedia));
        mBetterVideoPlayer.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);

        Pattern MY_PATTERN = Pattern.compile("//[(*?)]/*?.");
        Matcher m = MY_PATTERN.matcher(sMedia);
        while (m.find()) {
            s = m.group(1);
            // s now contains "BAR"
        }

        mBetterVideoPlayer.getToolbar().setTitle(s);
        mBetterVideoPlayer.getToolbar()
                .setNavigationIcon(android.support.v7.appcompat.R.drawable.abc_ic_ab_back_material);
        mBetterVideoPlayer.getToolbar().setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mainButton= (ImageView) findViewById(R.id.mainscreen_activity_button);
        mainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FullscreenActivity.this ,MainVideoActivity.class).putExtra("posFull",mBetterVideoPlayer.getCurrentPosition())
                        .putExtra("mediaName",sMedia).putExtra("srtName",sSub));
                finish();
            }
        });

        mySrt = findSrt(Environment.getExternalStorageDirectory());
        srtItems =new String[mySrt.size()];
        for (int j=0; j<mySrt.size(); j++){
            //Toast.makeText(getApplicationContext(),myAV.get(i).getName().toString(),Toast.LENGTH_SHORT).show();
            srtItems[j] = mySrt.get(j).getName().toString();
        }
        subAdp = new ArrayAdapter<String>(getApplicationContext(),R.layout.subtitle_layout,R.id.textSub,srtItems);

        mBetterVideoPlayer.getToolbar().inflateMenu(R.menu.menu_dizi);
        hide();
        mBetterVideoPlayer.getToolbar().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                hide();
                switch (item.getItemId()){
                    case R.id.action_sub:
                        if (mBetterVideoPlayer.isPlaying()){
                            mBetterVideoPlayer.pause();
                        }
                        final Dialog dialog = new Dialog(FullscreenActivity.this);
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
                                mBetterVideoPlayer.setCaptions(Uri.parse(sSub), CaptionsView.CMime.SUBRIP);
                                mBetterVideoPlayer.start();
                                dialog.dismiss();
                                hide();
                            }
                        });
                        break;
                    case R.id.action_enable_swipe:
                        mBetterVideoPlayer.enableSwipeGestures();
                        hide();
                        break;
                    case R.id.action_disable_swipe:
                        mBetterVideoPlayer.disableSwipeGestures();
                        hide();
                        break;
                    case R.id.action_show_bottombar:
                        mBetterVideoPlayer.setBottomProgressBarVisibility(true);
                        hide();
                        break;
                    case R.id.action_hide_bottombar:
                        mBetterVideoPlayer.setBottomProgressBarVisibility(false);
                        hide();
                        break;
                    default:
                        hide();
                        break;
                }
                hide();
                return false;
            }
        });

        mBetterVideoPlayer.enableSwipeGestures(getWindow());

        // Set up the user interaction to manually show or hide the system UI.
        /*mBetterVideoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });*/

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
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

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mBetterVideoPlayer.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
