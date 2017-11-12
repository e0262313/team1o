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

import java.util.ArrayList;

import cs5248.team10.dashplayer.R;
import cs5248.team10.dashplayer.VideoActivity;

/**
 * Created by zhirong on 12/11/17.
 *
 * Reference:
 * - https://stackoverflow.com/questions/12575068/how-to-get-the-result-of-onpostexecute-to-main-activity-because-asynctask-is-a
 */

public class PlaylistReader extends AsyncTask<String, Void, Void>
{
    private Context context;
    private SwipeRefreshLayout swipeContainer;

    private ArrayList<ListData> playlist;

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
        // TODO: read in main playlist of all available videos
        // TODO: what about live videos? (path = upload_live/folderName)

        // TODO: populate playlist with data

        Log.wtf("doInBackground","reading playlist...");

        return null;
    }

    @Override
    protected void onPostExecute(Void result)
    {
        Log.wtf("onPostExecute", "populating result");
        String[] name = {"output_1", "output_2", "output", "output3"};
        String[] path = {"upload/output_1", "upload/output_2", "upload/output", "upload/output3"};

//        // test download task
//        new MP4DownloaderTask().execute(name);

        // TODO: comment out below test data for playlist
        playlist = new ArrayList<>();
        for (int i = 0; i < name.length; i++)
        {
            ListData d = new ListData();
            d.folderName = name[i];
            d.folderPath = path[i];
            playlist.add(d);
        }

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
                String path = item.folderPath;
                new MP4DownloaderTask().execute(path);

                Intent intent = new Intent(context.getApplicationContext(), VideoActivity.class);
                intent.putExtra("src", path);
                context.startActivity(intent);
            }
        });

        if (swipeContainer != null)
            swipeContainer.setRefreshing(false);
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
