package me.ghini.pocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

/**
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
