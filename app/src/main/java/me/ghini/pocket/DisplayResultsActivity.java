/*
  This file is part of ghini.pocket.
  
  ghini.pocket is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  ghini.pocket is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with ghini.pocket.  If not, see <http://www.gnu.org/licenses/>.
 */

package me.ghini.pocket;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.File;

public class DisplayResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String plantCode = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        String fullPlantCode = String.format(getString(R.string.NOTFOUND), plantCode);
        String family = "";
        String species = "";
        String acqDate = "";
        String source = "";
        String location = "";

        TextView tvAccession = (TextView) findViewById(R.id.tvAccession);
        TextView tvFamily = (TextView) findViewById(R.id.tvFamily);
        TextView tvSpecies = (TextView) findViewById(R.id.tvSpecies);
        TextView tvAcqDate = (TextView) findViewById(R.id.tvAcqDate);
        TextView tvSource = (TextView) findViewById(R.id.tvSource);
        TextView tvLocation = (TextView) findViewById(R.id.tvLocation);

        String filename = new File(getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        if (plantCode.equalsIgnoreCase("settings")) {
            fullPlantCode = filename;
        } else {
            try {
                SQLiteDatabase database = openOrCreateDatabase(filename, MODE_PRIVATE, null);
                Cursor resultSet = database.rawQuery(
                        "SELECT s.family, s.genus, s.epithet, a.code, p.code, a.source, p.location " +
                        "FROM species s, accession a, plant p " +
                        "WHERE p.accession_id = a._id " +
                        "AND a.species_id = s._id " +
                        "AND a.code = ?", new String[] {plantCode});
                resultSet.moveToFirst();
                family = resultSet.getString(0);
                species = resultSet.getString(1) + " " + resultSet.getString(2);
                fullPlantCode = resultSet.getString(3) + resultSet.getString(4);
                source = resultSet.getString(5);
                location = resultSet.getString(6);
                resultSet.close();
            } catch (Exception e) {
                family = e.toString();
            }
        }

        // Capture the layout's TextView and set the string as its text
        tvAccession.setText(fullPlantCode);
        tvFamily.setText(family);
        tvSpecies.setText(species);
        tvAcqDate.setText(acqDate);
        tvSource.setText(source);
        tvLocation.setText(location);
    }

}
