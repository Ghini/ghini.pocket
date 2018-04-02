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
            return ((Epithet)resultValue).show;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            if (constraint != null) {
                String phonetic = TaxonomyDatabase.shorten(constraint.toString());
                ArrayList<Epithet> suggestions = new ArrayList<>();
                for (Epithet epithet : mEpithets) {
                    if (epithet.phonetic.startsWith(phonetic)) {
                        suggestions.add(epithet);
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
        // copy all the epithets into a master list
        mEpithets = new ArrayList<>(epithets.size());
        mEpithets.addAll(epithets);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return mFilter;
    }
}
