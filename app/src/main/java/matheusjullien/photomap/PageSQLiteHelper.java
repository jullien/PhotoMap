package matheusjullien.photomap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class PageSQLiteHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Pages.db";

    public static final String TABLE_NAME = "pages";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_PATH = "path";
    public static final String COLUMN_NAME_LATITUDE = "latitude";
    public static final String COLUMN_NAME_LONGITUDE = "longitude";
    public static final String COLUMN_NAME_DATE = "date";
    public static final String COLUMN_NAME_TYPE = "type";

    private static final String SQL_CREATE_ENTRIES =
            "create table " + TABLE_NAME + "("
            + COLUMN_NAME_ID	+ " integer primary key autoincrement, "
            + COLUMN_NAME_PATH	+ " text, "
            + COLUMN_NAME_LATITUDE + " double, "
            + COLUMN_NAME_LONGITUDE + " double, "
            + COLUMN_NAME_DATE + " text, "
            + COLUMN_NAME_TYPE + " text);";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

    public PageSQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
