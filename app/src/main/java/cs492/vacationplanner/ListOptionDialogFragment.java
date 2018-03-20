package cs492.vacationplanner;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.Toast;

import cs492.vacationplanner.Utils.DataUtils;

/**
 * Created by Cameron on 3/15/2018.
 */

public class ListOptionDialogFragment extends AppCompatDialogFragment {

    private DataUtils.SearchResult countryInfo; //holds user's current chosen country information

    private String selectedOptionHolder; //stores the user's selected option

    private SQLiteDatabase locationWritableDB; //connection to write values from database
    private SQLiteDatabase locationReadableDB; //connection to read values from database

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        countryInfo = (DataUtils.SearchResult)getArguments().getSerializable(MapsActivity.COUNTRY_DATA_KEY); //fetch user's country data from parent activity

        //set up database connections to store user's data into the database

        LocationContractHelper dbHelper = new LocationContractHelper(getContext());
        locationWritableDB = dbHelper.getWritableDatabase();
        locationReadableDB = dbHelper.getReadableDatabase();
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final CharSequence [] listOptions = {"Visited", "Wish List"}; //array of radio button options to be displayed to user when country is chosen

        selectedOptionHolder = null; //set to null so user cannot accept without giving option

        builder.setView(inflater.inflate(R.layout.list_option_layout, null)).setTitle("Selection for " + countryInfo.country)
                .setSingleChoiceItems(listOptions, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (i == 0) { //visited option is selected
                            selectedOptionHolder = (String)listOptions[i];
                        }
                        else if (i == 1) { //wish list option is selected
                            selectedOptionHolder = (String)listOptions[i];
                        }
                    }
                }).setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //add result to database depending on result
                        if (selectedOptionHolder != null) { //user gave an option
                            insertNewLocation(selectedOptionHolder, countryInfo);
                        }
                        else { //user pressed accept without choosing an option
                            Toast displayErrorMessage = Toast.makeText(getContext(), "Please select an option before pressing accept.", Toast.LENGTH_LONG);
                            displayErrorMessage.setGravity(Gravity.CENTER, 0, 0);
                            displayErrorMessage.show(); //display error message asking for user to give an option
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing, user wishes to exit dialog fragment
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

    private long insertNewLocation(String option, DataUtils.SearchResult searchResult) {
        if (searchResult != null) {
            if (isDuplicateEntry(searchResult.country) == false) { //check if country is already in the database
                ContentValues row = new ContentValues();
                row.put(LocationContract.Locations.COLUMN_COUNTRY_NAME, searchResult.country);
                row.put(LocationContract.Locations.COLUMN_LATITUDE, searchResult.latitude);
                row.put(LocationContract.Locations.COLUMN_LONGITUDE, searchResult.longitude);
                row.put(LocationContract.Locations.COLUMN_LIST_OPTION, option);

                Toast displayResult = Toast.makeText(getContext(), "Added " + searchResult.country + " to your list.", Toast.LENGTH_LONG);
                displayResult.show();
                return locationWritableDB.insert(LocationContract.Locations.TABLE_NAME, null, row);
            }
            else { //location is a duplicate, ignore
                Toast displayResult = Toast.makeText(getContext(), "Unable to add " + searchResult.country + " to your list, it already exists!", Toast.LENGTH_LONG);
                displayResult.show();
                return -1;
            }
        } else {
            return -1;
        }
    }

    public boolean isDuplicateEntry(String countryName) {
        boolean isSaved = false;

        if (countryName != null) {
            String sqlSelection = LocationContract.Locations.COLUMN_COUNTRY_NAME + " = ?";
            String[] sqlSelectionArgs = {countryName};
            Cursor cursor = locationReadableDB.query(
                    LocationContract.Locations.TABLE_NAME,
                    null,
                    sqlSelection,
                    sqlSelectionArgs,
                    null,
                    null,
                    null
            );
            isSaved = cursor.getCount() > 0; //true if there is an entry
            cursor.close();
        }
        return isSaved;
    }

    @Override
    public void onDestroy() { //close connections to database when exiting dialog fragment
        locationReadableDB.close();
        locationWritableDB.close();
        super.onDestroy();
    }
}
