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

import static me.ghini.pocket.MainActivity.BINOMIAL;
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
        return rootView;
    }

    @Override
    public void setArguments(Bundle b) {
        TextView t = getActivity().findViewById(R.id.tvCollectAccession);
        t.setText(b.getString(PLANT_CODE));
        t = getActivity().findViewById(R.id.tvCollectSpecies);
        t.setText(b.getString(BINOMIAL));
        t = getActivity().findViewById(R.id.tvCollectNumberOfPics);
        t.setText(b.getString(NO_OF_PICS));
        t = getActivity().findViewById(R.id.tvCollectNumberOfPlants);
        t.setText(b.getString(NO_OF_PLANTS));
        t = getActivity().findViewById(R.id.tvCollectOverride);
        t.setText(b.getString(OVERRIDE));
    }
   
}
