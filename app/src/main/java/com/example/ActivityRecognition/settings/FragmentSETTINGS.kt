package com.example.ActivityRecognition.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.example.ActivityRecognition.R
import com.example.ActivityRecognition.userProfile.TheUserProfileActivity


// about how to create Settings Fragment
// reference: https://blog.csdn.net/liting870907/article/details/123417989?ops_request_misc=&request_id=&biz_id=102&utm_term=android%20studio%20PreferenceFragm&utm_medium=distribute.pc_search_result.none-task-blog-2~all~sobaiduweb~default-1-123417989.142^v51^pc_rank_34_2,201^v3^control_1&spm=1018.2226.3001.4187
class FragmentSETTINGS : PreferenceFragmentCompat() {

    @SuppressLint("CommitPrefEdits")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val userProfile = findPreference<Preference>("UP")
        val intent = Intent(context, TheUserProfileActivity::class.java)
        userProfile?.intent = intent
        // Todo: 解决刚开始没选unit preference的情况
        val manager: PreferenceManager = preferenceManager

        val listPreference = manager.findPreference<Preference>("UnitPreference") as ListPreference?

        if (listPreference != null) {
            println("Unit::" + listPreference.value)
        }

        val sp: SharedPreferences = requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()

        if (listPreference != null) {
            editor.putString("UnitPreference", listPreference.value)
        }

        editor.apply()
//
//        userProfile.setOnPreferenceClickListener {
//            fun onPreferenceClick(preference: Preference): Boolean{
//                val intent = Intent(this, TheUserProfileActivity::class.java)
//                // start your next activity
//                startActivity(intent)
//
//                return true
//            }
//
//        }
    }

    override fun onPause() {
        super.onPause()

        val manager: PreferenceManager = preferenceManager

        val listPreference = manager.findPreference<Preference>("UnitPreference") as ListPreference?

        if (listPreference != null) {
            println("Unit::" + listPreference.value)
        }

        val sp: SharedPreferences = requireActivity().getSharedPreferences("User", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sp.edit()

        if (listPreference != null) {
            editor.putString("UnitPreference", listPreference.value)
        }

        editor.apply()

    }

}