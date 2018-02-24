package me.ghini.pocket;

import android.text.Editable;

/**
 * Created by mario on 2/21/18.
 */

public interface CommunicationInterface {
    public void switchToPage(int page);
    public void onCollectSpeciesChanged(Editable editable);
    public void onCollectNumberOfPlantsChanged(Editable editable);
}
