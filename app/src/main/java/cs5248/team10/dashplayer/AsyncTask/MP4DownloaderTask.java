package cs5248.team10.dashplayer.AsyncTask;

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
    private float prevBandwidth = 0.0f;
    private float prevPrevBandwidth = 0.0f;

    // threshold will not change if difference is less than buffer
    private float bandwidthBuffer = 0.003f;

    // thresholds
//    private String HIGH = "high/";
//    private String MEDIAN = "median/";
//    private String LOW = "low/";

    public static enum Thresholds
    {
        LOW ("low/"),
        MEDIAN ("median/"),
        HIGH ("high/");

        private String value;

        private static Thresholds[] vals = values();

        Thresholds(String s)
        {
            this.value = s;
        }

        public String toString()
        {
            return this.value;
        }

        public Thresholds next()
        {
            return vals[(this.ordinal() + 1) % vals.length];
        }
        public Thresholds prev()
        {
            return vals[(this.ordinal() - 1) % vals.length];
        }
    }

    private String prevThreshold = Thresholds.MEDIAN.toString();

    private String dwlPath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/";
    private String livePath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload_live/";

    private String savePath = Environment.getExternalStorageDirectory() + "/ttcVideo/";

//    private String TEST = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test1/high/output003.mp4";

    protected Void doInBackground(String... mFileName)
    {
        if (mFileName.length < 2)
        {
            Log.wtf("MP4DownloaderTask", "doInBackground for mFilename < 2 => " + mFileName.length);
            return null;
        }

        // check directory exist
        String folderName = mFileName[0];
        File saveFolder = checkAndCreateDir(savePath + mFileName[0] + "/");

        for (int i = 1; i < mFileName.length; i++)
        {
            // start first download task with median resolution
            if (i == 1)
            {
                prevPrevBandwidth = downloadFile(saveFolder, mFileName[i], folderName, prevThreshold);
            }
            else if (i == 2)
            {
                prevBandwidth = downloadFile(saveFolder, mFileName[i], folderName, prevThreshold);
            }
            // subsequent downloads
            else
            {
                // TODO: not tested yet!!!
Log.wtf("bandwidths", "prevPrevBandwidth: " + prevPrevBandwidth + " prevBandwidth: " + prevBandwidth);
                // if difference between previous 2 bandwidth is more than buffer
                if (Math.abs(prevPrevBandwidth - prevBandwidth) > bandwidthBuffer)
                {
                    // bandwidth decrease
                    if (prevPrevBandwidth > prevPrevBandwidth)
                    {
                        if (!prevThreshold.equals(Thresholds.LOW.toString()))
                        {
                            prevThreshold = Thresholds.valueOf(prevThreshold).prev().toString();
                        }
                    }
                    // bandwidth increase
                    else
                    {
                        if (!prevThreshold.equals(Thresholds.HIGH.toString()))
                        {
                            prevThreshold = Thresholds.valueOf(prevThreshold).next().toString();
                        }
                    }
                }
                prevPrevBandwidth = prevBandwidth;
                prevBandwidth = downloadFile(saveFolder, mFileName[i], folderName, prevThreshold);
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

    private float downloadFile (File saveFolder, String fileName, String folderName, String threshold)
    {
        float bandwidth = 0f;
        try
        {
            // get output file
            File saveFile = new File(saveFolder, fileName);

            //Create New File if not present
            if (!saveFile.exists()) {
                saveFile.createNewFile();
            }

            // file output stream
            FileOutputStream fos = new FileOutputStream(saveFile);

            long startTime = System.currentTimeMillis();

            // input stream from connection
            URL url = new URL(dwlPath + folderName + threshold + fileName);
            InputStream is = url.openConnection().getInputStream();

            //Set buffer type
            byte[] buffer = new byte[1024];
            //init length
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1)
            {
                //Write new file
                fos.write(buffer, 0, len1);
            }
            long contentLength = buffer.length;

            //Close all connection after doing task
            fos.close();
            is.close();

            // TODO: to test bandwidth calculation
            long endTime = System.currentTimeMillis();
            bandwidth = contentLength / ((endTime - startTime) * 1000);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return bandwidth;
    }
}
