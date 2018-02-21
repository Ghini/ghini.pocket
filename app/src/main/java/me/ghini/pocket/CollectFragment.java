package me.ghini.pocket;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.File;


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
        return rootView;
    }

    public void refreshContent(Object accessionCode,
                               Object binomial,
                               Object numberOfPics,
                               Object numberOfPlants,
                               Object editPending) {
        TextView t = getActivity().findViewById(R.id.tvCollectAccession);
        t.setText((String)accessionCode);
        t = getActivity().findViewById(R.id.tvCollectSpecies);
        t.setText((String)binomial);
        t = getActivity().findViewById(R.id.tvCollectNumberOfPics);
        t.setText((String)numberOfPics);
        t = getActivity().findViewById(R.id.tvCollectNumberOfPlants);
        t.setText((String)numberOfPlants);
        t = getActivity().findViewById(R.id.tvCollectOverride);
        t.setText((String)editPending);
    }
   
}
