package org.goodev.droidddle.frag;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;

import org.goodev.droidddle.R;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class ManageSpaceFragment extends android.support.v4.preference.PreferenceFragment implements OnPreferenceClickListener {

    private static final String PREVIEW_CACHE = "clear_cache_data";
    private static final String PREVIEW_IMAGE = "clear_download_image_data";
    private static final int TYPE_CACHE = 1;
    private static final int TYPE_IMAGES = 2;
    /**
     * A preference value change listener that updates the preference's summary
     * to reflect its new value.
     */
    private static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
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
    private Preference mCachePref;
    private Preference mImagesPref;
    private int mType;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_manage_space);

        mCachePref = findPreference(PREVIEW_CACHE);
        mImagesPref = findPreference(PREVIEW_IMAGE);
        mCachePref.setOnPreferenceClickListener(this);
        mImagesPref.setOnPreferenceClickListener(this);

        update();
    }

    @SuppressWarnings("deprecation")
    public void setupCacheData(Size result) {
        mCachePref = findPreference(PREVIEW_CACHE);
        mCachePref.setSummary(getString(R.string.pref_description_clear_cache_data, UiUtils.getFileSizeString(result.cache)));

        mImagesPref = findPreference(PREVIEW_IMAGE);
        mImagesPref.setSummary(getString(R.string.pref_description_clear_download_image_data, UiUtils.getFileSizeString(result.shotImages)));

    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mImagesPref) {
            showDeleteDialog(TYPE_IMAGES);
        } else if (preference == mCachePref) {
            showDeleteDialog(TYPE_CACHE);
        }
        return true;
    }

    private void showDeleteDialog(int type) {
        mType = type;
        int title = R.string.dialog_delete_title;
        int content = R.string.dialog_delete_message;
        switch (type) {
            case TYPE_IMAGES:
                title = R.string.dialog_title_clear_images;
                content = R.string.dialog_message_clear_images;
                break;
            case TYPE_CACHE:
                title = R.string.dialog_title_clear_cache;
                content = R.string.dialog_message_clear_cache;
                break;
        }

        //        final Dialog dialog = new MaterialDialog.Builder(getActivity()).title(title).content(content).positiveText(android.R.string.ok)  // the default is 'OK'
        //                .negativeText(android.R.string.cancel)  // leaving this line out will remove the negative button
        //                .callback(new MaterialDialog.SimpleCallback() {
        //
        //                    @Override
        //                    public void onPositive(MaterialDialog dialog) {
        //                        delete();
        //                    }
        //
        //                }).build();
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).setTitle(title)
                .setMessage(content).setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                delete();
                            }
                        })  // the default is 'OK'
                .setNegativeButton(android.R.string.cancel,
                        null)  // leaving this line out will remove the negative button
                .create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    protected void delete() {
        new DeleteTask(mType).execute();
    }

    public void update() {
        new UpdateTask().execute();
    }

    static class Progress {
        int total;
        int current;
    }

    class Size {
        public long cache;
        public long shotImages;
    }

    class UpdateTask extends AsyncTask<Void, Void, Size> {

        @Override
        protected Size doInBackground(Void... params) {
            Size size = new Size();
            final Context ctx = getActivity();
            size.cache = Utils.getPreviewCacheSize(ctx);
            size.shotImages = Utils.getCustomFontSize(ctx);
            return size;
        }

        @Override
        protected void onPostExecute(Size result) {
            super.onPostExecute(result);
            setupCacheData(result);
        }

    }

    class DeleteTask extends AsyncTask<Void, Progress, Boolean> {

        ProgressDialog mDialog;
        int mType;

        public DeleteTask(int type) {
            mType = type;
            mDialog = new ProgressDialog(getActivity());
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);
            if (mType == TYPE_CACHE) {
                mDialog.setIndeterminate(true);
                mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                mDialog.setMessage(getString(R.string.dialog_delete_file));
            } else {
                mDialog.setTitle(R.string.dialog_delete_file);
                mDialog.setIndeterminate(false);
                mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            }
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog.show();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            mDialog.dismiss();
            update();
        }

        @Override
        protected void onProgressUpdate(Progress... values) {
            super.onProgressUpdate(values);
            if (values != null && values[0] != null) {
                Progress p = values[0];
                mDialog.setMax(p.total);
                mDialog.setProgress(p.current);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            mDialog.dismiss();
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (mType == TYPE_IMAGES) {
                deleteShotImages();
            } else if (mType == TYPE_CACHE) {
                deleteCache();
            }
            return null;
        }

        private void deleteCache() {
            Utils.deleteCacheFiles(getActivity());
        }

        private void deleteShotImages() {
            Context context = getActivity();
            File fs = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);//Utils.getImageDir(context);
            File[] files = null;
            if (fs != null && fs.exists()) {
                files = fs.listFiles();
            }

            File[] interFiles = null;
            File inter = new File(context.getFilesDir(), "shots");
            if (inter != null && inter.exists()) {
                interFiles = inter.listFiles();
            }
            if (files == null && interFiles == null) {
                return;
            }
            ArrayList<File> files1 = new ArrayList<>();
            if (files != null) {
                files1.addAll(Arrays.asList(files));
            }
            if (interFiles != null) {
                files1.addAll(Arrays.asList(interFiles));
            }

            int length = files1.size();
            Progress p = new Progress();
            p.total = length;
            for (int i = 0; i < length; i++) {
                files1.get(i).delete();
                p.current = i;
                publishProgress(p);
            }
        }
    }
}
