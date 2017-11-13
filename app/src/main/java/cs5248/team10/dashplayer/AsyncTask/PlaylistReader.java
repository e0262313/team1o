package cs5248.team10.dashplayer.AsyncTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import cs5248.team10.dashplayer.R;
import cs5248.team10.dashplayer.VideoActivity;

/**
 * Created by zhirong on 12/11/17.
 *
 * Reference:
 * - https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
 * - https://stackoverflow.com/questions/12065951/how-can-i-parse-xml-from-url-in-android
 */

public class PlaylistReader extends AsyncTask<String, Void, Void>
{
    private Context context;
    private SwipeRefreshLayout swipeContainer;

    private ArrayList<ListData> playlist = new ArrayList<>();

    private String mpdList = "http://monterosa.d2.comp.nus.edu.sg/~team10/server/get_mpd_list.php";

    public PlaylistReader(Context context, SwipeRefreshLayout swipeContainer)
    {
        this.context = context;
        this.swipeContainer = swipeContainer;
    }

    @Override
    protected void onPreExecute()
    {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(String... arg0)
    {
        try
        {
            URL mpdListUrl = new URL(mpdList);
            URLConnection dc = mpdListUrl.openConnection();

            dc.setConnectTimeout(5000);
            dc.setReadTimeout(5000);

            BufferedReader inputStream = new BufferedReader(new InputStreamReader(dc.getInputStream()));

            String mpdFile;
            while ((mpdFile = inputStream.readLine()) != null)
            {
                String[] list = mpdFile.split("/");
                if (list.length > 1)
                {
                    String currFile = list[list.length - 1];
                    String[] file = currFile.split("\\.");
                    if (file.length > 1)
                    {
                        ListData d = new ListData();
                        d.folderName = file[0];
                        d.folderPath = mpdFile;
                        playlist.add(d);
                        Log.v("doInBackground", "%%%%% mdps => " + mpdFile + " & filename = " + d.folderName);
                    }
                    else Log.wtf("doInBackground", "%%%%% file length => " + file.length);
                }
                else Log.wtf("doInBackground", "%%%%% list length " + list.length);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        // TODO: what about live videos? (path = upload_live/folderName)

        Log.wtf("doInBackground", "reading playlist...");

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
//        // test download task
//        new MP4DownloaderTask().execute(name);

        Log.wtf("onPostExecute", "populating result");
//        String[] name = {"output_1", "output_2", "output", "output3"};
//        String[] path = {"upload/output_1", "upload/output_2", "upload/output", "upload/output3"};

//        // comment out below test data for playlist
//        playlist = new ArrayList<>();
//        for (int i = 0; i < name.length; i++)
//        {
//            ListData d = new ListData();
//            d.folderName = name[i];
//            d.folderPath = path[i];
//            playlist.add(d);
//        }

        ArrayAdapter<ListData> adapter = new ArrayAdapter<>(context, R.layout.vid_item, playlist);

        final ListView listView = ((Activity) context).findViewById(R.id.vidList);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                // TODO: get the list of mp4s for video selected
                // i.e path should be replaced with list of 3s vid file paths (arraylist)
                // assume list format is 0=foldername, 1 onwards= filename

                ListData item = (ListData) listView.getItemAtPosition(position);
                String folder = item.folderName;
                String path = "http://" + item.folderPath;

                new MPDReader(context).execute(folder, path);
            }
        });

        if (swipeContainer != null) swipeContainer.setRefreshing(false);
    }

    class ListData
    {
        String folderName;
        String folderPath;

        @Override
        public String toString()
        {
            return folderName;
        }
    }
}
