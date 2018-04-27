package me.ghini.pocket;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static me.ghini.pocket.MainActivity.BINOMIAL;
import static me.ghini.pocket.MainActivity.DB_LOCATION_CODE;
import static me.ghini.pocket.MainActivity.FAMILY;
import static me.ghini.pocket.MainActivity.LOCATION_CODE;
import static me.ghini.pocket.MainActivity.NO_OF_PICS;
import static me.ghini.pocket.MainActivity.OVERRIDE;
import static me.ghini.pocket.MainActivity.PLANT_CODE;

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
    CheckBox cbOverride;

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
        cbOverride = rootView.findViewById(R.id.cbOverride);
        return rootView;
    }

    @Override
    public void setArguments(Bundle b) {
        try {
            tvAccession.setText(b.getString(PLANT_CODE, ""));
            tvFamily.setText(b.getString(FAMILY, ""));
            tvSpecies.setText(b.getString(BINOMIAL, ""));
            tvAcqDate.setText(b.getString("acqDate", ""));
            tvSource.setText(b.getString("source", ""));
            tvLocation.setText(b.getString(DB_LOCATION_CODE, ""));
            tvDismissDate.setText(b.getString("dismissDate", ""));
            tvNoOfPics.setText(b.getString(NO_OF_PICS, "0"));
            cbOverride.setChecked(b.getBoolean(OVERRIDE, true));
        }
        catch (NullPointerException ignore) {}
     }
}
