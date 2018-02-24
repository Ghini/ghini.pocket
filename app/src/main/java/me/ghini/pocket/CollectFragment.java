package me.ghini.pocket;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import static me.ghini.pocket.MainActivity.BINOMIAL;
import static me.ghini.pocket.MainActivity.GRAB_POSITION;
import static me.ghini.pocket.MainActivity.NO_OF_PICS;
import static me.ghini.pocket.MainActivity.NO_OF_PLANTS;
import static me.ghini.pocket.MainActivity.OVERRIDE;
import static me.ghini.pocket.MainActivity.PLANT_CODE;


/**
 * A simple {@link Fragment} subclass.
 */
public class CollectFragment extends Fragment {
    private Uri imageUri;
    private int TAKE_PICTURE=1928003;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_collect, container, false);
        // mark local changes in local checkbox
        ((EditText) rootView.findViewById(R.id.etCollectSpecies)).addTextChangedListener(new AfterTextChangedWatcher(rootView) {
            @Override
            public void afterTextChanged(Editable editable) {
                ((CheckBox) rootView.findViewById(R.id.cbCollectOverride)).setChecked(true);
            }
        });
        ((EditText) rootView.findViewById(R.id.etCollectNumberOfPlants)).addTextChangedListener(new AfterTextChangedWatcher(rootView) {
            @Override
            public void afterTextChanged(Editable editable) {
                ((CheckBox) rootView.findViewById(R.id.cbCollectOverride)).setChecked(true);
            }
        });
        return rootView;
    }

    @Override
    public void setArguments(Bundle b) {
        TextView t = getActivity().findViewById(R.id.tvCollectAccession);
        t.setText(b.getString(PLANT_CODE, ""));
        t = getActivity().findViewById(R.id.etCollectSpecies);
        t.setText(b.getString(BINOMIAL, ""));
        t = getActivity().findViewById(R.id.tvCollectNumberOfPics);
        t.setText(b.getString(NO_OF_PICS, "0"));
        t = getActivity().findViewById(R.id.etCollectNumberOfPlants);
        t.setText(b.getString(NO_OF_PLANTS, "1"));
        CheckBox v = getActivity().findViewById(R.id.cbGrabPosition);
        v.setChecked(b.getBoolean(GRAB_POSITION, false));
        v = getActivity().findViewById(R.id.cbCollectOverride);
        v.setChecked(b.getBoolean(OVERRIDE, false));
    }
   
}
