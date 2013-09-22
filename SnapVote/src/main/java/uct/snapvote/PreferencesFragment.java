package uct.snapvote;

import android.preference.PreferenceFragment;
import android.os.Bundle;

/**
 * Configurable parameters pulled from the prferences XML file
 */
public class PreferencesFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

}