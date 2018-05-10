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

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Copyright © 2018 Mario Frasca. <mario@anche.no>
 *
 * Created by mario on 2018-02-24.
 */

abstract class FragmentWithState extends Fragment {
    public Bundle state;
    public FragmentWithState() {
        state = new Bundle();
    }
    public abstract void updateView();
    public Bundle getState() { return state; }
}
