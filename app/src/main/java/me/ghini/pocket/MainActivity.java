package me.ghini.pocket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.spec.ECField;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.zxing.integration.android.IntentIntegrator.REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements CommunicationInterface {

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
    static final String LOCATION_CODE = "location";
    static final String PICTURE_NAMES = "file_name_list";
    static final String PLANT_ID = "plant_id";
    static final String GRAB_POSITION = "grab_position";

    private final List<Fragment> fragmentList = new ArrayList<>(4);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    public static final String FORMS_CHOOSER_INTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
    FragmentWithState collectFragment = null;
    LocationManager locationManager;
    private Uri imageUri;
    private static final int TAKE_PICTURE = 1978;
    private Bundle state;
    private int plantId = -1;

    public MainActivity() {
        // create the fragments
        fragmentList.add(new TaxonomyFragment());
        fragmentList.add(new SearchFragment());
        fragmentList.add(new ResultsFragment());
        Fragment fragment = new CollectFragment();
        fragmentList.add(fragment);
        collectFragment = (FragmentWithState) fragment;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager = null;
        }
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
        mViewPager = findViewById(R.id.container);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switchToPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(SEARCH_PAGE);
    }

    public void onSearchDoSearch(View view) {
        EditText locationText = findViewById(R.id.locationText);
        EditText searchText = findViewById(R.id.searchText);
        String searchPlantCode = searchText.getText().toString();
        String locationCode = locationText.getText().toString();

        state.putString("locationCode", locationCode);
        state.putString("searchedPlantCode", searchPlantCode);

        executeSearch(searchPlantCode);
        switchToPage(RESULT_PAGE);
    }

    public void executeSearch(String fromScan) {
        logSearch();
        String fullPlantCode = String.format(getString(R.string.not_found), fromScan);

        String family = "";
        String acqDate = "";
        String source = "";
        String location = "";
        String dismissDate = "";
        String noOfPics = "";
        String noOfPlants = "";
        String species = "";
        int editPending = 0;

        String genus_epithet = "";
        String accessionCode = fromScan;
        String plantCode = ".1";
        Pattern pattern = Pattern.compile("(.*)(\\.[1-9][0-9]?[0-9]?)");
        Matcher m = pattern.matcher(fromScan);
        if (m.find()) {
            accessionCode = m.group(1);
            plantCode = m.group(2);
        }

        String filename = new File(getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        if (accessionCode.equalsIgnoreCase("settings")) {
            fullPlantCode = filename;
        } else {
            try {
                SQLiteDatabase database = openOrCreateDatabase(filename, MODE_PRIVATE, null);
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
                fullPlantCode = resultSet.getString(3) + resultSet.getString(4);
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
                resultSet.close();
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(this, "nothing matches", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                family = e.getClass().getSimpleName();
                location = e.getLocalizedMessage().substring(0, 25).concat(" ...");
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        state.putString(PLANT_CODE, fullPlantCode);
        state.putString(FAMILY, family);
        state.putString(BINOMIAL, species);
        state.putString("acqDate", acqDate);
        state.putString("source", source);
        state.putString(LOCATION_CODE, location);
        state.putString("dismissDate", dismissDate);
        state.putString(NO_OF_PICS, noOfPics);
        state.putBoolean(OVERRIDE, editPending != 0);

        state.putString(NO_OF_PLANTS, noOfPlants);
        state.putString(GENUS, genus_epithet);

        state.putInt(PLANT_ID, plantId);

        switchToPage(RESULT_PAGE);
    }

    public void onResultsWikipedia(View view) {
        TextView tvSpecies = findViewById(R.id.tvSpecies);
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

    public void onCollectMakeZeroPlants(View view) {
        TextView t = (TextView)view;
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
            collectFragment.updateView();
        }
    }

    public void onBinomialEdit(View view) {
        TextView t = (TextView)view;
        String newValue = String.valueOf(t.getText());
        String previousValue = state.getString(BINOMIAL, "");
        if(!previousValue.equals(newValue)) {
            collectFragment.state.putString(BINOMIAL, newValue);
            collectFragment.state.putBoolean(OVERRIDE, true);
            collectFragment.updateView();
        }
    }

    public void onGrabPosition(View view) {
        CheckBox t = (CheckBox)view;
        collectFragment.state.putBoolean(GRAB_POSITION, t.isChecked());
    }

    public void onCollectTakePicture(View view) {
        if (state.get(PICTURE_NAMES) == null) {
            Toast.makeText(this, R.string.picture_of_nothing, Toast.LENGTH_LONG).show();
            return;
        }
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
                    EditText editText = findViewById(R.id.searchText);
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
        // Fragment.state gets accepted in Activity.state,
        // then we write in the local database that we have local edits,
        // then we save the edits in the log file, so they can be imported,
        // finally we move back to the ResultsFragment.
        state.putAll(collectFragment.state);
        String filename = new File(getExternalFilesDir(null), "pocket.db").
                getAbsolutePath();
        try {
            SQLiteDatabase database = openOrCreateDatabase(filename, MODE_PRIVATE, null);
            String query = "UPDATE plant SET edit_pending=1 WHERE _id=?";
            Cursor resultSet = database.rawQuery(query, new String[]{String.valueOf(plantId)});
            resultSet.moveToLast();
            resultSet.close();
        }
        catch (Exception ignore) {
        }

        StringBuilder sb = new StringBuilder();
        Location location = null;
        if(state.getBoolean(GRAB_POSITION, false)) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }
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

        writeLogLine(String.format("%s : %s : %s : %s",
                state.getString(PLANT_CODE), state.getString(BINOMIAL),
                state.getString(NO_OF_PLANTS), sb.toString()));

        switchToPage(RESULT_PAGE);
    }

    @SuppressLint("HardwareIds")
    private void logSearch() {
        TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String deviceId = "";
        try {
            if (telephonyManager != null) deviceId = telephonyManager.getDeviceId();
        } catch (SecurityException e) {
            Toast.makeText(this, "Please grant permission to get phone's IMEI.", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
        }
        String locationCode = state.getString("locationCode");
        String fullPlantCode = state.getString(PLANT_CODE);
        writeLogLine(String.format("%s : %s : %s", locationCode, fullPlantCode, deviceId));
    }

    private void writeLogLine(String line) {
        Calendar calendar = Calendar.getInstance();
        String timeStamp = simpleDateFormat.format(calendar.getTime());
        PrintWriter out = null;
        try {
            String filename = new File(getExternalFilesDir(null), "searches.txt").getAbsolutePath();
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            out.println(String.format("%s : %s", timeStamp, line));
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

