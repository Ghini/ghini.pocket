package me.ghini.pocket;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class CollectFragment extends Fragment {
    private Uri imageUri;
    private int TAKE_PICTURE=1928003;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collect, container, false);
    }

    public void refreshContent(String accessionCode,
                                 String binomial,
                                 String numberOfPics,
                                 String numberOfPlants,
                                 String editPending) {
        TextView t = getActivity().findViewById(R.id.tvCollectAccession);
        t.setText(accessionCode);
        t = getActivity().findViewById(R.id.tvCollectSpecies);
        t.setText(binomial);
        t = getActivity().findViewById(R.id.tvCollectNumberOfPics);
        t.setText(numberOfPics);
        t = getActivity().findViewById(R.id.tvCollectNumberOfPlants);
        t.setText(numberOfPlants);
        t = getActivity().findViewById(R.id.tvCollectOverride);
        t.setText(editPending);
    }
}
