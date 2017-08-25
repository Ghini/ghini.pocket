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

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class DisplayResultsActivity extends AppCompatActivity {

    String fullPlantCode = "";
    String family = "";
    String species = "";
    String acqDate = "";
    String source = "";
    String location = "";
    String dismDate = "";
    String noOfPics = "";

    TextView tvAccession;
    TextView tvFamily;
    TextView tvSpecies;
    TextView tvAcqDate;
    TextView tvSource ;
    TextView tvLocation;
    TextView tvDismDate;
    TextView tvNoOfPics;

    public void onWikipedia(View view) {
        try {
            if(species.equals(""))
                throw new AssertionError("empty lookup");
            String wikiLink = String.format("https://en.wikipedia.org/wiki/%s", species);
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wikiLink));
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.missing_web_browser, Toast.LENGTH_LONG).show();
        } catch(AssertionError e){
            Toast.makeText(this, R.string.empty_lookup, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        String plantCode = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);

        fullPlantCode = String.format(getString(R.string.not_found), plantCode);

        tvAccession = (TextView) findViewById(R.id.tvAccession);
        tvFamily = (TextView) findViewById(R.id.tvFamily);
        tvSpecies = (TextView) findViewById(R.id.tvSpecies);
        tvAcqDate = (TextView) findViewById(R.id.tvAcqDate);
        tvSource = (TextView) findViewById(R.id.tvSource);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDismDate = (TextView) findViewById(R.id.tvDateOfDeath);
        tvNoOfPics = (TextView) findViewById(R.id.tvNumberOfPics);

        String filename = new File(getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        if (plantCode.equalsIgnoreCase("settings")) {
            fullPlantCode = filename;
        } else {
            try {
                SQLiteDatabase database = openOrCreateDatabase(filename, MODE_PRIVATE, null);
                Cursor resultSet = database.rawQuery(
                        "SELECT s.family, s.genus, s.epithet, a.code, p.code, a.source, p.location, a.start_date, p.end_date, p.n_of_pics " +
                                "FROM species s, accession a, plant p " +
                                "WHERE p.accession_id = a._id " +
                                "AND a.species_id = s._id " +
                                "AND a.code = ?", new String[]{plantCode});
                resultSet.moveToFirst();
                family = resultSet.getString(0);
                String genus_epithet = resultSet.getString(1);
                String sp_epithet = resultSet.getString(2);
                if (sp_epithet == null)
                    sp_epithet = "";
                if (genus_epithet.startsWith("Zzz"))
                    genus_epithet = "";
                if (!genus_epithet.equals("") && sp_epithet.equals("sp") && sp_epithet.equals("")) {
                    species = genus_epithet + " " + sp_epithet;
                } else {
                    species = genus_epithet;
                }
                fullPlantCode = resultSet.getString(3) + resultSet.getString(4);
                source = resultSet.getString(5);
                location = resultSet.getString(6);
                acqDate = resultSet.getString(7);
                if(acqDate != null) acqDate = acqDate.substring(0, 16);
                dismDate = resultSet.getString(8);
                if(dismDate != null) dismDate = dismDate.substring(0, 16);
                noOfPics = String.valueOf(resultSet.getInt(9));
                resultSet.close();
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(this, "nothing matches", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                family = e.toString();
            }
        }

        // Capture the layout's TextView and set the string as its text
        tvAccession.setText(fullPlantCode);
        tvFamily.setText(family);
        tvSpecies.setText(species);
        tvLocation.setText(location);
        tvSource.setText(source);
        tvAcqDate.setText(acqDate);
        tvDismDate.setText(dismDate);
        tvNoOfPics.setText(noOfPics);
    }

}
