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

import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public static final String PLANT_CODE = "me.ghini.pocket.code.plant";
    public static final String LOCATION_CODE = "me.ghini.pocket.code.location";
    EditText locationText;
    EditText editText;
    static String location = null;
    private SimpleDateFormat simpleDateFormat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        setContentView(R.layout.activity_main);
        locationText = (EditText) findViewById(R.id.locationText);
        editText = (EditText) findViewById(R.id.editText);
        if (location != null) {
            locationText.setText(location);
        }
    }

    public void onZeroSearchLog(View view) {
        try {
            String fileFormat = new File(getExternalFilesDir(null), "searches%s%s.txt").getAbsolutePath();
            String filename = String.format(fileFormat, "", "");
            File file = new File(filename);
            if (file.length() > 0) {
                Calendar calendar = Calendar.getInstance();
                String timeStamp = simpleDateFormat.format(calendar.getTime());
                String newNameForOldFile = String.format(fileFormat, "_", timeStamp);
                File newFile = new File(newNameForOldFile);
                boolean ok = file.renameTo(newFile);
            }
            new PrintWriter(filename).close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "can't create log file", Toast.LENGTH_LONG).show();
        }
    }

    /** Called when the user taps the Send button */
    public void searchData(View view) {
        location = locationText.getText().toString();
        String message = editText.getText().toString();
        Intent intent = new Intent(this, DisplayResultsActivity.class);
        intent.putExtra(PLANT_CODE, message);
        intent.putExtra(LOCATION_CODE, location);
        startActivity(intent);
    }

    /** Called when the user taps the Scan button */
    public void scanBarcode(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        AlertDialog scan = integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            EditText editText = (EditText) findViewById(R.id.editText);
            String contents = scanResult.getContents();
            editText.setText(contents);
        }
    }
}
