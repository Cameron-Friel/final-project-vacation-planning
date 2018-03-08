package cs492.vacationplanner.Utils;

import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Cameron on 3/3/18.
 */

public class DataUtils {
    public static final String EXTRA_SEARCH_RESULT = "GitHubUtils.SearchResult";

    final static String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/";
    final static String TYPE = "json";
    final static String QUERY_PARAM = "address";
    final static String API_PARAM = "key";
    final static String API_KEY = "AIzaSyBsv5znm5W6ayCcJ9XtaMbe6xhGjicHF6Y";

    public static class SearchResult implements Serializable {
        public double latitude;
        public double longitude;
        public String country;
    }

    public static String buildLocationURL(String address) {
        return Uri.parse(BASE_URL + TYPE).buildUpon()
                .appendQueryParameter(QUERY_PARAM, address)
                .appendQueryParameter(API_PARAM, API_KEY)
                .build()
                .toString();
    }

    public static SearchResult parseSearchResultsJSON(String searchResultsJSON) {
        try {
            JSONObject searchResultsObj = new JSONObject(searchResultsJSON);
            JSONArray searchResultsItems = searchResultsObj.getJSONArray("results");
            System.out.println(searchResultsItems);

            SearchResult result = new SearchResult();

            for (int i = 0; i < searchResultsItems.length(); i++) {
                JSONObject resultItem = searchResultsItems.getJSONObject(i);
                JSONArray places = resultItem.getJSONArray("address_components");

                result.country = places.getJSONObject(places.length() - 1).getString("long_name");
                result.latitude = resultItem.getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                result.longitude = resultItem.getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                break;
            }
            return result;
        } catch (JSONException e) {
            return null;
        }
    }
}
