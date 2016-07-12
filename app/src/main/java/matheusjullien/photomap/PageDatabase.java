package matheusjullien.photomap;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class PageDatabase {
        private SQLiteDatabase database;
    private PageSQLiteHelper dbHelper;

    private String[] allColumns = {
            PageSQLiteHelper.COLUMN_NAME_ID,
            PageSQLiteHelper.COLUMN_NAME_PATH,
            PageSQLiteHelper.COLUMN_NAME_LATITUDE,
            PageSQLiteHelper.COLUMN_NAME_LONGITUDE,
            PageSQLiteHelper.COLUMN_NAME_DATE,
            PageSQLiteHelper.COLUMN_NAME_TYPE };

    private ArrayList<Page> pageArrayList;
    private Page p;
    private Cursor c;
    private ContentValues v;

    public PageDatabase(Context context) {
        dbHelper = new PageSQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public boolean isOpen() {
        if (database.isOpen()) {
            return true;
        } else {
            return false;
        }
    }

    public void insertPage(Page page) {
        v = new ContentValues();
        v.put(PageSQLiteHelper.COLUMN_NAME_PATH, page.getPath());
        v.put(PageSQLiteHelper.COLUMN_NAME_LATITUDE, page.getLatitude());
        v.put(PageSQLiteHelper.COLUMN_NAME_LONGITUDE, page.getLongitude());
        v.put(PageSQLiteHelper.COLUMN_NAME_DATE, page.getDate());
        v.put(PageSQLiteHelper.COLUMN_NAME_TYPE, page.getType());

        database.insert(PageSQLiteHelper.TABLE_NAME, null, v);
    }

    public void deletePage(String path) {
        c = database.query(PageSQLiteHelper.TABLE_NAME, allColumns, PageSQLiteHelper.COLUMN_NAME_PATH + "=?", new String[] { path }, null, null, null, null);

        if (c.moveToFirst()) {
            database.delete(PageSQLiteHelper.TABLE_NAME, PageSQLiteHelper.COLUMN_NAME_PATH + "=?", new String[] { path });
        }
    }

    public ArrayList<Page> getAllPages() {
        pageArrayList = new ArrayList<>();

        c = database.query(PageSQLiteHelper.TABLE_NAME, allColumns, null, null, null, null, PageSQLiteHelper.COLUMN_NAME_DATE + " DESC");

        c.moveToFirst();
        while (!c.isAfterLast()) {
            p = cursorToPage(c);
            pageArrayList.add(p);
            c.moveToNext();
        }

        return pageArrayList;
    }

    public ArrayList<Page> getPagesByLatLng(LatLng latlng) {
        pageArrayList = new ArrayList<>();

        c = database.query(PageSQLiteHelper.TABLE_NAME, allColumns, PageSQLiteHelper.COLUMN_NAME_LATITUDE + "=? AND " + PageSQLiteHelper.COLUMN_NAME_LONGITUDE + "=?", new String[] { String.valueOf(latlng.latitude), String.valueOf(latlng.longitude) }, null, null, PageSQLiteHelper.COLUMN_NAME_DATE + " DESC");

        c.moveToFirst();
        while (!c.isAfterLast()) {
            p = cursorToPage(c);
            pageArrayList.add(p);
            c.moveToNext();
        }

        return pageArrayList;
    }

    private Page cursorToPage(Cursor c) {
        p = new Page();

        p.setId(c.getInt(c.getColumnIndex(PageSQLiteHelper.COLUMN_NAME_ID)));
        p.setPath(c.getString(c.getColumnIndex(PageSQLiteHelper.COLUMN_NAME_PATH)));
        p.setLatitude(c.getDouble(c.getColumnIndex(PageSQLiteHelper.COLUMN_NAME_LATITUDE)));
        p.setLongitude(c.getDouble(c.getColumnIndex(PageSQLiteHelper.COLUMN_NAME_LONGITUDE)));
        p.setDate(c.getString(c.getColumnIndex(PageSQLiteHelper.COLUMN_NAME_DATE)));
        p.setType(c.getString(c.getColumnIndex(PageSQLiteHelper.COLUMN_NAME_TYPE)));

        return p;
    }
}
