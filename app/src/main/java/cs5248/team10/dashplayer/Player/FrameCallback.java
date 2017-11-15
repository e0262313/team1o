package cs5248.team10.dashplayer.Player;

/**
 * Created by zhirong on 1/11/17.
 *
 * callback interface
 */
public interface FrameCallback {
    /**
     * called when preparing finished
     */
    void onPrepared();

    /**
     * called when playing finished
     */
    void onFinished();

    /**
     * called every frame before time adjusting
     * return true if you don't want to use internal time adjustment
     */
    boolean onFrameAvailable(long presentationTimeUs);
}