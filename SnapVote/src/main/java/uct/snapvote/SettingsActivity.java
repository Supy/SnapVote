package uct.snapvote;

import android.os.Bundle;
import android.app.Activity;

/**
 * Settings activity allows the user to customise major algorithm parameters as
 * contained in the PreferencesFragment
 */
public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // modify the view hierarchy
        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new PreferencesFragment()).commit();
    }
}
