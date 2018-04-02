package me.ghini.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mario on 2018-04-02.
 *
 * An Epithet is something that looks like a String (it shows as one).
 * Used in EpithetAdapter, based on its phonetic equivalent.
 */

class Epithet {
    final String show;
    final String phonetic;

    Epithet(String show, String phonetic) {
        this.show = show;
        this.phonetic = phonetic;
    }

    @NonNull
    @Override
    public String toString() {
        return show;
    }
}
