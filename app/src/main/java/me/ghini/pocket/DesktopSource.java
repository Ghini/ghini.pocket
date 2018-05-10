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

import android.app.Activity;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Toast;

import java.io.File;
import java.io.NotActiveException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;
import static me.ghini.pocket.MainActivity.ACQUISITION;
import static me.ghini.pocket.MainActivity.BINOMIAL;
import static me.ghini.pocket.MainActivity.DB_LOCATION_CODE;
import static me.ghini.pocket.MainActivity.DISMISSAL;
import static me.ghini.pocket.MainActivity.FAMILY;
import static me.ghini.pocket.MainActivity.GENUS;
import static me.ghini.pocket.MainActivity.NO_OF_PICS;
import static me.ghini.pocket.MainActivity.NO_OF_PLANTS;
import static me.ghini.pocket.MainActivity.OVERRIDE;
import static me.ghini.pocket.MainActivity.PLANT_CODE;
import static me.ghini.pocket.MainActivity.PLANT_ID;
import static me.ghini.pocket.MainActivity.SOURCE;

/**
 * Copyright © 2018 Mario Frasca. <mario@anche.no>
 *
 * Created by mario on 2018-04-26.
 */

public class DesktopSource implements TableSource {

    private final Activity activity;

    DesktopSource(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Bundle getAt(String lookup) {
        Bundle bundle = new Bundle();

        String family = "";
        String acqDate = "";
        String source = "";
        String location = "";
        String dismissDate = "";
        String noOfPics = "";
        String noOfPlants = "";
        String species = "";
        int editPending = 0;
        int plantId = 0;

        String genus_epithet = "";
        String accessionCode;
        String plantCode = ".1";  // default value if not specified by user
        /* Accession patterns we are willing to recognize:
           CCYY.####;  // itf2 standard (year.sequential)
           CCYY.##.### // tanager (year.lot.sequential)
           ######;     // quito (sequential)
           full Accession.Plant is one of the above, plus as many as three trailing digits.
         */
        String Itf2Pattern = "[12][0-9][0-9][0-9]\\.[0-9][0-9][0-9][0-9]";
        String TanagerPattern = "[12][0-9][0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9][0-9]";
        String QuitoPattern = "[0-9][0-9][0-9][0-9][0-9][0-9]";

        Pattern pattern = Pattern.compile(
                "^(" + Itf2Pattern + "|" + TanagerPattern + "|" + QuitoPattern + ")\\b" +
                "((?:\\.[1-9][0-9]?[0-9]?)?)$");
        Matcher m = pattern.matcher(lookup);
        if (m.find()) {
            accessionCode = m.group(1);
            if (m.group(2).length() > 0)
                plantCode = m.group(2);
        } else {
            /*  one more attempt, in case there's one or two trailing digit,
                we might assume it's a plant number and the rest is accession code
            */
            m = Pattern.compile("^(.*)(\\.[1-9][0-9]?)$").matcher(lookup);
            if (m.find()) {
                accessionCode = m.group(1);
                plantCode = m.group(2);
            } else {
                /* otherwise it is all accession code */
                accessionCode = lookup;
            }
        }

        bundle.putString(PLANT_CODE, accessionCode + plantCode);

        String filename = new File(activity.getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        if (accessionCode.equalsIgnoreCase("settings")) {
            family = filename;
        } else {
            SQLiteDatabase database = activity.openOrCreateDatabase(filename, MODE_PRIVATE, null);
            String query = "SELECT s.family, s.genus, s.epithet, " +
                    "a.code, p.code, a.source, p.location, " +
                    "a.start_date, p.end_date, p.n_of_pics, p.quantity, p.edit_pending," +
                    "p._id " +
                    "FROM species s, accession a, plant p " +
                    "WHERE p.accession_id = a._id " +
                    "AND a.species_id = s._id " +
                    "AND a.code = ? " +
                    "AND p.code = ? ";
            Cursor resultSet = database.rawQuery(query, new String[]{accessionCode, plantCode});
            try {
                resultSet.moveToFirst();
                family = resultSet.getString(0);
                genus_epithet = resultSet.getString(1);
                String sp_epithet = resultSet.getString(2);
                if (sp_epithet == null)
                    sp_epithet = "";
                if (genus_epithet.startsWith("Zzz"))
                    genus_epithet = "";
                if (genus_epithet.startsWith("Zz") && genus_epithet.length() > 4)
                    genus_epithet = genus_epithet.substring(4);
                if (!genus_epithet.equals("") && !sp_epithet.equals("sp") && !sp_epithet.equals("")) {
                    species = genus_epithet + " " + sp_epithet;
                } else {
                    species = genus_epithet;
                }
                source = resultSet.getString(5);
                location = resultSet.getString(6);
                acqDate = resultSet.getString(7);
                try {
                    acqDate = acqDate.substring(0, 16);
                } catch (Exception ignored) {
                }
                dismissDate = resultSet.getString(8);
                try {
                    dismissDate = dismissDate.substring(0, 16);
                } catch (Exception ignored) {
                }
                noOfPics = String.valueOf(resultSet.getInt(9));
                noOfPlants = String.valueOf(resultSet.getInt(10));
                editPending = resultSet.getInt(11);
                plantId = resultSet.getInt(12);
            } catch (CursorIndexOutOfBoundsException e) {
                bundle.putString(FAMILY, activity.getString(R.string.no_match));
                bundle.putString(BINOMIAL, "");
                bundle.putString(DB_LOCATION_CODE, "");
                bundle.putString(SOURCE, "");
                bundle.putString(NO_OF_PICS, "");
                bundle.putString(ACQUISITION, "");
                bundle.putString(DISMISSAL, "");
                bundle.putBoolean(OVERRIDE, false);
                Toast.makeText(activity, R.string.no_match, Toast.LENGTH_SHORT).show();
            }
            resultSet.close();
        }

        bundle.putString(FAMILY, family);
        bundle.putString(BINOMIAL, species);
        bundle.putString(ACQUISITION, acqDate);
        bundle.putString(SOURCE, source);
        bundle.putString(DB_LOCATION_CODE, location);
        bundle.putString(DISMISSAL, dismissDate);
        bundle.putString(NO_OF_PICS, noOfPics);
        bundle.putBoolean(OVERRIDE, editPending != 0);

        bundle.putString(NO_OF_PLANTS, noOfPlants);
        bundle.putString(GENUS, genus_epithet);

        bundle.putInt(PLANT_ID, plantId);

        return bundle;
    }

    @Override
    public Bundle getAt(int id) {
        return null;
    }

    @Override
    public void putAt(int id, Bundle bundle) throws NotActiveException {
        throw new NotActiveException("Can only read from desktop.");
    }
}
