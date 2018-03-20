package cs492.vacationplanner;

import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.Feature;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;

import java.io.IOException;

import cs492.vacationplanner.Utils.DataUtils;
import cs492.vacationplanner.Utils.NetworkUtils;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String>, NavigationView.OnNavigationItemSelectedListener {

    //Keys that go with bundles to new activities

    public final static String VISITED_TITLE_KEY = "visitedKey";
    public final static String WISH_LIST_TITLE_KEY = "wishListKey";

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int FUSION_TABLE_LOADER_ID = 1;

    private GoogleMap mMap;

    private FusionTableLoaderManager mFusionTableLoaderManager = new FusionTableLoaderManager();

    private Marker mCurrentSelectionMarker;

    private ArrayList<GeoJsonLayer> mCountryOverlays = new ArrayList<GeoJsonLayer>();

    private ArrayList<Float> tempLats = new ArrayList<Float>();
    private ArrayList<Float> tempLons = new ArrayList<Float>();
    private ArrayList<String> tempNames = new ArrayList<String>();
    private SQLiteDatabase locationDB; //update database with values

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private SearchView search; //object to handle user searches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationView navigationView = findViewById(R.id.main_navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

        LocationContractHelper dbHelper = new LocationContractHelper(this);
        locationDB = dbHelper.getWritableDatabase();

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_activity_menu, menu);
        search = (SearchView)menu.findItem(R.id.search).getActionView();

        search.setOnQueryTextListener (
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextChange (String newText) {
                        //text has changed, apply suggestions for search
                        return false;
                    }
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        //text submitted by user
                        System.out.println(query);
                        String locationURL = DataUtils.buildLocationURL(query);
                        System.out.println(locationURL);

                        createMapSearch(locationURL);
                        return false;
                    }
                });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.main_settings:
                //add functionality for settings
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public ArrayList<String> getAllContryNamesFromDatabase() {
        Cursor cursor = locationDB.query(
                LocationContract.Locations.TABLE_NAME,
                new String[]{LocationContract.Locations.COLUMN_COUNTRY_NAME},
                null,
                null,
                null,
                null,
                null);
        ArrayList<String> savedCountyNames = new ArrayList<String>();
        while (cursor.moveToNext()) {
            String name = new String();
            name = cursor.getString(cursor.getColumnIndex(LocationContract.Locations.COLUMN_COUNTRY_NAME));
            savedCountyNames.add(name);
        }
        return savedCountyNames;
    }

    public LatLng getLattitudeAndLongitudeOfCountryFromDB(String country) {
        Cursor cursor = locationDB.query(
                LocationContract.Locations.TABLE_NAME,
                new String[]{LocationContract.Locations.COLUMN_LATITUDE, LocationContract.Locations.COLUMN_LONGITUDE},
                LocationContract.Locations.COLUMN_COUNTRY_NAME + "='" + country + "'",
                null,
                null,
                null,
                null);
        cursor.moveToNext();
        String lat = cursor.getString(cursor.getColumnIndex(LocationContract.Locations.COLUMN_LATITUDE));
        String lon = cursor.getString(cursor.getColumnIndex(LocationContract.Locations.COLUMN_LONGITUDE));
        Log.d(TAG, lat);
        LatLng retVal = new LatLng(Double.parseDouble(lat),Double.parseDouble(lon));
        //LatLng retVal = new LatLng(1,1);
        return retVal;
    }

    public void initVisitedAndWishListActivity() { //sends user to recycle view of their visited and wish list entries in the database
        Intent visitedAndWishListActivity = new Intent(this, visited_wishlist_activity.class);
        startActivity(visitedAndWishListActivity);
    }

    public void initVisitedActivity() { //sends user to recycle view of their visited entries in the database
        Intent visitedActivity = new Intent(this, visited_wishlist_activity.class);
        visitedActivity.putExtra(VISITED_TITLE_KEY, "Visited Places");
        startActivity(visitedActivity);
    }

    void setTemps() {
        for(int i = -10; i < 10; i++){
            tempLats.add((float) (15.0*i));
            tempLons.add((float) (15.0*i));
            tempNames.add(((Float) (float) (15.0*i)).toString());
        }
    }

    private void addSavedLocations() {
        for (int i = 0; i < tempLats.size(); i++) {
            Log.d(TAG, "addSavedLocations:adding " + i);
            LatLng toAdd = new LatLng(tempLats.get(i), tempLons.get(i));
            mMap.addMarker(new MarkerOptions().position(toAdd).title(tempNames.get(i)));
        }
    }

    private void deleteOverlays() {
        for (GeoJsonLayer layer : mCountryOverlays) {
            layer.removeLayerFromMap();
        }
        while (!mCountryOverlays.isEmpty()) {
            mCountryOverlays.remove(0);
        }
    }

    private void createOverlays(ArrayList<CountryOutlinesUtils.LayerInfo> borderData) {
        GeoJsonLayer layer = new GeoJsonLayer(mMap, borderData.get(0).layerGeometry);
        for(GeoJsonFeature e : layer.getFeatures()) {
            layer.removeFeature(e);
        }
        for(int i=0;i<borderData.size();i++)
        {
            Log.d(TAG,"Creating Overlay");
            GeoJsonLayer tempLayer = new GeoJsonLayer(mMap,borderData.get(i).layerGeometry);
            for (GeoJsonFeature e : tempLayer.getFeatures())
            {
                GeoJsonPolygonStyle style = new GeoJsonPolygonStyle();
                style.setFillColor(0xff00ff00+(20*i));                      //Color of the overlays
                style.setStrokeColor(0xff000000);
                style.setStrokeWidth(2);
                e.setPolygonStyle(style);
                e.setProperty("Name", borderData.get(i).name);
                layer.addFeature(e);
            }
        }
        mCountryOverlays.add(layer);
        layer.setOnFeatureClickListener(new OnLayerClickListener());
    }

    private void addOverlays() {
        for (GeoJsonLayer layer : mCountryOverlays) {
            layer.addLayerToMap();
        }
    }
    public void initWishListActivity() { //sends user to recycle view of their wish list entries in the database
        Intent wishListActivity = new Intent(this, visited_wishlist_activity.class);
        wishListActivity.putExtra(WISH_LIST_TITLE_KEY, "Places on Wish List");
        startActivity(wishListActivity);
    }

    public void createMapSearch(String locationURL) {
        Bundle args = new Bundle();
        args.putString("url", locationURL);
        getSupportLoaderManager().restartLoader(0, args, this);
    }

    @Override
    public Loader<String> onCreateLoader(int id, Bundle args) {
        String searchURL = null;
        if (args != null) {
            searchURL = args.getString("url");
        }
        return new SearchLoader(this, searchURL);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null) {
            DataUtils.SearchResult searchResult = DataUtils.parseSearchResultsJSON(data); //fetch the country name and lat and lng positions

            LatLng newLocation = new LatLng(searchResult.latitude, searchResult.longitude);
            mMap.addMarker(new MarkerOptions().position(newLocation).title(searchResult.country));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));

            insertNewLocation(searchResult); //add values to be saved in database
            loadOverlays(true);
        } else {

        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Nothing to do...
    }

    private long insertNewLocation(DataUtils.SearchResult searchResult) {
        if (searchResult != null) {
            ContentValues row = new ContentValues();
            row.put(LocationContract.Locations.COLUMN_COUNTRY_NAME, searchResult.country);
            row.put(LocationContract.Locations.COLUMN_LATITUDE, searchResult.latitude);
            row.put(LocationContract.Locations.COLUMN_LONGITUDE, searchResult.longitude);
            return locationDB.insert(LocationContract.Locations.TABLE_NAME, null, row);
        } else {
            return -1;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadOverlays(boolean reload) {
        ArrayList<String> savedCountries = getAllContryNamesFromDatabase();
        Log.d(TAG,CountryOutlinesUtils.buildLocationInString(savedCountries));
        String url = CountryOutlinesUtils.buildFusionTablesQuery(CountryOutlinesUtils.buildLocationInString(savedCountries));
        Bundle args = new Bundle();
        args.putString(FusionTableLoaderManager.FUSION_TABLE_URL_KEY, url);
        if(reload) {
            getSupportLoaderManager().restartLoader(FUSION_TABLE_LOADER_ID, args, mFusionTableLoaderManager);
        } else {
            getSupportLoaderManager().initLoader(FUSION_TABLE_LOADER_ID, args, mFusionTableLoaderManager);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_visited_wishlist:
                initVisitedAndWishListActivity();
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.nav_visited:
                initVisitedActivity();
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.nav_wishlist:
                initWishListActivity();
                mDrawerLayout.closeDrawers();
                return true;
            default:
                return false;
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnInfoWindowClickListener(new OnInfoWindowClickedListener());
        mCurrentSelectionMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0,0)).alpha(0).infoWindowAnchor((float)0.5, 1));

        loadOverlays(false);

        GeoJsonLayer layer = null;


        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        // Implemented Zoom Controls to allow for easier navigation on the emulator
        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
    }

    private class FusionTableLoaderManager implements LoaderManager.LoaderCallbacks<String> {
        public static final String FUSION_TABLE_URL_KEY = "fusionTableURL";
        private ArrayList<CountryOutlinesUtils.LayerInfo> mFusionTableResults = null;

        @Override
        public Loader<String> onCreateLoader(int id, Bundle args) {
            String fusionTableURL = null;
            if(args != null) {
                fusionTableURL = args.getString(FUSION_TABLE_URL_KEY);
            }
            return new FusionTableLoader(MapsActivity.this, fusionTableURL);
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            if(data != null) {
                mFusionTableResults = CountryOutlinesUtils.getGeoJsonCoordinates(data);
                Log.d(TAG,data);
                if(mFusionTableResults!=null) {
                    Log.d(TAG,mFusionTableResults.toString());
                    deleteOverlays();
                    createOverlays(mFusionTableResults);
                    addOverlays();
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }

    }

    private class OnLayerClickListener implements GeoJsonLayer.GeoJsonOnFeatureClickListener {

        @Override
        public void onFeatureClick(Feature feature) {
            //Log.d(TAG, feature.toString());
            //Log.d(TAG,feature.getProperties().toString());
                countryClicked(feature.getProperty("Name"));
        }
    }

    /**
     * Add functionality here for clicking on the information window for markers
     */
    private class OnInfoWindowClickedListener implements GoogleMap.OnInfoWindowClickListener {
        @Override
        public void onInfoWindowClick(Marker marker) {
            Log.d(TAG, marker.getTitle() + "'s info window clicked");
            //Implement info window clicked
        }
    }

    public void countryClicked(String countryName) {
        Log.d(TAG, countryName + " Clicked");
        LatLng location = getLattitudeAndLongitudeOfCountryFromDB(countryName);
        mCurrentSelectionMarker.setPosition(location);
        mCurrentSelectionMarker.setTitle(countryName);
        mCurrentSelectionMarker.setSnippet("This country is called " + countryName);    //Info Window Snippet
        mCurrentSelectionMarker.showInfoWindow();
    }
}
