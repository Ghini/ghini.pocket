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

import android.support.annotation.NonNull;

/**
 * Copyright © 2018 Mario Frasca. <mario@anche.no>
 *
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
