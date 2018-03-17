package cs492.vacationplanner;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.MenuPopupWindow;
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

public class visited_wishlist_activity extends AppCompatActivity implements visited_wishlist_adapter.OnListItemClickListener {

    private RecyclerView mVWListRecyclerView;
    private visited_wishlist_adapter mAdapter;
    private SQLiteDatabase mDB;
    public final static String EXTRA_LOCATION = "LOCATION";
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

        Intent intent = getIntent();

        if (intent.hasExtra(MapsActivity.VISITED_TITLE_KEY)) {
            //add visited countries to the recycler view
            mAdapter.updateVWList(getVisitedLocationFromDB());
            mListType.setText(intent.getStringExtra(MapsActivity.VISITED_TITLE_KEY));
            mVWListRecyclerView.setAdapter(mAdapter);
        }
        else if (intent.hasExtra(MapsActivity.WISH_LIST_TITLE_KEY)) {
            //add wish list countries to the recycler view
            mAdapter.updateVWList(getWishListLocationFromDB());
            mListType.setText(intent.getStringExtra(MapsActivity.WISH_LIST_TITLE_KEY));
            mVWListRecyclerView.setAdapter(mAdapter);
        }
        else {
            //add both wish and visited list to the recycler view
            mAdapter.updateVWList(getAllSavedLocationFromDB());
            mListType.setText("Visited and Wish List");
            mVWListRecyclerView.setAdapter(mAdapter);
        }
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
                null
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
        String selection = "list_option = ?";
        String[] selectionArgs = {"Visited"};
        Cursor cursor = mDB.query(
                LocationContract.Locations.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                LocationContract.Locations.COLUMN_TIMESTAMP + " DESC"

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

    private ArrayList<String> getWishListLocationFromDB() {
        String selection = "list_option = ?";
        String[] selectionArgs = {"Wish List"};
        Cursor cursor = mDB.query(
                LocationContract.Locations.TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                LocationContract.Locations.COLUMN_TIMESTAMP + " DESC"

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
}
