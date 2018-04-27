package me.ghini.pocket;

import android.app.Activity;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.util.Log;

import java.io.File;

import static android.content.Context.MODE_PRIVATE;
import static me.ghini.pocket.MainActivity.BINOMIAL;
import static me.ghini.pocket.MainActivity.DB_LOCATION_CODE;
import static me.ghini.pocket.MainActivity.LOCATION_CODE;
import static me.ghini.pocket.MainActivity.NO_OF_PICS;
import static me.ghini.pocket.MainActivity.NO_OF_PLANTS;
import static me.ghini.pocket.MainActivity.PICTURE_NAMES;
import static me.ghini.pocket.MainActivity.PLANT_CODE;

/**
 * Created by mario on 2018-04-26.
 */

public class PocketSource implements TableSource {
    private final Activity activity;

    PocketSource(Activity activity) {
        this.activity = activity;
    }

    private void createLocalEditsTable() {
        String filename = new File(activity.getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        SQLiteDatabase database = activity.openOrCreateDatabase(filename, MODE_PRIVATE, null);
        String query = "CREATE TABLE local_edits " +
                "(_id INTEGER PRIMARY KEY," +
                " code TEXT, location TEXT, " +
                " taxon TEXT, quantity INTEGER," +
                " lat FLOAT, lon FLOAT, pictures INTEGER)";
        Cursor resultSet = database.rawQuery(query, new String[]{});
        resultSet.moveToFirst();
        resultSet.close();
    }

    @Override
    public Bundle getAt(int id) {
        Bundle result = new Bundle();
        String query = "SELECT taxon, quantity, pictures, " +
                "lat, lon, location " +
                "FROM local_edits " +
                "WHERE _id = ? ";
        try {
            String filename = new File(activity.getExternalFilesDir(null), "pocket.db").getAbsolutePath();
            SQLiteDatabase database = activity.openOrCreateDatabase(filename, MODE_PRIVATE, null);
            Cursor resultSet = database.rawQuery(query, new String[]{String.valueOf(id)});
            resultSet.moveToFirst();
            if(resultSet.getString(0) != null) {
                result.putString(BINOMIAL, resultSet.getString(0));
            }
            if(resultSet.getString(1) != null) {
                result.putString(NO_OF_PLANTS, String.valueOf(resultSet.getInt(1)));
            }
            if(resultSet.getString(2) != null) {
                result.putString(NO_OF_PICS, String.valueOf(resultSet.getInt(2)));
            }
            if(resultSet.getString(5) != null) {
                result.putString(DB_LOCATION_CODE, resultSet.getString(5));
            }
            resultSet.close();
        } catch (CursorIndexOutOfBoundsException ignore) {
        } catch (SQLiteException e) {
            createLocalEditsTable();
        }
        return result;
    }

    @Override
    public Bundle getAt(String lookup) {
        try {
            String filename = new File(activity.getExternalFilesDir(null), "pocket.db").getAbsolutePath();
            SQLiteDatabase database = activity.openOrCreateDatabase(filename, MODE_PRIVATE, null);
            String query = "SELECT _id " +
                    "FROM local_edits " +
                    "WHERE code = ? ";
            Cursor resultSet = database.rawQuery(query, new String[]{lookup});
            resultSet.moveToFirst();
            int id = resultSet.getInt(0);
            resultSet.close();
            return getAt(id);
        } catch (CursorIndexOutOfBoundsException e) {
            return new Bundle();
        } catch (SQLiteException e) {
            createLocalEditsTable();
            return new Bundle();
        }
    }

    @Override
    public void putAt(int id, Bundle bundle) {
        String filename = new File(activity.getExternalFilesDir(null), "pocket.db").
                getAbsolutePath();
        try {
            SQLiteDatabase database = activity.openOrCreateDatabase(filename, MODE_PRIVATE, null);
            String query = "UPDATE plant SET edit_pending=1 WHERE _id=?";
            Cursor resultSet = database.rawQuery(query, new String[]{String.valueOf(id)});
            resultSet.moveToLast();
            resultSet.close();

            query = "INSERT OR REPLACE INTO local_edits " +
                    "(_id, code, taxon, location, quantity, lat, lon, pictures) " +
                    "VALUES (?, " +
                    "  COALESCE(?, (SELECT code FROM local_edits WHERE _id=" + id + ")), " +
                    "  COALESCE(?, (SELECT taxon FROM local_edits WHERE _id=" + id + ")), " +
                    "  COALESCE(?, (SELECT location FROM local_edits WHERE _id=" + id + ")), " +
                    "  COALESCE(?, (SELECT quantity FROM local_edits WHERE _id=" + id + ")), " +
                    "  COALESCE(?, (SELECT lat FROM local_edits WHERE _id=" + id + ")), " +
                    "  COALESCE(?, (SELECT lon FROM local_edits WHERE _id=" + id + ")), " +
                    "  COALESCE(?, (SELECT pictures FROM local_edits WHERE _id=" + id + ")) " +
                    ")";
            SQLiteStatement statement = database.compileStatement(query);
            statement.bindLong(1, id);
            statement.bindString(2, bundle.getString(PLANT_CODE));
            if (bundle.getString(BINOMIAL).length() > 0) {
                statement.bindString(3, bundle.getString(BINOMIAL));
            } else {
                statement.bindNull(3);
            }
            if (bundle.getString(LOCATION_CODE).length() > 0) {
                statement.bindString(4, bundle.getString(LOCATION_CODE));
            } else {
                statement.bindNull(4);
            }
            if (bundle.getString(NO_OF_PLANTS).length() > 0) {
                statement.bindLong(5, Long.parseLong(bundle.getString(NO_OF_PLANTS)));
            } else {
                statement.bindNull(5);
            }
            if (bundle.containsKey("latitude")) {
                statement.bindDouble(6, bundle.getDouble("latitude"));
                statement.bindDouble(7, bundle.getDouble("longitude"));
            } else {
                statement.bindNull(6);
                statement.bindNull(7);
            }
            int local_pics = bundle.getStringArrayList(PICTURE_NAMES).size();
            if (local_pics > 0) {
                statement.bindLong(8, local_pics);
            } else {
                statement.bindNull(8);
            }
            statement.execute();
        }
        catch (Exception ignore) {
            Log.e("pocket", ignore.getLocalizedMessage());
        }
    }
}
