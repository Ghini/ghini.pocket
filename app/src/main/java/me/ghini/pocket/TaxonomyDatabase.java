package me.ghini.pocket;
/*
  This file is part of ghini.pocket.

  ghini.pocket is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option)
  any later version.

  ghini.pocket is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  for more details.

  You should have received a copy of the GNU General Public License along
  with ghini.pocket.  If not, see <http://www.gnu.org/licenses/>.
 */

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
 * Copyright © 2018 Mario Frasca. <mario@anche.no>
 *
 * Created by mario on 2018-04-01.
 */

class TaxonomyDatabase extends SQLiteAssetHelper {

    private static final String DATABASE_NAME = "taxonomy.db";
    private static final int DATABASE_VERSION = 11;

    TaxonomyDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    List<Epithet> getAllGenera() {
        ArrayList<Epithet> r = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(
                "select o.epithet, a.epithet, o.family_name, o.metaphone " +
                        "from taxon o "+"" +
                        "left join taxon a on o.accepted_id = a.id " +
                        "where o.rank = 5 " +
                        "order by o.epithet",
                new String[]{});
        while (cr.moveToNext()) {
            String epithet = cr.getString(0);
            String accepted = cr.getString(1);
            String family = cr.getString(2);
            String phonetic = cr.getString(3);
            r.add(new Epithet(epithet, family, accepted, phonetic));
        }
        cr.close();
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

    private void computeFamilyNameForGenera(PrintWriter out) {
        String innerQuery =
                "select epithet, rank, parent_id, accepted_id " +
                        "from taxon " +
                        "where id = ? " +
                        "order by epithet";
        SQLiteDatabase db = getReadableDatabase();
        Cursor cr = db.rawQuery(
                "select o.epithet, a.epithet, o.parent_id, o.metaphone, o.id " +
                        "from taxon o "+"" +
                        "left join taxon a on o.accepted_id = a.id " +
                        "where o.rank = 5 " +
                        "order by o.epithet",
                new String[]{});
        while (cr.moveToNext()) {
            //String epithet = cr.getString(0);
            String accepted = cr.getString(1);
            int parent_id = cr.getInt(2);
            //String phonetic = cr.getString(3);
            int self_id = cr.getInt(4);
            String family = "";
            boolean done = accepted != null;
            Cursor cr2;
            int rank;
            int accepted_id;
            while(!done) {
                cr2 = db.rawQuery(
                        innerQuery,
                        new String[]{Integer.toString(parent_id)});
                cr2.moveToFirst();
                rank = cr2.getInt(1);
                accepted_id = cr2.getInt(3);
                parent_id = cr2.getInt(2);
                if (rank == 1 && accepted_id == 0) {
                    family = cr2.getString(0);
                    done = true;
                } else if(accepted_id != 0) {
                    parent_id = accepted_id;
                }
                cr2.close();
            }
            out.println("update taxon set family_name='" + family + "' where id=" + self_id + ";");
        }
        cr.close();
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
                out.println(String.format("update taxon set metaphone='%s' where id=%s;", metaphone, id));
            }
            computeFamilyNameForGenera(out);
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

