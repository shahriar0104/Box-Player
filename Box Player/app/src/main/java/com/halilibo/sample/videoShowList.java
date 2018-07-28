package com.halilibo.sample;

import android.widget.ImageView;

/**
 * Created by Anik on 1/18/2018.
 */

public class videoShowList {
    public ImageView thmbImage;
    public String videoText;

    public videoShowList (ImageView thmbImage , String videoText){
        this.thmbImage=thmbImage;
        this.videoText=videoText;
    }

    public ImageView getThmbImage() {
        return thmbImage;
    }

    public String getVideoText() {
        return videoText;
    }

    public void setThmbImage(ImageView thmbImage) {
        this.thmbImage = thmbImage;
    }

    public void setVideoText(String videoText) {
        this.videoText = videoText;
    }
}
