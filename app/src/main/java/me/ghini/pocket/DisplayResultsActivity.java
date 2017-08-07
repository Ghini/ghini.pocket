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

        String fullPlantCode = getString(R.string.NOTFOUND) + " " + plantCode;
        String family = "";
        String species = "";
        String acqDate = "";
        String source = "";
        String filename = new File(getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        try {
            SQLiteDatabase database = openOrCreateDatabase(filename, MODE_PRIVATE, null);
            Cursor resultSet = database.rawQuery("select s.family, s.genus, s.epithet, a.code, p.code " +
                    "from species s, accession a, plant p " +
                    "where p.accession_id = a._id and a.species_id = s._id and a.code = '" + plantCode + "'", null);
            resultSet.moveToFirst();
            family = resultSet.getString(0);
            species = resultSet.getString(1) + " " + resultSet.getString(2);
            fullPlantCode = resultSet.getString(3) + resultSet.getString(4);
            resultSet.close();
        } catch (Exception e) {

        }

        // Capture the layout's TextView and set the string as its text
        TextView tvAccession = (TextView) findViewById(R.id.tvAccession);
        tvAccession.setText(fullPlantCode);
        TextView tvFamily = (TextView) findViewById(R.id.tvFamily);
        tvFamily.setText(family);
        TextView tvSpecies = (TextView) findViewById(R.id.tvSpecies);
        tvSpecies.setText(species);
        TextView tvAcqDate = (TextView) findViewById(R.id.tvAcqDate);
        tvAcqDate.setText(acqDate);
        TextView tvSource = (TextView) findViewById(R.id.tvSource);
        tvSource.setText(source);
    }

}
