package me.ghini.pocket;

import android.os.Bundle;

import java.io.NotActiveException;

/**
 * Created by mario on 2018-04-26.
 */

public interface TableSource {
    Bundle getAt(int id);
    Bundle getAt(String lookup) throws NotActiveException;
    void putAt(int id, Bundle bundle) throws NotActiveException;
}
