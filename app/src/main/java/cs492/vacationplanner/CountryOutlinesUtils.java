package cs492.vacationplanner;

import android.location.Geocoder;
import android.net.Uri;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.maps.android.data.geojson.GeoJsonLayer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Kenneth Price on 3/15/2018.
 */

public class CountryOutlinesUtils {
    private static final String TAG = CountryOutlinesUtils.class.getSimpleName();

    static final String BORDERS_TABLE_ID = "1uKyspg-HkChMIntZ0N376lMpRzduIjr85UYPpQ";

    static final String FUSION_TABLES_BASE_URL = "https://www.googleapis.com/fusiontables/v2/query";
    static final String FUSION_TABLES_SQL_PARAM = "sql";
    static final String FUSION_TABLES_KEY_PARAM = "key";
    static final String FUSION_TABLES_APIKEY = "AIzaSyAZye_2iYZvKqAkxI6KgAmQ76cKqg9l5sk";

    public static String buildFusionTablesQuery(String countryNames) {
        Uri.Builder builder = Uri.parse(FUSION_TABLES_BASE_URL).buildUpon();

        String queryValue = new String();
        queryValue = "SELECT json_4326 FROM " + BORDERS_TABLE_ID + " WHERE name IN (" + countryNames + ")";
        builder.appendQueryParameter(FUSION_TABLES_SQL_PARAM,queryValue);
        builder.appendQueryParameter(FUSION_TABLES_KEY_PARAM,FUSION_TABLES_APIKEY);

        return builder.build().toString();
    }

    //public static ArrayList<GeoJsonLayer> getGeoJsonCoordinates(String queryResults) {
    public static ArrayList<JSONObject> getGeoJsonCoordinates(String queryResults) {
        ArrayList<JSONObject> layersList = new ArrayList<>();
        try {
            Log.d(TAG,"test");
            JSONObject queryResultsObj = new JSONObject(queryResults);

            JSONArray borderData = queryResultsObj.getJSONArray("rows");
            Log.d(TAG, borderData.toString());
            for(int i =0; i<borderData.length();i++)
            {
                JSONArray country = borderData.getJSONArray(i);
                Log.d(TAG,country.toString());
                Log.d(TAG,country.get(0).toString());
                layersList.add(new JSONObject(country.get(0).toString()));
            }
            return layersList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
