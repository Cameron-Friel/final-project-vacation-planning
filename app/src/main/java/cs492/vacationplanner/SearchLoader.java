package cs492.vacationplanner;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import cs492.vacationplanner.Utils.NetworkUtils;

import java.io.IOException;

/**
 * Created by Cameron on 3/3/18.
 */

public class SearchLoader extends AsyncTaskLoader<String> {
    private final static String TAG = SearchLoader.class.getSimpleName();

    private String mSearchResultsJSON;
    private String mSearchURL;

    SearchLoader(Context context, String url) {
        super(context);
        mSearchURL = url;
    }

    @Override
    protected void onStartLoading() {
        if (mSearchURL != null) {
            if (mSearchResultsJSON != null) {
                Log.d(TAG, "loader returning cached results");
                deliverResult(mSearchResultsJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Override
    public String loadInBackground() {
        if (mSearchURL != null) {
            Log.d(TAG, "loadin results with URL: " + mSearchURL);
            String searchResults = null;
            try {
                searchResults = NetworkUtils.doHTTPGet(mSearchURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return searchResults;
        } else {
            return null;
        }
    }

    @Override
    public void deliverResult(String data) {
        mSearchResultsJSON = data;
        super.deliverResult(data);
    }
}
