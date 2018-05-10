package me.ghini.pocket;
/*
  This file is part of ghini.pocket.

  ghini.pocket is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by the Free
  Software Foundation, either version 3 of the License, or (at your option)
  any later version.

  ghini.pocket is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
  for more details.

  You should have received a copy of the GNU General Public License along
  with ghini.pocket.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright © 2018 Mario Frasca. <mario@anche.no>
 *
 * Created by mario on 4/2/18.
 */

public class EpithetAdapter extends ArrayAdapter<Epithet> {
    private List<Epithet> mEpithets;

    private Filter mFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            return ((Epithet)resultValue).epithet;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                String exact = constraint.toString();
                String phonetic = TaxonomyDatabase.shorten(exact);
                exact = exact.substring(0, 1).toUpperCase() + exact.substring(1).toLowerCase();
                ArrayList<Epithet> suggestions = new ArrayList<>();
                for (Epithet epithet : mEpithets) {
                    if (epithet.isExact) {
                        if (epithet.epithet.startsWith(exact)) {
                            suggestions.add(epithet);
                        }
                    } else {
                        if (epithet.phonetic.startsWith(phonetic) &&
                                !epithet.epithet.startsWith(exact)) {
                            suggestions.add(epithet);
                        }
                    }
                }

                results.values = suggestions;
                results.count = suggestions.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            clear();
            if (results != null && results.count > 0) {
                // we have filtered results
                addAll((List<Epithet>) results.values);
            }
            notifyDataSetChanged();
        }
    };

    EpithetAdapter(Context context, int textViewResourceId, List<Epithet> epithets) {
        super(context, textViewResourceId, epithets);
        // copy all the epithets twice
        mEpithets = new ArrayList<>(epithets.size() * 2);
        // first to be used as exact matches (String×4 constructor)
        mEpithets.addAll(epithets);
        // then again to be used as phonetic matches (copy constructor)
        for(Epithet e : epithets) {
            mEpithets.add(new Epithet(e));
        }
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
