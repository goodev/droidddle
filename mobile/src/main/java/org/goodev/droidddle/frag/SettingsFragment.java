package org.goodev.droidddle.frag;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v4.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import org.goodev.droidddle.R;
import org.goodev.droidddle.ThemeActivity;
import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.Settings;
import org.goodev.droidddle.utils.ThemeUtil;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.ThemeListPreference;

import java.io.File;

public class SettingsFragment extends PreferenceFragment {
    protected static final int REQUEST_DIRECTORY = 1;
    private static final int PICK_FILE_CODE = 111;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();

            if (preference instanceof ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(stringValue);

                // Set the summary to reflect the new value.
                preference.setSummary(index >= 0 ? listPreference.getEntries()[index] : null);

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.setSummary(stringValue);
            }
            return true;
        }
    };

    public static void setupRatePref(Preference preference) {
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                final Context ctx = preference.getContext();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + ctx.getPackageName()));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(intent);
                return true;
            }
        });
    }

    public static void setupTranslatePref(Preference findPreference) {
        findPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                final Context ctx = preference.getContext();
                //                new MaterialDialog.Builder((Activity) preference.getContext()).title(R.string.translate_title).content(R.string.translate_message).positiveText(R.string.translate_button).callback(new MaterialDialog.SimpleCallback() {
                //                    @Override
                //                    public void onPositive(MaterialDialog materialDialog) {
                //                        Intent intent = new Intent(Intent.ACTION_VIEW);
                //                        intent.setData(Uri.parse("https://goodev.oneskyapp.com"));
                //                        ctx.startActivity(intent);
                //                    }
                //                }).build().show();
                new AlertDialog.Builder((Activity) preference.getContext())
                        .setTitle(R.string.translate_title).setMessage(R.string.translate_message)
                        .setPositiveButton(R.string.translate_button,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(Intent.ACTION_VIEW);
                                        intent.setData(Uri.parse("https://goodev.oneskyapp.com"));
                                        ctx.startActivity(intent);
                                    }
                                }).create().show();

                return true;
            }
        });
    }

    /**
     * Binds a preference's summary to its value. More specifically, when the
     * preference's value is changed, its summary (line of text below the
     * preference title) is updated to reflect the value. The summary is also
     * immediately updated upon calling this method. The exact display format is
     * dependent on the type of preference.
     *
     * @see #sBindPreferenceSummaryToValueListener
     */
    private static void bindPreferenceSummaryToValue(Preference preference) {
        // Set the listener to watch for value changes.
        preference.setOnPreferenceChangeListener(sBindPreferenceSummaryToValueListener);

        // Trigger the listener immediately with the preference's
        // current value.
        sBindPreferenceSummaryToValueListener.onPreferenceChange(preference, PreferenceManager.getDefaultSharedPreferences(preference.getContext()).getString(preference.getKey(), ""));
    }

    public void setupAboutLikePref(Preference preference) {
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                UiUtils.showShotLikeAndUnlikeDialog(getActivity());
                return true;
            }
        });
    }

    public void setupDownloadPathPref(Preference preference) {
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        preference.setSummary(Pref.getDownloadDir(getActivity(), dir));
        preference.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {
                Context mContext = getActivity();
                if (UiUtils.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    UiUtils.showFileDirPicker(SettingsFragment.this, PICK_FILE_CODE);
                } else {
                    Toast.makeText(mContext, R.string.sdcard_permission_tips, Toast.LENGTH_LONG).show();
                    UiUtils.requestPermissions(mContext, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 111);
                }
                return true;
            }
        });
    }


    private boolean isFree() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().getPackageName().equals("org.goodev.droidddle");
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_settings);

        setupTranslatePref(findPreference(Settings.TRANSLATE));
        setupRatePref(findPreference(Settings.RATE_APP));
        setupAboutLikePref(findPreference(Settings.ABOUT_LIKE));
        setupDownloadPathPref(findPreference(Settings.DOWNLOAD_PATH));

        if (Settings.IS_PRO && !isFree()) {
            getPreferenceScreen().removePreference(findPreference(Settings.UPGRADE_PRO));
        } else {
            //TODO just remove this now
            getPreferenceScreen().removePreference(findPreference(Settings.UPGRADE_PRO));
        }

        bindPreferenceSummaryToValue(findPreference(Settings.ORIENTATION));
        bindPreferenceSummaryToValue(findPreference(Settings.THEME));

        ThemeListPreference pref = (ThemeListPreference) findPreference(Settings.THEME);

        pref.setOnThemeChangeListener(new OnPreferenceChangeListener() {

            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Pref.setTheme(getActivity(), newValue.toString());
                ThemeUtil.reloadTheme((ThemeActivity) getActivity());
                return true;
            }
        });

        //        String about = getString(R.string.about_summary, UiUtils.getVersion(getActivity()));
        //        findPreference(Settings.ABOUT_APP).setSummary(about);
    }

    @SuppressLint("SdCardPath")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_FILE_CODE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if (uri != null) {
                String path = uri.getPath();
                Pref.setDownloadDir(getActivity(), path);
                findPreference(Settings.DOWNLOAD_PATH).setSummary(path);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
