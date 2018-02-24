package me.ghini.pocket;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by mario on 2018-02-24.
 */

abstract class FragmentWithState extends Fragment {
    public Bundle state;
    public FragmentWithState() {
        state = new Bundle();
    }
    public abstract void updateView();
}
