package com.example.os10.hands_freecontrols;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by OS 10 on 12/27/2017.
 */

public class SettingsActivity extends Activity{

    public static class SettingsFragment extends PreferenceFragment{

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            //Control whether a fragment instance is retained across Activity re-creation
            //(such as from a configuration change).
            setRetainInstance(true);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preference_fragment);

            /* Disable vertical speed if needed */
            final NumberPickerPreference vSpeedPreference = (NumberPickerPreference)
                    getPreferenceScreen().findPreference("vertical_speed");
            final NumberPickerPreference hSpeedPreference = (NumberPickerPreference)
                    getPreferenceScreen().findPreference("horizontal_speed");

        }

    }
}
