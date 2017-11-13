package cs5248.team10.dashplayer.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import cs5248.team10.dashplayer.VideoActivity;

/**
 * Created by zhirong on 14/11/17.
 */

public class MPDReader extends AsyncTask<String, Void, Void>
{
    private Context context;

    private String folderName;
    private String mpdPath;

    private ArrayList<String> segmentNames = new ArrayList();

    private String dwlPath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/";
    private String livePath = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload_live/";

    public MPDReader(Context context)
    {
        this.context = context;
    }

    @Override
    protected Void doInBackground(String... strings)
    {
        folderName = strings[0];
        mpdPath = strings[1];

        Log.wtf("path", "mpd path = " + mpdPath);

        try
        {
            URL url = new URL(mpdPath);

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(false);
            XmlPullParser xpp = factory.newPullParser();

            // We will get the XML from an input stream
            xpp.setInput(url.openConnection().getInputStream(), "UTF_8");

            boolean insideItem = false;

            // Returns the type of current event: START_TAG, END_TAG, etc..
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT)
            {
                if (eventType == XmlPullParser.START_TAG)
                {
                    /*
                    <Representation id="high" mimeType="video/mp4" bandwidth="5600000" width="854" height="480">
                        <SegmentList duration="6">
                            <Initialization sourceURL="http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test/high/output003.mp4"/>
                            <SegmentURL media="http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test/high/output003.mp4" />
                            <SegmentURL media="http://monterosa.d2.comp.nus.edu.sg/~team10/server/upload/test/high/output002.mp4" />
                        </SegmentList>
                    </Representation>
                     */
                    Log.d("startTag", "<<< " + xpp.getName());
                    Log.d("AttributeCount", "<<< " + xpp.getAttributeCount());

                    if (xpp.getName().equalsIgnoreCase("SegmentURL"))
                    {
                        String media = xpp.getAttributeValue(null, "media");
                        Log.wtf("media", media);
                    }
                    if (xpp.getName().equalsIgnoreCase("SegmentList"))
                    {
                        insideItem = true;
                    }
                    else if (xpp.getName().equalsIgnoreCase("SegmentURL") && insideItem)
                    {
                        String media = xpp.getAttributeValue(null, "media");
                        String [] paths = media.split("/");
                        if (paths.length > 1)
                        {
                            String segment = paths[paths.length - 1];
                             segmentNames.add(segment);
                        }
                    }
                }
                else if (eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("SegmentList"))
                {
                    Log.d("endTag", ">>> " + xpp.getName());
                    Log.d("AttributeCount", ">>> " + xpp.getAttributeCount());
//                    insideItem = false;
                    break;
                }

                eventType = xpp.next(); //move to next element
            }

        }
        catch (XmlPullParserException | IOException e)
        {
            Log.wtf("xml parsing", "error: " + e.getMessage());
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        // start downloading
        String[] pathList =  segmentNames.toArray(new String[0]);
        new MP4DownloaderTask(folderName).execute(pathList);

        // start video
        Intent intent = new Intent(context.getApplicationContext(), VideoActivity.class);
        intent.putExtra("folder", folderName);
        intent.putExtra("files",  segmentNames);
        context.startActivity(intent);
    }
}
