package cs492.vacationplanner;

import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
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

    public static class LayerInfo {
        public JSONObject layerGeometry;
        public String name;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static String buildLocationInString(ArrayList<String> countries) {
        String inString = new String("'");
        inString += String.join("', '", countries);
        inString += "'";
        return inString;
    }

    public static String buildFusionTablesQuery(String countryNames) {
        Uri.Builder builder = Uri.parse(FUSION_TABLES_BASE_URL).buildUpon();

        String queryValue = new String();
        queryValue = "SELECT json_4326, name FROM " + BORDERS_TABLE_ID + " WHERE name IN (" + countryNames + ")";
        builder.appendQueryParameter(FUSION_TABLES_SQL_PARAM,queryValue);
        builder.appendQueryParameter(FUSION_TABLES_KEY_PARAM,FUSION_TABLES_APIKEY);

        return builder.build().toString();
    }

    //public static ArrayList<GeoJsonLayer> getGeoJsonCoordinates(String queryResults) {
    public static ArrayList<LayerInfo> getGeoJsonCoordinates(String queryResults) {
        ArrayList<LayerInfo> layersList = new ArrayList<>();
        try {
            Log.d(TAG,"test");
            JSONObject queryResultsObj = new JSONObject(queryResults);

            JSONArray borderData = queryResultsObj.getJSONArray("rows");
            Log.d(TAG, borderData.toString());
            for(int i =0; i<borderData.length();i++)
            {
                LayerInfo layerInfo = new LayerInfo();
                JSONArray country = borderData.getJSONArray(i);
                Log.d(TAG,country.toString());
                Log.d(TAG,country.get(0).toString());
                Log.d(TAG,country.get(1).toString());
                layerInfo.layerGeometry = new JSONObject(country.get(0).toString());
                layerInfo.name = country.get(1).toString();
                layersList.add(layerInfo);
            }
            return layersList;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
