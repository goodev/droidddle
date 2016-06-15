
package org.goodev.droidddle.widget;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import org.goodev.droidddle.R;
import org.goodev.droidddle.utils.Pref;

import java.util.ArrayList;

public class ThemeListPreference extends ListPreference {

    ThemeListPreferenceAdapter customListPreferenceAdapter = null;
    Context mContext;
    CharSequence[] entries;
    CharSequence[] entryValues;
    String[] colors;
    ArrayList<RadioButton> rButtonList;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;
    View mView;
    OnPreferenceChangeListener mThemeListener;
    private LayoutInflater mInflater;
    private int mEntryIndex;
    private float mDensity = 0;

    public ThemeListPreference(Context context) {
        super(context);
        init(context);
    }

    public ThemeListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public void init(Context context) {
        mContext = context;
        mDensity = context.getResources().getDisplayMetrics().density;
        colors = context.getResources().getStringArray(R.array.pref_theme_list_colors);
        mInflater = LayoutInflater.from(context);
        rButtonList = new ArrayList<RadioButton>();
        prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        editor = prefs.edit();

        entries = getEntries();
        entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            return;
        }

        String value = prefs.getString(getKey(), null);
        mEntryIndex = findIndexOfValues(value, entryValues);
        if (mEntryIndex != -1) {
            // setSummary(getColorSummary(mEntryIndex));
        }
    }

    public CharSequence getColorSummary(int index) {
        CharSequence value = entries[index];
        Spannable summary = new SpannableString(value);
        int color = Color.parseColor(colors[index]);
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        summary.setSpan(span, 0, value.length(), 0);
        // SpannableStringBuilder builder = new SpannableStringBuilder(value);
        // builder.setSpan(span, 0, value.length(),
        // Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return summary;
    }

    public int findIndexOfValues(String value, CharSequence[] mEntryValues) {
        if (value != null && mEntryValues != null) {
            for (int i = mEntryValues.length - 1; i >= 0; i--) {
                if (mEntryValues[i].equals(value)) {
                    return i;
                }
            }
        }
        return Pref.DEFAULT_THEME;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        super.onSetInitialValue(restoreValue, defaultValue);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mEntryIndex >= 0 && entryValues != null) {
            String value = entryValues[mEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {

        entries = getEntries();
        entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException("ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        mEntryIndex = findIndexOfValues(getValue(), entryValues);

        OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mEntryIndex = which;
                // setSummary(getColorSummary(mEntryIndex));
                editor.putString(getKey(), entryValues[which].toString()).commit();
                /*
                 * Clicking on an item simulates the positive button click, and
                 * dismisses the dialog.
                 */
                if (mThemeListener != null) {
                    mThemeListener.onPreferenceChange(ThemeListPreference.this, which);
                }
                ThemeListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                dialog.dismiss();
            }
        };
        ThemeListPreferenceAdapter adapter = new ThemeListPreferenceAdapter(mContext);
        builder.setSingleChoiceItems(adapter, mEntryIndex, listener);

        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        builder.setPositiveButton(null, null);
    }

    public void setOnThemeChangeListener(OnPreferenceChangeListener listener) {
        mThemeListener = listener;
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        mView = view;
        setPreviewColor();
    }

    private void setPreviewColor() {
        if (mView == null)
            return;
        ImageView iView = new ImageView(getContext());
        LinearLayout widgetFrameView = ((LinearLayout) mView.findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null)
            return;
        widgetFrameView.setVisibility(View.VISIBLE);
        widgetFrameView.setPadding(widgetFrameView.getPaddingLeft(), widgetFrameView.getPaddingTop(), (int) (mDensity * 8), widgetFrameView.getPaddingBottom());
        // remove already create preview image
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }
        widgetFrameView.addView(iView);
        widgetFrameView.setMinimumWidth(0);
        iView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
        iView.setImageBitmap(getPreviewBitmap(mDensity, colors[mEntryIndex]));
    }

    public static Bitmap getPreviewBitmap(float density, String colorStr) {
        int color = Color.GRAY;
        try {
            color = Color.parseColor(colorStr);
        } catch (Exception e) {
        }
        return getPreviewBitmap(density, color);
    }

    public static Bitmap getPreviewBitmap(float density, int color) {
        if (density == 0) {
            density = 1.5f;
        }
        int d = (int) (density * 31); // 30dip
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    private class ThemeListPreferenceAdapter extends BaseAdapter {
        public ThemeListPreferenceAdapter(Context context) {

        }

        public int getCount() {
            return entries.length;
        }

        public Object getItem(int position) {
            return entries[position];
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            View row = convertView;

            if (row == null) {
                row = mInflater.inflate(android.R.layout.simple_list_item_single_choice, parent, false);
            }
            CheckedTextView tv = (CheckedTextView) row.findViewById(android.R.id.text1);
            tv.setText(getItem(position).toString());
            tv.setChecked(mEntryIndex == position);
            tv.setTextColor(Color.parseColor(colors[position]));
            return row;
        }

    }
}
