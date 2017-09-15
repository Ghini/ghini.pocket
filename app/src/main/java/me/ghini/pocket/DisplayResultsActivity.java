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

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisplayResultsActivity extends AppCompatActivity {

    public static final String FORMS_CHOOSER_INTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";

    String species = "";
    TextView tvAccession;
    TextView tvFamily;
    TextView tvSpecies;
    TextView tvAcqDate;
    TextView tvSource;
    TextView tvLocation;
    TextView tvDismissionDate;
    TextView tvNoOfPics;
    private String searchedPlantCode;
    private String locationCode;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);

    public void onNextScan(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            String scanResultContents = scanResult.getContents();
            refreshContent(scanResultContents);
        }
    }

    public void onCollect(View view) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setType(FORMS_CHOOSER_INTENT_TYPE);
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("last plant search", searchedPlantCode);
            clipboard.setPrimaryClip(clip);
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.missing_web_browser, Toast.LENGTH_LONG).show();
        }
    }

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

    public void refreshContent(String fromScan) {
        searchedPlantCode = fromScan;
        logSearch();
        String fullPlantCode = String.format(getString(R.string.not_found), searchedPlantCode);

        String family = "";
        String acqDate = "";
        String source = "";
        String location = "";
        String dismissDate = "";
        String noOfPics = "";
        species = "";

        String accessionCode = searchedPlantCode;
        String plantCode = ".1";
        Pattern pattern = Pattern.compile("(.*)(\\.[1-9][0-9]?[0-9]?)");
        Matcher m = pattern.matcher(searchedPlantCode);
        if(m.find()) {
            accessionCode = m.group(1);
            plantCode = m.group(2);
        }

        String filename = new File(getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        if (accessionCode.equalsIgnoreCase("settings")) {
            fullPlantCode = filename;
        } else {
            try {
                SQLiteDatabase database = openOrCreateDatabase(filename, MODE_PRIVATE, null);
                Cursor resultSet = database.rawQuery(
                        "SELECT s.family, s.genus, s.epithet, a.code, p.code, a.source, p.location, a.start_date, p.end_date, p.n_of_pics " +
                                "FROM species s, accession a, plant p " +
                                "WHERE p.accession_id = a._id " +
                                "AND a.species_id = s._id " +
                                "AND a.code = ? " +
                                "AND p.code = ? ", new String[]{accessionCode, plantCode});
                resultSet.moveToFirst();
                family = resultSet.getString(0);
                String genus_epithet = resultSet.getString(1);
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
                fullPlantCode = resultSet.getString(3) + resultSet.getString(4);
                source = resultSet.getString(5);
                location = resultSet.getString(6);
                acqDate = resultSet.getString(7);
                if(acqDate != null) acqDate = acqDate.substring(0, 16);
                dismissDate = resultSet.getString(8);
                if(dismissDate != null) dismissDate = dismissDate.substring(0, 16);
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
        tvDismissionDate.setText(dismissDate);
        tvNoOfPics.setText(noOfPics);
    }

    @SuppressLint("HardwareIds")
    private void logSearch() {
        PrintWriter out = null;
        try {
            String filename = new File(getExternalFilesDir(null), "searches.txt").getAbsolutePath();
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            Calendar calendar = Calendar.getInstance();
            String timeStamp = simpleDateFormat.format(calendar.getTime());
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = "";
            try {
                deviceId = telephonyManager.getDeviceId();
            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            out.println(String.format("%s : %s : %s : %s", timeStamp, locationCode, searchedPlantCode, deviceId));
        } catch (IOException e) {
            Toast.makeText(this, "can't log search", Toast.LENGTH_SHORT).show();
        } finally {
            if(out != null){
                out.close();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_result);

        tvAccession = (TextView) findViewById(R.id.tvAccession);
        tvFamily = (TextView) findViewById(R.id.tvFamily);
        tvSpecies = (TextView) findViewById(R.id.tvSpecies);
        tvAcqDate = (TextView) findViewById(R.id.tvAcqDate);
        tvSource = (TextView) findViewById(R.id.tvSource);
        tvLocation = (TextView) findViewById(R.id.tvLocation);
        tvDismissionDate = (TextView) findViewById(R.id.tvDateOfDeath);
        tvNoOfPics = (TextView) findViewById(R.id.tvNumberOfPics);

        // Get the Intent that started this activity and extract the string
        Intent intent = getIntent();
        searchedPlantCode = intent.getStringExtra(MainActivity.PLANT_CODE);
        locationCode = intent.getStringExtra(MainActivity.LOCATION_CODE);
        refreshContent(searchedPlantCode);
    }

}
