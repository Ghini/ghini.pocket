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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

/*
 * Copyright © 2018 Mario Frasca. <mario@anche.no>
 *
 */

@SuppressWarnings("RedundantCast")
public class MainActivity extends AppCompatActivity implements CommunicationInterface {
    public static Resources resources;

    public final static int TAXONOMY_PAGE = 0;
    public final static int SEARCH_PAGE = 1;
    public final static int RESULT_PAGE = 2;
    public final static int COLLECT_PAGE = 3;

    static final String BINOMIAL = "species";
    static final String NO_OF_PICS = "noOfPics";
    static final String FAMILY = "family";
    static final String OVERRIDE = "editPending";
    static final String NO_OF_PLANTS = "noOfPlants";
    static final String GENUS = "genus";
    static final String PLANT_CODE = "fullPlantCode";
    static final String LOCATION_CODE = "locationCode";
    static final String DB_LOCATION_CODE = "locationCodeDB";
    static final String PICTURE_NAMES = "file_name_list";
    static final String PLANT_ID = "plant_id";
    static final String GRAB_POSITION = "grab_position";
    static final String SOURCE = "source";
    static final String ACQUISITION = "acqDate";
    static final String DISMISSAL = "dismissDate";

    private final List<Fragment> fragmentList = new ArrayList<>(4);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    public static final String FORMS_CHOOSER_INTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
    FragmentWithState collectFragment;
    LocationManager locationManager;
    private Uri imageUri;
    private static final int TAKE_PICTURE = 1978;
    private Bundle state;
    private int plantId = -1;
    Activity that;
    private DesktopSource desktopSource;
    private PocketSource pocketSource;
    private int lastPosition = 0;
    public static String deviceId;
    public static String serverIP;
    public static String serverPort;
    private Properties props;

    public MainActivity() {
        // create the fragments
        fragmentList.add(new TaxonomyFragment());
        fragmentList.add(new SearchFragment());
        fragmentList.add(new ResultsFragment());
        Fragment fragment = new CollectFragment();
        fragmentList.add(fragment);
        collectFragment = (FragmentWithState) fragment;
        pocketSource = new PocketSource(this);
        desktopSource = new DesktopSource(this);
        that = this;
    }

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putAll(this.state);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onPause() {
        super.onPause();
        storePersistentProperties();
    }

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resources = getResources();
        state = new Bundle();
        if (savedInstanceState != null) {
            state.putAll(savedInstanceState);
        } else {
            state.putString("searchedPlantCode", "");
            state.putString(PLANT_CODE, "");
            state.putString(BINOMIAL, "");
            state.putInt(PLANT_ID, -1);
            state.putBoolean(GRAB_POSITION, false);
        }
        initializePersistentProperties();
        readPersistentProperties();

        // we have permission to access location?
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        } else {
            locationManager = null;
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // handle the fragments over to the adapter
        /*
          The {@link android.support.v4.view.PagerAdapter} that provides
          a fragment for each of the sections. We use a
          {@link FragmentPagerAdapter} derivative, which keeps every
          fragment in memory. If this becomes too memory intensive, switch to
          {@link android.support.v4.app.FragmentStatePagerAdapter}.
        */
        FragmentPagerAdapter mSectionsPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragmentList.get(position);
            }

            @Override
            public int getCount() {
                return 4;
            }
        };

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                if(position == RESULT_PAGE && lastPosition == SEARCH_PAGE) {
                    EditText searchText = (EditText) findViewById(R.id.searchText);
                    String searchPlantCode = searchText.getText().toString();
                    if (searchPlantCode.length() == 0) {
                        Toast.makeText(that, R.string.empty_lookup, Toast.LENGTH_SHORT).show();
                        switchToPage(SEARCH_PAGE);
                    } else {
                        executeSearch(searchPlantCode);
                        switchToPage(RESULT_PAGE);
                    }
                } else {
                    switchToPage(position);
                }
                lastPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(SEARCH_PAGE);
        lastPosition = SEARCH_PAGE;
    }

    private void readPersistentProperties() {
        props = new Properties();
        String filename = new File(getExternalFilesDir(null), "persistent.properties").getAbsolutePath();
        FileInputStream input = null;
        try {
            input = new FileInputStream(filename);
            props.load(input);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            safeClose(input);
        }
        deviceId = props.getProperty("phone-identifier");
        serverIP = props.getProperty("server-ip", "192.168.43.226");
        serverPort = props.getProperty("server-port", "44464");
    }

    private void initializePersistentProperties()
    {
        Properties props = new Properties();
        String filename = new File(getExternalFilesDir(null), "persistent.properties").getAbsolutePath();
        File file = new File(filename);
        long fl = file.length();
        if (fl == 0) {
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            try {
                if (telephonyManager != null) deviceId = telephonyManager.getDeviceId();
            } catch (SecurityException e) {
                deviceId = generateRandomString(12);
            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            serverIP = "192.168.43.226";
            serverPort = "44464";
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filename);
                props.setProperty("phone-identifier", deviceId);
                props.setProperty("server-ip", serverIP);
                props.setProperty("server-port", serverPort);
                props.store(out, null);
            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            } finally {
                safeClose(out);
            }
        }
    }

    private void storePersistentProperties()
    {
        Properties props = new Properties();
        String filename = new File(getExternalFilesDir(null), "persistent.properties").getAbsolutePath();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(filename);
            props.setProperty("phone-identifier", deviceId);
            props.setProperty("server-ip", serverIP);
            props.setProperty("server-port", serverPort);
            props.store(out, null);
        } catch (IOException e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            safeClose(out);
        }
    }

    private void safeClose(InputStream stream) {
        if(stream != null){
            try {
                stream.close();
            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void safeClose(OutputStream stream) {
        if(stream != null){
            try {
                stream.close();
            } catch (IOException e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String generateRandomString(int length)
    {
        Toast.makeText(this, R.string.create_persistent, Toast.LENGTH_LONG).show();
        Random rng = new Random();
        //noinspection SpellCheckingInspection
        String characters = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char[] text = new char[length];
        for (int i = 0; i < length; i++)
        {
            text[i] = characters.charAt(rng.nextInt(characters.length()));
        }
        return new String(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // return true so that the menu pop up is opened
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.action_desktop: {
                startDesktopClientActivity();
            } break;
            case R.id.action_rtfd:
            case R.id.action_news:
            case R.id.action_github: {
                String url = "http://ghini.readthedocs.io/";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            } break;
            case R.id.action_about: {
                startAboutActivity();
            } break;
        }
        return true;
    }

    private void startAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    private void startDesktopClientActivity() {
        Intent intent = new Intent(this, DesktopClientActivity.class);
        startActivity(intent);
    }

    public void onSearchDoSearch(View view) {
        switchToPage(RESULT_PAGE);
    }

    public void executeSearch(String fromScan) {
        EditText locationText = (EditText) findViewById(R.id.locationText);
        String locationCode = locationText.getText().toString();
        state.putString(LOCATION_CODE, locationCode);
        try {
            state.putAll(desktopSource.getAt(fromScan));
            state.putAll(pocketSource.getAt(state.getString(PLANT_CODE)));
        } catch (Exception e) {
            state.putString(FAMILY, e.getClass().getSimpleName());
            state.putString(LOCATION_CODE, e.getLocalizedMessage().substring(0, 25).concat(" ..."));
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        }
        // log late, because searching might fill-in the plant code.
        logSearch();

        switchToPage(RESULT_PAGE);
    }

    public void onResultsWikipedia(View view) {
        TextView tvSpecies = (TextView) findViewById(R.id.tvSpecies);
        String species = tvSpecies.getText().toString();
        try {
            if (species.equals(""))
                throw new AssertionError("empty lookup");
            String wikiLink = String.format("https://en.wikipedia.org/wiki/%s", species);
            Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(wikiLink));
            startActivity(myIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.missing_web_browser, Toast.LENGTH_LONG).show();
        } catch (AssertionError e) {
            Toast.makeText(this, R.string.empty_lookup, Toast.LENGTH_SHORT).show();
        }
    }

    public void onResultsTaxonomy(View view) {
        switchToPage(TAXONOMY_PAGE);
    }

    public void onCollectMakeZeroPlants(View view) {
        String previousValue = state.getString(NO_OF_PLANTS, "0");
        if(!previousValue.equals("0")) {
            collectFragment.state.putString(NO_OF_PLANTS, "0");
            collectFragment.state.putBoolean(OVERRIDE, true);
            collectFragment.updateView();
        }
    }

    public void onNumberEdit(View view) {
        TextView t = (TextView)view;
        String newValue = String.valueOf(t.getText());
        String previousValue = state.getString(NO_OF_PLANTS, "0");
        if(!previousValue.equals(newValue)) {
            collectFragment.state.putString(NO_OF_PLANTS, newValue);
            collectFragment.state.putBoolean(OVERRIDE, true);
        }
    }

    public void onBinomialEdit(View view) {
        TextView t = (TextView)view;
        String newValue = String.valueOf(t.getText());
        String previousValue = state.getString(BINOMIAL, "");
        if(!previousValue.equals(newValue)) {
            collectFragment.state.putString(BINOMIAL, newValue);
            collectFragment.state.putBoolean(OVERRIDE, true);
        }
    }

    public void onGrabPosition(View view) {
        CheckBox t = (CheckBox)view;
        if(locationManager == null) {
            t.setChecked(false);
            Toast.makeText(this, R.string.permit_gps, Toast.LENGTH_SHORT).show();
        } else {
            collectFragment.state.putBoolean(GRAB_POSITION, t.isChecked());
        }
    }

    public void onCollectTakePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.unavailable_camera, Toast.LENGTH_LONG).show();
            return;
        }
        try {
            File photo = createImageFile();
            imageUri = Uri.fromFile(photo);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, TAKE_PICTURE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onSharedScanBarcode(View view) {
        // invokes startActivityForResult (REQUEST_CODE)
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    // general callbacks
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CODE: {
                IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
                if (scanResult != null) {
                    EditText editText = (EditText) findViewById(R.id.searchText);
                    String contents = scanResult.getContents();
                    editText.setText(contents);
                    onSearchDoSearch(null);
                }
            }
            break;
            case TAKE_PICTURE: {
                ArrayList<String> nameList = collectFragment.state.getStringArrayList(PICTURE_NAMES);
                assert nameList != null;
                nameList.add(String.valueOf(imageUri));
                collectFragment.state.putStringArrayList(PICTURE_NAMES, nameList);
                collectFragment.state.putBoolean(OVERRIDE, true);
                collectFragment.updateView();
            }
            break;
        }
    }

    @Override
    public void switchToPage(int page) {
        fragmentList.get(page).setArguments(state);
        if (mViewPager.getCurrentItem() != page ) {
            mViewPager.setCurrentItem(page);
        }
    }

    public void onSearchZeroLog(View view) {
        try {
            String fileFormat = new File(getExternalFilesDir(null), "searches%s%s.txt").getAbsolutePath();
            String filename = String.format(fileFormat, "", "");
            File file = new File(filename);
            if (file.length() > 0) {
                Calendar calendar = Calendar.getInstance();
                String timeStamp = simpleDateFormat.format(calendar.getTime());
                String newNameForOldFile = String.format(fileFormat, "_", timeStamp);
                File newFile = new File(newNameForOldFile);
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(newFile);
            }
            new PrintWriter(filename).close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "can't create log file", Toast.LENGTH_LONG).show();
        }
    }

    // coming from CollectFragment
    @SuppressLint("MissingPermission")
    public void onCollectSaveToLog(View view) {
        ArrayList<String> listOfNames = collectFragment.state.
                getStringArrayList(PICTURE_NAMES);
        if (listOfNames == null) {
            return;
        }

        Location location = null;
        if(state.getBoolean(GRAB_POSITION, true) && locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }

        // Fragment.state gets accepted in Activity.state,
        // then we write in the local database that we have local edits,
        // then we write the local edits to the local database,
        // then we save the edits in the log file, so they can be imported,
        // finally we move back to the ResultsFragment.

        pocketSource.putAt(plantId, collectFragment.getState());
        state.putAll(collectFragment.getState());

        StringBuilder sb = new StringBuilder();

        if(location != null) {
            sb.append("(");
            sb.append(location.getLatitude());
            sb.append(";");
            sb.append(location.getLongitude());
            sb.append(")");
        } else {
            sb.append("(@;@)");
        }
        for (String s : listOfNames) {
            sb.append(" : ");
            sb.append(s);
        }

        writeLogLine("PENDING_EDIT", String.format("%s : %s : %s : %s",
                state.getString(PLANT_CODE), state.getString(BINOMIAL),
                state.getString(NO_OF_PLANTS), sb.toString()));

        switchToPage(RESULT_PAGE);
    }

    private void logSearch() {
        String locationCode = state.getString(LOCATION_CODE);
        String fullPlantCode = state.getString(PLANT_CODE);
        writeLogLine("INVENTORY", String.format("%s : %s : %s", locationCode, fullPlantCode, deviceId));
    }

    private void writeLogLine(String tag, String line) {
        Calendar calendar = Calendar.getInstance();
        String timeStamp = simpleDateFormat.format(calendar.getTime());
        PrintWriter out = null;
        try {
            String filename = new File(getExternalFilesDir(null), "searches.txt").getAbsolutePath();
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            out.println(String.format("%s :%s: %s", timeStamp, tag, line));
        } catch (IOException e) {
            Toast.makeText(this, "can't write to log", Toast.LENGTH_SHORT).show();
        } finally {
            if(out != null){
                out.close();
            }
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = simpleDateFormat.format(new Date());
        String imageFileName = "GPP_" + timeStamp;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
    }

}

