package cs492.vacationplanner;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import cs492.vacationplanner.Utils.DataUtils;
import cs492.vacationplanner.Utils.NetworkUtils;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, LoaderManager.LoaderCallbacks<String> {

    private GoogleMap mMap;

    private SearchView search; //object to handle user searches

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        switch (item.getItemId()) {
            case R.id.instance_notes:
                initNotesActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void initNotesActivity() { //preliminary notes activity init. In the future this needs to send a bundle that contains country specified data
        Intent notesIntent = new Intent(this, NotesActivity.class);
        startActivity(notesIntent);
    }

    public void createMapSearch(String locationURL) {
        Bundle args = new Bundle();
        args.putString("url", locationURL);
        //mLoadingProgressBar.setVisibility(View.VISIBLE);
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

    @Override
    public void onLoadFinished(Loader<String> loader, String data) {
        if (data != null) {
            DataUtils.SearchResult searchResult = DataUtils.parseSearchResultsJSON(data); //fetch the country name and lat and lng positions

            LatLng newLocation = new LatLng(searchResult.latitude, searchResult.longitude);
            mMap.addMarker(new MarkerOptions().position(newLocation).title(searchResult.country));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
        } else {

        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {
        // Nothing to do...
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }
}
