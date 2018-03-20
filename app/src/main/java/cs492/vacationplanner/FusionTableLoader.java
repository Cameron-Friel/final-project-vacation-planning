package cs492.vacationplanner;

import android.content.Context;

import org.json.JSONObject;

import android.support.v4.content.AsyncTaskLoader;
import java.io.IOException;

/**
 * Created by Kenneth Price on 3/15/2018.
 */

public class FusionTableLoader extends AsyncTaskLoader<String> {
    public static String FUSION_TABLE_URL_ID = "fusionTableUrl";

    private String mFusionTableURL;
    private String mBorderDataResultsJSON;

    public FusionTableLoader(Context context, String url) {
        super(context);
        mFusionTableURL = url;
    }

    @Override
    protected void onStartLoading() {
        if(mFusionTableURL != null) {
            if(mBorderDataResultsJSON != null) {
                deliverResult(mBorderDataResultsJSON);
            } else {
                forceLoad();
            }
        }
    }

    @Override
    public String loadInBackground() {
        if(mFusionTableURL != null) {
            String borderDataResults = null;
            try {
                borderDataResults = NetworkUtils.doHTTPGet(mFusionTableURL);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return borderDataResults;
        } else {
            return null;
        }
    }

    @Override
    public void deliverResult(String data) {
        mBorderDataResultsJSON = data;
        super.deliverResult(data);
    }
}
