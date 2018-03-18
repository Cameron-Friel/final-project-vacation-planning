package cs492.vacationplanner;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.data.geojson.GeoJsonFeature;
import com.google.maps.android.data.geojson.GeoJsonLayer;
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final int FUSION_TABLE_LOADER_ID = 1;

    private GoogleMap mMap;

    private FusionTableLoaderManager mFusionTableLoaderManager = new FusionTableLoaderManager();


    private ArrayList<GeoJsonLayer> mCountryOverlays = new ArrayList<GeoJsonLayer>();

    private ArrayList<Float> tempLats = new ArrayList<Float>();
    private ArrayList<Float> tempLons = new ArrayList<Float>();
    private ArrayList<String> tempNames = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        setTemps();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    private void createOverlays(ArrayList<JSONObject> borderData) {
        GeoJsonLayer layer = null;
        for(int i=0;i<borderData.size();i++)
        {
            Log.d(TAG,"Creating Overlay");
            layer = new GeoJsonLayer(mMap,borderData.get(i));
            for (GeoJsonFeature eFeature : layer.getFeatures()) {
                GeoJsonPolygonStyle style = new GeoJsonPolygonStyle();
                style.setFillColor(0xff00ff00);
                style.setStrokeColor(0xff000000);
                style.setStrokeWidth(2);
                eFeature.setPolygonStyle(style);
            }
            mCountryOverlays.add(layer);
        }
    }

    private void addOverlays() {
        for(GeoJsonLayer layer : mCountryOverlays) {
            layer.addLayerToMap();
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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        String url = CountryOutlinesUtils.buildFusionTablesQuery("'Germany', 'Japan', 'Italy', 'Aruba', 'Mexico', 'Canada'");
        Bundle args = new Bundle();
        args.putString(FusionTableLoaderManager.FUSION_TABLE_URL_KEY, url);
        getSupportLoaderManager().initLoader(FUSION_TABLE_LOADER_ID, args, mFusionTableLoaderManager);

        GeoJsonLayer layer = null;


        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        addSavedLocations();
    }

    private class FusionTableLoaderManager implements LoaderManager.LoaderCallbacks<String> {
        public static final String FUSION_TABLE_URL_KEY = "fusionTableURL";
        private ArrayList<JSONObject> mFusionTableResults = null;

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
                    createOverlays(mFusionTableResults);
                    addOverlays();
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<String> loader) {

        }
    }

}
