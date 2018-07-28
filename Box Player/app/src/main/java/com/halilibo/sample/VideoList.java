package com.halilibo.sample;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.util.ArrayList;

import static android.provider.MediaStore.Video.Thumbnails.MICRO_KIND;

public class VideoList extends AppCompatActivity {

    //private ListView videoItems;
    private File file;
    private String sMedia, sSub="file://";
    private String[] videoItems;
    private ImageView[] thmbImage;
    private ArrayAdapter<String> adp;
    private ArrayList<File> myAV;

    private Cursor videocursor;
    private int video_column_index;
    ListView videolist;
    int count;
    String[] thumbColumns = { MediaStore.Video.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.VIDEO_ID };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_list);

        //listViewVideo=(ListView) findViewById(R.id.listViewVideo);
        //myAV = findAV(Environment.getExternalStorageDirectory());
        init_phone_video_grid();

        /*videoItems  =new String[myAV.size()];
        for (int i = 0; i<myAV.size(); i++){
            videoItems[i] = myAV.get(i).getName().toString().replace(".mp4","").replace(".mkv","");
        }*/
        //adp=new ArrayAdapter<String>(getApplicationContext(),R.layout.av_layout,R.id.textList,videoItems);
        //listViewVideo.setAdapter(adp);

        /*listViewVideo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(getApplicationContext(),MainVideoActivity.class).putExtra("pos",i).putExtra("videoList",myAV));
            }
        });*/
    }

    @SuppressWarnings("deprecation")
    private void init_phone_video_grid() {
        System.gc();
        String[] proj = { MediaStore.Video.Media._ID,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DISPLAY_NAME,
                MediaStore.Video.Media.SIZE };
        videocursor = managedQuery(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                proj, null, null, null);
        count = videocursor.getCount();
        videolist = (ListView) findViewById(R.id.listViewVideo);
        videolist.setAdapter(new VideoAdapter(getApplicationContext()));
        videolist.setOnItemClickListener(videogridlistener);
    }

    private AdapterView.OnItemClickListener videogridlistener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position,
                                long id) {
            System.gc();
            video_column_index = videocursor
                    .getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            videocursor.moveToPosition(position);
            String filename = videocursor.getString(video_column_index);
            Intent intent = new Intent(VideoList.this,
                    MainVideoActivity.class);
            intent.putExtra("videofilename", filename);
            boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
            if (isServiceRunning){
                Intent stopIntent = new Intent(getApplicationContext(),SongService.class);
                stopService(stopIntent);
            }
            startActivity(intent);
        }
    };

    public class VideoAdapter extends BaseAdapter {
        private Context vContext;

        public VideoAdapter(Context c) {
            vContext = c;
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            System.gc();
            ViewHolder holder;
            String id = null;
            convertView = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(vContext).inflate(
                        R.layout.av_layout, parent, false);
                holder = new ViewHolder();
                holder.txtTitle = (TextView) convertView
                        .findViewById(R.id.textList);
                holder.txtSize = (TextView) convertView
                        .findViewById(R.id.txtSize);
                holder.thumbImage = (ImageView) convertView
                        .findViewById(R.id.Thumbnail);

                video_column_index = videocursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME);
                videocursor.moveToPosition(position);
                id = videocursor.getString(video_column_index);
                id=removeExtension(id);
                video_column_index = videocursor
                        .getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                videocursor.moveToPosition(position);
                // id += " Size(KB):" +
                // videocursor.getString(video_column_index);
                holder.txtTitle.setText(id);
                holder.txtSize.setText(" Size(KB):"
                        + videocursor.getInt(video_column_index));

                String[] proj = { MediaStore.Video.Media._ID,
                        MediaStore.Video.Media.DISPLAY_NAME,
                        MediaStore.Video.Media.DATA };
                @SuppressWarnings("deprecation")
                Cursor cursor = managedQuery(
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI, proj,
                        MediaStore.Video.Media.DISPLAY_NAME + "=?",
                        new String[] { id }, null);
                cursor.moveToFirst();
                long ids = cursor.getLong(cursor
                        .getColumnIndex(MediaStore.Video.Media._ID));

                ContentResolver crThumb = getContentResolver();
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 1;
                Bitmap curThumb = MediaStore.Video.Thumbnails.getThumbnail(
                        crThumb, ids, MediaStore.Video.Thumbnails.MICRO_KIND,
                        options);
                holder.thumbImage.setImageBitmap(curThumb);
                curThumb = null;

            } /*
    * else holder = (ViewHolder) convertView.getTag();
    */
            return convertView;
        }
    }

    static class ViewHolder {

        TextView txtTitle;
        TextView txtSize;
        ImageView thumbImage;
    }

    public static String removeExtension(String filename){
        return filename;
    }



    /*public ArrayList<File> findAV(File root) {
        ArrayList<File> al = new ArrayList<File>();
        File[] files = root.listFiles();
        for (File singleFile : files){
            if (singleFile.isDirectory() && !singleFile.isHidden()){
                al.addAll(findAV(singleFile));
            }else {
                if (singleFile.getName().endsWith(".mp4") || singleFile.getName().endsWith(".mkv") || singleFile.getName().endsWith(".MP4") || singleFile.getName().endsWith(".MKV")
                        || singleFile.getName().endsWith(".flv")){
                    al.add(singleFile);
                }
            }
        }
        return al;
    }*/
}
