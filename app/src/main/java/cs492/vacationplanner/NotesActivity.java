package cs492.vacationplanner;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.view.View;
import android.view.Menu;



/**
 * Created by Cameron on 3/3/2018.
 */

public class NotesActivity extends AppCompatActivity {

    private TextView mNoteName;
    private TextView mNoteDescription;

    private SQLiteDatabase locationWritableDB; //connection to write values from database
    private SQLiteDatabase locationReadableDB; //connection to read values from database

    private String mDBResult;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        LocationContractHelper dbHelper = new LocationContractHelper(this);
        locationWritableDB = dbHelper.getWritableDatabase();
        locationReadableDB = dbHelper.getReadableDatabase();

        // Initialize variables to hold the location (mNoteName) and the note itself (mNoteDescription)
        mNoteName = (TextView)findViewById(R.id.tv_note_name);
        mNoteDescription = (EditText)findViewById(R.id.tv_note_description);

        Intent intent = getIntent();
        if (intent != null) {
            mDBResult = (String)intent.getSerializableExtra("LOCATION");
            // Set the text within the note as well as the name of the note with info from the database
            mNoteName.setText(mDBResult);
            mNoteDescription.setText(getDataFromDB(mDBResult));
        }

        final Intent cancel = new Intent(this, MapsActivity.class);

        Button cancelButton = (Button)findViewById(R.id.button_cancel);
        Button saveButton = (Button)findViewById(R.id.button_save);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(cancel);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String note = mNoteDescription.getText().toString();
                updateLocationDB(mDBResult, note);
                mNoteDescription.clearFocus();
            }
        });
    }

    @Override
    protected void onDestroy() {
        locationReadableDB.close();
        locationWritableDB.close();
        super.onDestroy();
    }

    private String getDataFromDB(String location) {
        String sqlSelection = LocationContract.Locations.COLUMN_COUNTRY_NAME + " = ?";
        String[] sqlSelectionArgs = { location };

        Cursor cursor = locationReadableDB.query(
                LocationContract.Locations.TABLE_NAME,
                null,
                sqlSelection,
                sqlSelectionArgs,
                null,
                null,
                null
                );

        cursor.moveToNext();
        String notes = cursor.getString(cursor.getColumnIndex(LocationContract.Locations.COLUMN_NOTES));
        cursor.close();

        return notes;
    }

    private void updateLocationDB(String location, String notes) {
        if (notes != null) {
            ContentValues row = new ContentValues();
            row.put(LocationContract.Locations.COLUMN_NOTES, notes);

            String sqlSelection = LocationContract.Locations.COLUMN_COUNTRY_NAME + " = ?";
            String[] sqlSelectionArgs = { location };

            locationWritableDB.update(LocationContract.Locations.TABLE_NAME, row, sqlSelection, sqlSelectionArgs);
        }
    }

    public void shareNotes(String notes, String location) {
        if (notes != null) {
            String shareText = getString(R.string.share_text_prefix) + " " +
                    location + ":\n" + notes;

            ShareCompat.IntentBuilder.from(this)
                    .setChooserTitle(R.string.share_chooser_title)
                    .setType("text/plain")
                    .setText(shareText)
                    .startChooser();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.notes_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareNotes(mNoteDescription.getText().toString(), mNoteName.getText().toString());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
