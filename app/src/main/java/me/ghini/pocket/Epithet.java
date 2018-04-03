package me.ghini.pocket;

import android.support.annotation.NonNull;

/**
 * Created by mario on 2018-04-02.
 *
 * An Epithet is something that looks like a String (it shows as one).
 * Used in EpithetAdapter, based on its phonetic equivalent.
 */

class Epithet {
    final String epithet;
    final String phonetic;
    private final String family;
    private final String accepted;
    boolean isExact;

    Epithet(String epithet, String family, String accepted, String phonetic) {
        this.epithet = epithet;
        this.family = family;
        this.accepted = accepted;
        this.phonetic = phonetic;
        isExact = true;
    }

    Epithet(Epithet e) {
        this.epithet = e.epithet;
        this.family = e.family;
        this.accepted = e.accepted;
        this.phonetic = e.phonetic;
        isExact = false;
    }

    void makeExact() {
        isExact = true;
    }

    void makePhonetic() {
        isExact = false;
    }

    @NonNull
    @Override
    public String toString() {
        if (accepted != null) {
            return epithet + " → " + accepted;
        } else {
            return epithet + " (" + family + ")";
        }
    }
}
