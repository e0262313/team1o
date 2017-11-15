package cs5248.team10.dashplayer.AsyncTask;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by zhirong on 12/11/17.
 */

public class MP4DownloaderTask extends AsyncTask<String, Void, Void>
{
    private String folderName;

    private float prevBandwidth = 0.0f;
    private float prevPrevBandwidth = 0.0f;

    // threshold will not change if difference is less than buffer
    private float bandwidthBuffer = 0.0001f;

    // thresholds
    private String HIGH = "high/";
    private String MEDIAN = "median/";
    private String LOW = "low/";

    private String prevThreshold = LOW;

    private String dwlPath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/";
    private String livePath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload_live/";

    private String savePath = Environment.getExternalStorageDirectory() + "/ttcVideo/";

//    private String TEST = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test1/high/output003.mp4";

    public MP4DownloaderTask(String folderName)
    {
        this.folderName = folderName;
    }

    protected Void doInBackground(String... mSegmentName)
    {
        if (mSegmentName.length < 1)
        {
            Log.wtf("MP4DownloaderTask", "doInBackground for mFilename < 1 => " + mSegmentName.length);
            return null;
        }

        // check directory exist
        String folderDir = savePath + folderName + "/";
        File saveFolder = checkAndCreateDir(folderDir);

        for (int i = 0; i < mSegmentName.length; i++)
        {
            // start first download task with median resolution
            if (i == 0)
            {
                prevPrevBandwidth = downloadFile(folderDir, mSegmentName[i], folderName, prevThreshold);
            }
            // 2nd segment
            else if (i == 1)
            {
                prevBandwidth = downloadFile(folderDir, mSegmentName[i], folderName, prevThreshold);
            }
            // subsequent downloads
            else
            {
Log.wtf("bandwidths", "prevPrevBandwidth: " + String.format("%.5f", prevPrevBandwidth) + " prevBandwidth: " + String.format("%.5f", prevBandwidth));
                // if difference between previous 2 bandwidth is more than buffer
                if (Math.abs(prevPrevBandwidth - prevBandwidth) > bandwidthBuffer)
                {
                    // bandwidth decrease
                    if (prevPrevBandwidth > prevBandwidth)
                    {
                        Log.wtf("bandwidth", "decrease");
                        if (!prevThreshold.equals(LOW))
                        {
                            if (prevThreshold.equals(MEDIAN))
                                prevThreshold = LOW;
                            else prevThreshold = HIGH;
                        }
                    }
                    // bandwidth increase
                    else
                    {
                        Log.wtf("bandwidth", "increase");
                        if (!prevThreshold.equals(HIGH))
                        {
                            if (prevThreshold.equals(MEDIAN))
                                prevThreshold = HIGH;
                            else prevThreshold = MEDIAN;
                        }
                    }
                }
                prevPrevBandwidth = prevBandwidth;
                prevBandwidth = downloadFile(folderDir, mSegmentName[i], folderName, prevThreshold);
            }
        }

        // calculate the video time?

        return null;
    }

    private File checkAndCreateDir (String targetPath)
    {
        File target = new File(targetPath);
        if (!target.exists() || !target.isDirectory())
        {
            target.mkdir();
        }
        return target;
    }

    private float downloadFile (String folderDir, String fileName, String folderName, String threshold)
    {
        float bandwidth = 0f;
        try
        {
            Log.wtf("downloadFile", "downloading -> " + fileName);


            long startTime = System.currentTimeMillis();

            // input stream from connection
            Log.wtf("downloadFile", "download path = " + dwlPath + folderName + "/" + threshold + fileName);
            URL url = new URL(dwlPath + folderName + "/" + threshold + fileName);
            InputStream is = url.openConnection().getInputStream();

            // file output stream
            FileOutputStream fos = new FileOutputStream(folderDir + fileName);

            //Set buffer type
            byte[] buffer = new byte[1024];
            //init length
            int len1;
            while ((len1 = is.read(buffer)) != -1)
            {
                //Write new file
                fos.write(buffer, 0, len1);
            }
            long contentLength = buffer.length;

            // Close all connection after doing task
            fos.close();
            is.close();

            // TODO: to test bandwidth calculation
            long endTime = System.currentTimeMillis();

            bandwidth = contentLength / ((endTime - startTime) * 1000f);

//            Log.wtf("downloadFile","contentlength = " + contentLength + " | start time = " + startTime + " | end time = " + endTime);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bandwidth;
    }
}
