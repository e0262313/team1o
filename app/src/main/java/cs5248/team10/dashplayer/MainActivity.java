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
 * <p>
 * <p>
 * Bonus:
 * 1) Implement video switching and rendering seamlessly.
 * 2) Display the current network bandwidth estimation results visually.
 * <p>
 * <p>
 * References:
 * - https://github.com/vecio/MediaCodecDemo/blob/master/src/io/vec/demo/mediacodec/DecodeActivity.java
 * - https://examples.javacodegeeks.com/android/core/ui/surfaceview/android-surfaceview-example/
 * - http://sohailaziz05.blogspot.sg/2014/06/mediacodec-decoding-aac-android.html
 */

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    // DONE: to play a single video from url with media codec + extractor
    // TODO: play audio (audio track not working)
    // TODO: to play multiple videos (back to back)
    // TODO: Media player for user to seek, play, pause etc.
    // TODO: to retrieve list of videos from server (playlist)
    // TODO: to display the playlist to user (need to update or auto refresh?)
    // TODO: to play video retrieved from server (MPEG-DASH XML-formatted??)
    // TODO: to use asynctask to retrieve the 3 sec streamlets on the fly
    // TODO: **** bandwidth estimation algorithm???
    // TODO: **** adaptively switch between streamlet

    private String CURR_POSITION = "curr_position";

    private int playToPosition = 0;

    private PlayerThread playerThread = null;

    private int BUFFER_SIZE = 10000;

    private String videoUrl = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
    private String SAMPLE = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d("Initialise", "on creation");

        // BUG: once screen rotates or change, app will close

//        // remove title
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        Remove notification bar
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        setContentView(R.layout.activity_main);

        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceView.getHolder().addCallback(MainActivity.this);

    }

    protected void onDestroy() {
        super.onDestroy();
    }

    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

    }


    // After rotating the phone. This method is called.
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Get saved position.
        playToPosition = savedInstanceState.getInt(CURR_POSITION);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (playerThread == null) {
            playerThread = new PlayerThread(surfaceHolder.getSurface(), SAMPLE);
            playerThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (playerThread != null) {
            playerThread.interrupt();
        }
    }

    private class PlayerThread extends Thread {
        private MediaExtractor extractor;
        private MediaCodec decoder;
        private MediaCodec adecoder;
        private Surface surface;
        private String source;

        public PlayerThread(Surface surface, String source) {
            this.surface = surface;
            this.source = source;
        }

        @Override
        public void run() {
            // try looping datasource to combine streamlets??? (TODO: BUG -> cannot combine via looping)
            for (int eg = 0; eg < 1; eg++) {
                extractor = new MediaExtractor();
                try {
                    extractor.setDataSource(source);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                for (int i = 0; i < extractor.getTrackCount(); i++) {
                    MediaFormat format = extractor.getTrackFormat(i);
                    String mime = format.getString(MediaFormat.KEY_MIME);
                    if (mime.startsWith("video/")) {
                        extractor.selectTrack(i);
                        try {
                            decoder = MediaCodec.createDecoderByType(mime);
                            decoder.configure(format, surface, null, 0);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    }
                    // TODO: No audio
                    // according to https://stackoverflow.com/questions/26776920/android-mediacodec-mediaextractor-for-video-and-audio-playing
                    //  -> should be 2 decodec to one extractor to do the job
//                    if (mime.startsWith("audio")){
//                        Log.i("FORMAT", "Audio found!");
//                        // get the sample rate to configure AudioTrack
//                        int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
//                        Log.i("Sample Rate: ", String.valueOf(sampleRate));
//
//                        try {
//                            adecoder = MediaCodec.createDecoderByType(mime);
//                            adecoder.configure(format, null, null, 0);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
////                        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
////                                AudioFormat.CHANNEL_OUT_STEREO, AudioFormat.ENCODING_PCM_8BIT,
////                                AudioTrack.getMinBufferSize(sampleRate,
////                                        AudioFormat.CHANNEL_OUT_STEREO,
////                                        AudioFormat.ENCODING_PCM_8BIT), AudioTrack.MODE_STREAM);
////
////                        audioTrack.play();
//                    }
                }

                if (decoder == null) {
                    Log.e("DecodeActivity", "Can't find video info!");
                    return;
                }
//                if (adecoder == null) {
//                    Log.e("DecodeActivity", "Can't find audio info!");
//                    return;
//                }

                decoder.start();
//                adecoder.start();

                MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
                boolean reachOutputEOS = false;
                boolean isEOS = false;
                long startMs = System.currentTimeMillis();

//                while (!Thread.interrupted()) {
                while (!reachOutputEOS) {
                    if (!isEOS) {
                        int inIndex = decoder.dequeueInputBuffer(BUFFER_SIZE);
//                        int aInIndex = adecoder.dequeueInputBuffer(BUFFER_SIZE);
//                        if (inIndex >= 0 && aInIndex >= 0) {
                        if (inIndex >= 0) {
                            ByteBuffer buffer = decoder.getInputBuffer(inIndex);
                            int sampleSize = extractor.readSampleData(buffer, 0);
                            if (sampleSize < 0) {
                                // We shouldn't stop the playback at this point, just pass the EOS
                                // flag to decoder, we will get it again from the
                                // dequeueOutputBuffer
                                Log.d("DecodeActivity", "InputBuffer BUFFER_FLAG_END_OF_STREAM");
                                decoder.queueInputBuffer(inIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
//                                adecoder.queueInputBuffer(aInIndex, 0, 0, 0, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                                isEOS = true;
                            } else {
                                decoder.queueInputBuffer(inIndex, 0, sampleSize, extractor.getSampleTime(), 0);
//                                adecoder.queueInputBuffer(aInIndex, 0, sampleSize, extractor.getSampleTime(), 0);
                                extractor.advance();
                            }
                        }
                        else
                            Log.i("===> In index", String.valueOf(inIndex));
                    }

                    int outIndex = decoder.dequeueOutputBuffer(info, BUFFER_SIZE);
//                    int aoutIndex = adecoder.dequeueOutputBuffer(info, BUFFER_SIZE);
                    switch (outIndex) {
                        case MediaCodec.INFO_OUTPUT_FORMAT_CHANGED:
                            Log.d("DecodeActivity", "New format " + decoder.getOutputFormat());
                            break;
                        case MediaCodec.INFO_TRY_AGAIN_LATER:
                            Log.d("DecodeActivity", "dequeueOutputBuffer timed out!");
                            break;
                        default:
                            if (outIndex >= 0) {
                                ByteBuffer buffer = decoder.getOutputBuffer(outIndex);
//                                ByteBuffer abuffer = adecoder.getOutputBuffer(aoutIndex);

//                                final byte[] chunk = new byte[info.size];
//                                buffer.get(chunk);
//                                buffer.clear();
//
//                                if (chunk.length > 0) {
//                            Log.d("AUDIO", "Playing chunk");
//                                    // play audio
////                                    audioTrack.write(chunk, 0, chunk.length);
//                                }

                                // We use a very simple clock to keep the video FPS, or the video
                                // playback will be too fast
                                while (info.presentationTimeUs / 1000 > System.currentTimeMillis() - startMs) {
                                    try {
                                        sleep(10);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                        break;
                                    }
                                }
                                decoder.releaseOutputBuffer(outIndex, true);
//                                adecoder.releaseOutputBuffer(aoutIndex, true);

                                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                                    Log.i("sohail", "saw output EOS.");
                                    reachOutputEOS = true;
                                }

                                break;
                            }
                            else
                                Log.i("===> Out index", String.valueOf(outIndex));
                    }

                    // All decoded frames have been rendered, we can stop playing now
                    if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        Log.d("DecodeActivity", "OutputBuffer BUFFER_FLAG_END_OF_STREAM");
                        break;
                    }
                }
            }

//            if (audioTrack != null) {
//                audioTrack.flush();
//                audioTrack.release();
//                audioTrack = null;
//            }

            decoder.stop();
            decoder.release();
            extractor.release();
        }
    }
}
