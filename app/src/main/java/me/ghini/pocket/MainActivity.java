package me.ghini.pocket;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    static String lastGenusFound = "";
    private final List<Fragment> fragmentList = new ArrayList<>(4);
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    public static final String FORMS_CHOOSER_INTENT_TYPE = "vnd.android.cursor.dir/vnd.odk.form";
    private Uri imageUri;
    private int TAKE_PICTURE=1978;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setCurrentItem(1);

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
                //noinspection ResultOfMethodCallIgnored
                file.renameTo(newFile);
            }
            new PrintWriter(filename).close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "can't create log file", Toast.LENGTH_LONG).show();
        }
    }

    public void scanBarcode(View view) {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (scanResult != null) {
            EditText editText = findViewById(R.id.searchText);
            String contents = scanResult.getContents();
            editText.setText(contents);
            searchData(null);
        }
    }

    public void searchData(View view) {
        EditText locationText = findViewById(R.id.locationText);
        EditText editText = findViewById(R.id.searchText);

        String location = locationText.getText().toString();
        mViewPager.setCurrentItem(2);
        ResultsFragment resultFragment = (ResultsFragment) fragmentList.get(2);
        resultFragment.setLocation(location);
        MainActivity.lastGenusFound = resultFragment.refreshContent(editText.getText().toString());
        CollectFragment cf = (CollectFragment) fragmentList.get(3);
        cf.refreshContent(resultFragment.fullPlantCode, resultFragment.species,
                resultFragment.noOfPics, resultFragment.noOfPlants, resultFragment.editPending);
    }

    public void onWikipedia(View view) {
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

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

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
    }
    public void onMakeZeroPlants(View view) {
        TextView t = findViewById(R.id.tvCollectNumberOfPlants);
        t.setText("0");
    }

    public void onSaveToLog(View view) {
        mViewPager.setCurrentItem(2);
    }

    public void onTakePicture(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(getExternalFilesDir(null),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

}
