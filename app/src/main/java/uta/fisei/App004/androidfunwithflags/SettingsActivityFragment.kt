package uta.fisei.App004.androidfunwithflags

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.flagquizapp.androidfunwithflags.R

class SettingsActivityFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }
}