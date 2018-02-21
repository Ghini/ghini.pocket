package me.ghini.pocket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by mario on 2018-01-28.
 */

public class ResultsFragment extends android.support.v4.app.Fragment {

    private final SimpleDateFormat simpleDateFormat;
    String species = "";
    EditText searchText;
    TextView tvAccession;
    TextView tvFamily;
    TextView tvSpecies;
    TextView tvAcqDate;
    TextView tvSource;
    TextView tvLocation;
    TextView tvDismissDate;
    TextView tvNoOfPics;
    TextView tvOverride;

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
        tvDismissDate = rootView.findViewById(R.id.tvDateOfDeath);
        tvNoOfPics = rootView.findViewById(R.id.tvNumberOfPics);
        tvOverride = rootView.findViewById(R.id.tvOverride);
        return rootView;
    }

    public void updateFields(Object accession,
                             Object family,
                             Object species,
                             Object acqDate,
                             Object source,
                             Object location,
                             Object dismissDate,
                             Object noOfPics,
                             Object isUpdatePending) {
        try {
            tvAccession.setText((String)accession);
            tvFamily.setText((String)family);
            tvSpecies.setText((String)species);
            tvAcqDate.setText((String)acqDate);
            tvSource.setText((String)source);
            tvLocation.setText((String)location);
            tvDismissDate.setText((String)dismissDate);
            tvNoOfPics.setText((String)noOfPics);
            tvOverride.setText((String)isUpdatePending);
        }
        catch (NullPointerException ignore) {}
     }
}
