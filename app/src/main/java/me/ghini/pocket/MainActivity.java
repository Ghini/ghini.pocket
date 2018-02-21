package me.ghini.pocket;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.view.View;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements CommunicationInterface {

    public final static int TAXONOMY_PAGE = 0;
    public final static int SEARCH_PAGE = 1;
    public final static int RESULT_PAGE = 2;
    public final static int COLLECT_PAGE = 3;

    private final List<Fragment> fragmentList = new ArrayList<>(4);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    public static final String FORMS_CHOOSER_INTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
    private Uri imageUri;
    private int TAKE_PICTURE=1978;
    private Bundle myb;

    public MainActivity() {
        // create the fragments
        fragmentList.add(new TaxonomyFragment());
        fragmentList.add(new SearchFragment());
        fragmentList.add(new ResultsFragment());
        fragmentList.add(new CollectFragment());
    }
    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onSaveInstanceState(Bundle state) {
        state.putAll(myb);
        super.onSaveInstanceState(state);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myb = new Bundle();
        if(savedInstanceState != null) {
            myb.putAll(savedInstanceState);
        } else {
            myb.putString("searchedPlantCode", "");
            myb.putString("fullPlantCode", "");
            myb.putString("binomial", "");
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
            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return getString(R.string.title_1);
                    case 1:
                        return getString(R.string.title_2);
                    case 2:
                        return getString(R.string.title_3);
                    case 3:
                        return getString(R.string.title_4);
                }
                return null;
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

    public void onSharedScanBarcode(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onSearchDoSearch(View view) {
        EditText locationText = findViewById(R.id.locationText);
        EditText searchText = findViewById(R.id.searchText);
        String searchPlantCode = searchText.getText().toString();
        String locationCode = locationText.getText().toString();

        myb.putString("locationCode", locationCode);
        myb.putString("searchedPlantCode", searchPlantCode);

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
        String editPending = "";

        String genus_epithet = "";
        String accessionCode = fromScan;
        String plantCode = ".1";
        Pattern pattern = Pattern.compile("(.*)(\\.[1-9][0-9]?[0-9]?)");
        Matcher m = pattern.matcher(fromScan);
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
                String query = "SELECT s.family, s.genus, s.epithet, "+
                        "a.code, p.code, a.source, p.location, "+
                        "a.start_date, p.end_date, p.n_of_pics, p.quantity, p.edit_pending " +
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
                } catch (Exception ignored) {}
                dismissDate = resultSet.getString(8);
                try {
                    dismissDate = dismissDate.substring(0, 16);
                } catch (Exception ignored) {}
                noOfPics = String.valueOf(resultSet.getInt(9));
                noOfPlants = String.valueOf(resultSet.getInt(10));
                editPending = String.valueOf(resultSet.getInt(11));
                resultSet.close();
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(this, "nothing matches", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                family = e.getClass().getSimpleName();
                location = e.getLocalizedMessage().substring(0, 25).concat(" ...");
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
            }
        }

        myb.putString("fullPlantCode", fullPlantCode);
        myb.putString("family", family);
        myb.putString("species", species);
        myb.putString("location", location);
        myb.putString("source", source);
        myb.putString("acqDate", acqDate);
        myb.putString("dismissDate", dismissDate);
        myb.putString("noOfPics", noOfPics);
        myb.putString("noOfPlants", noOfPlants);
        myb.putString("genus", genus_epithet);
        myb.putString("editPending", editPending);

        switchToPage(RESULT_PAGE);
    }

    public void onResultsWikipedia(View view) {
        TextView tvSpecies = findViewById(R.id.tvSpecies);
        String species = tvSpecies.getText().toString();
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

    // coming from CollectFragment
    public void onCollectSaveToLog(View view) {
        mViewPager.setCurrentItem(RESULT_PAGE);
    }

    public void onCollectTakePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(getExternalFilesDir(null),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public void onCollectMakeZeroPlants(View view) {
        TextView t = findViewById(R.id.tvCollectNumberOfPlants);
        t.setText("0");
    }

    // general callbacks
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            EditText editText = findViewById(R.id.searchText);
            String contents = scanResult.getContents();
            editText.setText(contents);
            onSearchDoSearch(null);
        }
    }

    @Override
    public void switchToPage(int page) {
        Fragment f = fragmentList.get(page);
        switch (page) {
            case TAXONOMY_PAGE:
                TaxonomyFragment tf = (TaxonomyFragment) f;
                tf.refreshContent(myb.get("genus"));
                break;
            case SEARCH_PAGE:
                break;
            case RESULT_PAGE:
                ResultsFragment rf = (ResultsFragment) f;
                rf.updateFields(myb.get("fullPlantCode"),
                        myb.get("family"),
                        myb.get("species"),
                        myb.get("acqDate,"),
                        myb.get("source"),
                        myb.get("location"),
                        myb.get("dismissDate"),
                        myb.get("noOfPics"),
                        myb.get("editPending"));
                break;
            case COLLECT_PAGE:
                CollectFragment cf = (CollectFragment) f;
                cf.refreshContent(myb.get("fullPlantCode"),
                        myb.get("species"),
                        myb.get("noOfPics"),
                        myb.get("noOfPlants"),
                        myb.get("editPending"));
                break;
        }
        if (mViewPager.getCurrentItem() != page ) {
            mViewPager.setCurrentItem(page);
        }
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
                if (telephonyManager != null) deviceId = telephonyManager.getDeviceId();
            } catch (SecurityException e) {
                Toast.makeText(this, "Please grant permission to get phone's IMEI.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            String locationCode = myb.getString("locationCode");
            String fullPlantCode = myb.getString("fullPlantCode");
            out.println(String.format("%s : %s : %s : %s", timeStamp, locationCode, fullPlantCode, deviceId));
        } catch (IOException e) {
            Toast.makeText(this, "can't log search", Toast.LENGTH_SHORT).show();
        } finally {
            if(out != null){
                out.close();
            }
        }
    }

}
