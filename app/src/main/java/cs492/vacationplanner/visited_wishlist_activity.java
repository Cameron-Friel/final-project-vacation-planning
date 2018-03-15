package cs492.vacationplanner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.ArrayList;

public class visited_wishlist_activity extends AppCompatActivity
        implements visited_wishlist_adapter.OnListItemClickListener,
            NavigationView.OnNavigationItemSelectedListener{

    private RecyclerView mVWListRecyclerView;
    private visited_wishlist_adapter mAdapter;
    private SQLiteDatabase mDB;
    public final static String EXTRA_LOCATION = "LOCATION";
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView mListType;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visited_wishlist_activity);

        mVWListRecyclerView = findViewById(R.id.rv_visited_wishlist);
        mVWListRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mVWListRecyclerView.setHasFixedSize(true);
        mListType = findViewById(R.id.tv_list_type);

        LocationContractHelper dbHelper = new LocationContractHelper(this);
        mDB = dbHelper.getReadableDatabase();

        mAdapter = new visited_wishlist_adapter(this);
        mAdapter.updateVWList(getAllSavedLocationFromDB());
        mListType.setText("Visited and Wishlist!");
        mVWListRecyclerView.setAdapter(mAdapter);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationView navigationView = findViewById(R.id.nv_navigation_drawer);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    protected void onDestroy() {
        mDB.close();
        super.onDestroy();
    }

    private ArrayList<String> getAllSavedLocationFromDB() {
        Cursor cursor = mDB.query(
                LocationContract.Locations.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                LocationContract.Locations.COLUMN_TIMESTAMP + "DESC"
        );
        ArrayList<String> listLocation = new ArrayList<>();
        while(cursor.moveToNext()){
            String location;
            location = cursor.getString(
                    cursor.getColumnIndex(LocationContract.Locations.COLUMN_COUNTRY_NAME)
            );
            listLocation.add(location);
        }
        cursor.close();
        return listLocation;
    }

    private ArrayList<String> getVisitedLocationFromDB() {
        String selection = "visited_wishlist =?";
        String[] selectionArgs = {"visited"};
        Cursor cursor = mDB.query(
                LocationContract.Locations.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                LocationContract.Locations.COLUMN_TIMESTAMP + "DESC"

        );
        ArrayList<String> listLocation = new ArrayList<>();
        while(cursor.moveToNext()){
            String location;
            location = cursor.getString(
                    cursor.getColumnIndex(LocationContract.Locations.COLUMN_COUNTRY_NAME)
            );
            listLocation.add(location);
        }
        cursor.close();
        return listLocation;
    }

    private ArrayList<String> getWishlistLocationFromDB() {
        String selection = "visited_wishlist =?";
        String[] selectionArgs = {"visited"};
        Cursor cursor = mDB.query(
                LocationContract.Locations.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                LocationContract.Locations.COLUMN_TIMESTAMP + "DESC"

        );
        ArrayList<String> listLocation = new ArrayList<>();
        while(cursor.moveToNext()){
            String location;
            location = cursor.getString(
                    cursor.getColumnIndex(LocationContract.Locations.COLUMN_COUNTRY_NAME)
            );
            listLocation.add(location);
        }
        cursor.close();
        return listLocation;
    }

    @Override
    public void onListItemClick(String location) {
        Intent noteIntent = new Intent(this, NotesActivity.class);
        noteIntent.putExtra(EXTRA_LOCATION, location);
        startActivity(noteIntent);
    }

    public boolean onOptionItemSelected(MenuItem item){
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_visited_wishlist:
                mAdapter.updateVWList(getAllSavedLocationFromDB());
                mListType.setText("Visited and Wishlist!");
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.nav_visited:
                mAdapter.updateVWList(getVisitedLocationFromDB());
                mListType.setText("Visited!");
                mDrawerLayout.closeDrawers();
                return true;
            case R.id.nav_wishlist:
                mAdapter.updateVWList(getWishlistLocationFromDB());
                mListType.setText("Wishlist!");
                mDrawerLayout.closeDrawers();
                return true;
            default:
                return false;
        }
    }
}
