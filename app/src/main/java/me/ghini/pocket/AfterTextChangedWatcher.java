package me.ghini.pocket;

import android.text.TextWatcher;
import android.view.View;

/**
 * Created by mario on 2018-02-24.
 */

abstract class AfterTextChangedWatcher implements TextWatcher {
    View rootView;
    AfterTextChangedWatcher(View view) { rootView = view; }
    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*empty*/}
    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*empty*/}
}
