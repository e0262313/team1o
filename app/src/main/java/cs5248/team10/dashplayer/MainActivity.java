package cs5248.team10.dashplayer;

/**
 * DASH Player Requirement:
 * 1) Retrieve a list of videos available on your server and download the MPEG-DASH XML-formatted
 * playlist file for the selected video (on-demand or live).
 * 2) Read the playlist file and schedule retrieval of individual streamlets on-the-fly.
 * 3) Switch the rendering of one video streamlet to another.
 * ** support adaptive stream switching ─ that means it adaptively chooses
 * the most suitable quality for the next video streamlet to be downloaded based on your
 * bandwidth estimation algorithm, while playing the current streamlet
 *
 *
 * Bonus:
 * 1) Implement video switching and rendering seamlessly.
 * 2) Display the current network bandwidth estimation results visually.
 *
 * Reference:
 * - https://stackoverflow.com/questions/21369037/android-list-view-with-additional-extra-hidden-fields
 * - https://guides.codepath.com/android/implementing-pull-to-refresh-guide
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cs5248.team10.dashplayer.AsyncTask.MP4DownloaderTask;
import cs5248.team10.dashplayer.AsyncTask.PlaylistReader;
import cs5248.team10.dashplayer.Player.TTCMediaController;
import cs5248.team10.dashplayer.Player.TTCMoviePlayer;

public class MainActivity extends AppCompatActivity
{

    // DONE: to start a single video from url with media codec + extractor
    // HALF: start video + audio together (refactor code)
    // DONE: play video from phone itself - assume that will dwl video from server (do later)
    // TODO: Media player pause, seekTo, resume, time passed + progress (totally unusable now)
    // TODO: to start multiple videos (back to back) === BUG: why is video playback faster

    // TODO: to retrieve list of videos from server (playlist) -- waiting for file
    // DONE: to display the playlist to user (need to update or auto refresh?) -- used fake data
    // TODO: to dwl video when selected (MPEG-DASH XML-formatted??) (async?) -- waiting for file
    // DONE: to use asynctask to retrieve the 3 sec streamlets on the fly -- MP4DownloaderTask
    // TODO: **** bandwidth estimation algorithm???
    // TODO: **** adaptively switch between streamlet

    private SwipeRefreshLayout swipeContainer;

    private TTCMoviePlayer mPlayer = null;
    private TTCMediaController mController;

    private Handler handler = new Handler();

    private String CURR_POSITION = "curr_position";

    private int counter = 0;

    private int playToPosition = 0;

    private int BUFFER_SIZE = 10000;

    private String dwlPath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/";
    private String livePath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload_live/";

    private String savePath = Environment.getExternalStorageDirectory() + "/ttcVideo/";

    private String TEST = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test1/high/output003.mp4";

    private String SAMPLE = Environment.getExternalStorageDirectory() + "/ttcVideo/MVCAU_20171015_165316/VIDEO_20171015_165329.mp4";
//    private String SAMPLE = Environment.getExternalStorageDirectory() + "/ttcVideo/big_buck_bunny.mp4";
//    private String SAMPLE = "/storage/emulated/0/ttcVideo/big_buck_bunny.mp4";
//    private String SAMPLE = "file:///storage/emulated/0/ttcVideo/big_buck_bunny.mp4";
//    private String SAMPLE = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        Log.wtf("Initialise --------", "on creation");

        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        setContentView(R.layout.activity_main);

        // check savePath exist
        checkAndCreateDir(savePath);

        // populate into the list view
        new PlaylistReader(MainActivity.this, null).execute();

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                new PlaylistReader(MainActivity.this, swipeContainer).execute();
            }
        });
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }

    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

    }

    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved position.
        playToPosition = savedInstanceState.getInt(CURR_POSITION);
    }

    /**
     * When user touches the screen
     *
     * @param event
     * @return
     */
    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        Log.d("@@ onTouchEvent", "click list?");
//        mController.show();
        return false;
    }

    private File checkAndCreateDir(String targetPath)
    {
        File target = new File(targetPath);
        if (!target.exists() || !target.isDirectory())
        {
            target.mkdir();
        }
        return target;
    }
}
