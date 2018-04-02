package me.ghini.pocket;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by mario on 2018-04-01.
 */

class TaxonomyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "taxonomy.db";
    private static final int DATABASE_VERSION = 10;

    TaxonomyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    List<Epithet> getAllGenera() {
        Cursor cr = getMatchingGenera("");
        ArrayList<Epithet> r = new ArrayList<>();
        while (cr.moveToNext()) {
            r.add(new Epithet(cr.getString(0), cr.getString(4)));
        }
        return r;
    }

    Cursor getMatchingGenera(String s) {
        String field;
        if(s.toUpperCase().equals(s)) {
            field = "metaphone";
            s = shorten(s);
        } else {
            field = "epithet";
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = null;
        for (int rank = 5; rank >= 0; rank--) {
            c = db.rawQuery(
                    "select epithet, authorship, accepted_id, parent_id, metaphone " +
                            "from taxon " +
                            "where rank = ? and " + field + " like ? " +
                            "order by epithet",
                    new String[]{Integer.toString(rank), s + "%"});
            if (c.getCount() != 0)
                return c;
        }
        return c;
    }

    /**
     * return a shortened, simplified, typo-tolerant name, corresponding to the given epithet
     * @param epithet the epithet to simplify
     * @return the typo-tolerant equivalent
     */
    static String shorten(String epithet) {
        epithet = epithet.toLowerCase();  // ignore case
        epithet = epithet.replaceAll("-", "");  // remove hyphen
        epithet = epithet.replaceAll("c([yie])", "z$1");  // palatal c sounds like z
        epithet = epithet.replaceAll("g([ie])", "j$1");  // palatal g sounds like j
        epithet = epithet.replaceAll("ph", "f");  // ph sounds like f
        epithet = epithet.replaceAll("v", "f");  // v sounds like f // fricative (voiced or not)

        epithet = epithet.replaceAll("h", "");  // h sounds like nothing
        epithet = epithet.replaceAll("[gcq]", "k");  // g, c, q sound like k // guttural
        epithet = epithet.replaceAll("[xz]", "s");  // x, z sound like s
        epithet = epithet.replaceAll("ae", "e");  // ae sounds like e
        epithet = epithet.replaceAll("[ye]", "i");  // y, e sound like i
        epithet = epithet.replaceAll("[ou]", "u");  // o, u sound like u // so we only have a, i, u
        epithet = epithet.replaceAll("[aiu]([aiu])[aiu]*", "$1");  // remove diphtongs
        epithet = epithet.replaceAll("(.)\\1", "$1");  // doubled letters sound like single
        return epithet;
    }

    Cursor getParentTaxon(Integer lookupId) {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT epithet, authorship, accepted_id, parent_id " +
                "FROM taxon " +
                "WHERE id=?";
        Cursor c = db.rawQuery(query,
                new String[] {lookupId.toString()});

        c.moveToFirst();
        return c;
    }

    void updateMetaphone(String dst) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        Cursor c1 = db.rawQuery("select id, epithet from taxon", null);
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(dst, true)));
            out.println("begin transaction;");
            while (c1.moveToNext()) {
                Integer id = c1.getInt(0);
                String epithet = c1.getString(1);
                String metaphone = shorten(epithet);
                out.println(String.format("updateView taxon set metaphone='%s' where id=%s;", metaphone, id));
            }
            out.println("commit;");
        } catch (IOException e1) {
            e1.printStackTrace();
        } finally {
            if(out != null){
                out.close();
            }
        }
        c1.close();
        db.endTransaction();
        db.close();
    }
}

