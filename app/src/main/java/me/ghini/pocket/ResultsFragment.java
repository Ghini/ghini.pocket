package me.ghini.pocket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

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

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by mario on 2018-01-28.
 */

public class ResultsFragment extends android.support.v4.app.Fragment {

    private final SimpleDateFormat simpleDateFormat;
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

    public ResultsFragment() {
        simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_results, container, false);
        tvAccession = rootView.findViewById(R.id.tvAccession);
        tvFamily = rootView.findViewById(R.id.tvFamily);
        tvSpecies = rootView.findViewById(R.id.tvSpecies);
        tvAcqDate = rootView.findViewById(R.id.tvAcqDate);
        tvSource = rootView.findViewById(R.id.tvSource);
        tvLocation = rootView.findViewById(R.id.tvLocation);
        tvDismissionDate = rootView.findViewById(R.id.tvDateOfDeath);
        tvNoOfPics = rootView.findViewById(R.id.tvNumberOfPics);
        return rootView;
    }

    public String refreshContent(String fromScan) {
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

        String genus_epithet = "";
        String accessionCode = searchedPlantCode;
        String plantCode = ".1";
        Pattern pattern = Pattern.compile("(.*)(\\.[1-9][0-9]?[0-9]?)");
        Matcher m = pattern.matcher(searchedPlantCode);
        if(m.find()) {
            accessionCode = m.group(1);
            plantCode = m.group(2);
        }

        String filename = new File(getActivity().getExternalFilesDir(null), "pocket.db").getAbsolutePath();
        if (accessionCode.equalsIgnoreCase("settings")) {
            fullPlantCode = filename;
        } else {
            try {
                SQLiteDatabase database = getActivity().openOrCreateDatabase(filename, MODE_PRIVATE, null);
                String query = "SELECT s.family, s.genus, s.epithet, a.code, p.code, a.source, p.location, a.start_date, p.end_date, p.n_of_pics " +
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
                resultSet.close();
            } catch (CursorIndexOutOfBoundsException e) {
                Toast.makeText(getActivity(), "nothing matches", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                family = e.getClass().getSimpleName();
                location = e.getLocalizedMessage().substring(0, 25).concat(" ...");
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
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
        return genus_epithet;
    }

    @SuppressLint("HardwareIds")
    private void logSearch() {
        PrintWriter out = null;
        try {
            String filename = new File(getActivity().getExternalFilesDir(null), "searches.txt").getAbsolutePath();
            out = new PrintWriter(new BufferedWriter(new FileWriter(filename, true)));
            Calendar calendar = Calendar.getInstance();
            String timeStamp = simpleDateFormat.format(calendar.getTime());
            TelephonyManager telephonyManager = (TelephonyManager)getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            String deviceId = "";
            try {
                if (telephonyManager != null) deviceId = telephonyManager.getDeviceId();
            } catch (SecurityException e) {
                Toast.makeText(getActivity(), "Please grant permission to get phone's IMEI.", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
            out.println(String.format("%s : %s : %s : %s", timeStamp, locationCode, searchedPlantCode, deviceId));
        } catch (IOException e) {
            Toast.makeText(getActivity(), "can't log search", Toast.LENGTH_SHORT).show();
        } finally {
            if(out != null){
                out.close();
            }
        }
    }

    public void setLocation(String location) {
        this.locationCode = location;
    }
}
