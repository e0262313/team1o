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
 *
 * References:
 * - https://github.com/vecio/MediaCodecDemo/blob/master/src/io/vec/demo/mediacodec/DecodeActivity.java
 * - https://examples.javacodegeeks.com/android/core/ui/surfaceview/android-surfaceview-example/
 * - http://sohailaziz05.blogspot.sg/2014/06/mediacodec-decoding-aac-android.html
 * - https://github.com/saki4510t/AudioVideoPlayerSample
 * - https://stackoverflow.com/questions/999771/get-filenotfoundexception-when-initialising-fileinputstream-with-file-object
 * - https://stackoverflow.com/questions/8854359/exception-open-failed-eacces-permission-denied-on-android
 */

import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.File;

import cs5248.team10.dashplayer.Player.FrameCallback;
import cs5248.team10.dashplayer.Player.TTCMediaController;
import cs5248.team10.dashplayer.Player.TTCMoviePlayer;

public class VideoActivity extends AppCompatActivity implements SurfaceHolder.Callback, TTCMediaController.MediaPlayerControl
{
    private TTCMoviePlayer mPlayer = null;
    private TTCMediaController mController;

    private Handler handler = new Handler();

    private String CURR_POSITION = "curr_position";

    private int counter = 0;

    private int playToPosition = 0;

    private int BUFFER_SIZE = 10000;

    //private String SAMPLE = Environment.getExternalStorageDirectory() + "/ttcVideo/MVCAU_20171015_165316/VIDEO_20171015_165329.mp4";
//    private String SAMPLE = Environment.getExternalStorageDirectory() + "/ttcVideo/big_buck_bunny.mp4";
//    private String SAMPLE = "/storage/emulated/0/ttcVideo/big_buck_bunny.mp4";
//    private String SAMPLE = "file:///storage/emulated/0/ttcVideo/big_buck_bunny.mp4";
//
//
//
//    private String SAMPLE = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test1/high/output003.mp4";

    private String SAMPLE = Environment.getExternalStorageDirectory() + "/ttcVideo/MVCAU_20171015_165316/VIDEO_20171015_165329.mp4";

    //private String SAMPLE = ((File)((File)Environment.getExternalStorageDirectory()).listFiles()[7]).listFiles()[0].getAbsolutePath();
    // Storage Permissions
    private Surface mSurface;

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

        setContentView(R.layout.activity_video);

        verifyStoragePermissions(VideoActivity.this);

        // get the file list
        Bundle extras = getIntent().getExtras();
        if (extras != null)
        {
            // TODO: src should be a list of file path
            // assume list format is 0=foldername, 1 onwards= filename
            String src = extras.getString("src");
            SAMPLE = src;
            Log.wtf("getintent", "intent not null => " + src);
        }

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        Log.wtf("vid activity", "surface view = " + surfaceView);
        surfaceView.getHolder().addCallback(VideoActivity.this);

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

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder)
    {
        Log.wtf("Main --", "surfaceCreated");
        if (mPlayer == null)
        {
            mSurface = surfaceHolder.getSurface();

            mPlayer = new TTCMoviePlayer(mSurface, mFrameCallback);
            // TODO: there should be a list of src, on create play 2nd
            // assume list format is 0=foldername, 1 onwards= filename
            mPlayer.prepare(SAMPLE);

            //            mController = new TTCMediaController(this);
//            mController.setMediaPlayer(mPlayer);
////            mController.setAnchorView(findViewById(R.id.surfaceView));
//            mController.setAnchorView((FrameLayout) findViewById(R.id.videoSurfaceContainer));
            Log.wtf("surfaceCreated", "<<<<<<<< setup mplayer and mController");
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2)
    {
        Log.wtf("Main --", "surfaceChanged");
//        if (mPlayer != null)
//        {
//            mPlayer.prepare(SAMPLE);
//        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder)
    {
        if (mPlayer != null)
        {
            mPlayer.end();
            mPlayer = null;
        }
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
        Log.d("@@ onTouchEvent", "to show MC");
//        mController.show();
        return false;
    }

    /**
     * callback methods from decoder
     */
    private final FrameCallback mFrameCallback = new FrameCallback()
    {
        @Override
        public void onPrepared()
        {
            Log.wtf("prepared... ", "&&&&&&& onPrepared()");
            if (mPlayer == null)
            {
                Log.wtf("callback... ", "Why is mPlayer null???");
                return;
            }

            mPlayer.start();

            handler.post(new Runnable()
            {
                public void run()
                {
                    // TODO: check if looping before setting this
                    if (counter == 0)
                    {
                        //mController.setEnabled(true);
                        //mController.show();
                    }
                }
            });

        }

        @Override
        public void onFinished()
        {
            Log.wtf("done.. ", "&&&&&&& onFinished() with counter == " + counter);

            // try loop playing?
            if (counter++ < 4)
            {
                mPlayer = null;
                mPlayer = new TTCMoviePlayer(mSurface, mFrameCallback);
                // TODO: change repeat loop to loop the src list
                mPlayer.prepare(SAMPLE);
            }
            else
            {
                mPlayer = null;
//                mController.hide();
            }
        }

        @Override
        public boolean onFrameAvailable(long presentationTimeUs)
        {
            return false;
        }
    };


    //--MediaPlayerControl methods----------------------------------------------------
    public void start()
    {
        Log.wtf("MediaCTR ==> ", " start button!!");
        mPlayer.start();
    }

    public void pause()
    {
        mPlayer.pause();
    }

    public int getDuration()
    {
        return mPlayer.getDuration();
    }

    public int getCurrentPosition()
    {
        return mPlayer.getCurrentPosition();
    }

    public void seekTo(int i)
    {
        Log.wtf("seekTo", "i = " + i);
        mPlayer.seekTo(i);
    }

    public boolean isPlaying()
    {
        return mPlayer.isPlaying();
    }

    public int getBufferPercentage()
    {
        return 0;
    }

    public boolean canPause()
    {
        return true;
    }

    public boolean canSeekBackward()
    {
        return true;
    }

    public boolean canSeekForward()
    {
        return true;
    }
    //--------------------------------------------------------------------------------

    /**
     * For API 23+
     *
     * Checks if the app has permission to read the device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity)
    {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            // We don't have permission so prompt the user
            // Will display popup to ask user for permission
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }
}