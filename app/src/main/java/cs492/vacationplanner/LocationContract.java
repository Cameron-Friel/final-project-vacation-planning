package cs492.vacationplanner;

import android.provider.BaseColumns;

/**
 * Created by Cameron on 3/3/18.
 */

public class LocationContract {
    private LocationContract() {

    }

    public static class Locations implements BaseColumns {
        public static final String TABLE_NAME = "locations";
        public static final String COLUMN_COUNTRY_NAME = "country";
        public static final String COLUMN_LATITUDE = "latitude";
        public static final String COLUMN_LONGITUDE = "longitude";
        public static final String COLUMN_LIST_OPTION = "list_option";
        public static final String COLUMN_NOTES = "notes";
        public static final String COLUMN_TIMESTAMP = "timestamp";
    }
}
