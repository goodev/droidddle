package org.goodev.droidddle;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.goodev.droidddle.frag.SearchBaseFragment;
import org.goodev.droidddle.utils.UiUtils;
import org.goodev.droidddle.widget.ThemeListPreference;
import org.goodev.seekbar.SeekBarCompat;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import butterknife.OnLongClick;

/**
 * Created by yfcheng on 2015/8/21.
 */
public class ColorSearchActivity extends UpActivity {
    private final static String DEFAULT_COLOR = "C2185B";
    SearchBaseFragment mSearchFragment;
    String mQuery;
    int mPercent = 30;
    String mType;
    boolean mIsLarge;
    @InjectView(R.id.fab)
    FloatingActionButton mFab;
    float mDensity;
    @InjectView(R.id.color_picker)
    ImageView mColorPickerBtn;
    @InjectView(R.id.color_minimum)
    TextView mColorMinView;
    @InjectView(R.id.rangebar)
    SeekBarCompat mRangeBar;

    protected int getBastLayout() {
        return R.layout.activity_color_base_layout;
    }

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_search_color);
        mDensity = getResources().getDisplayMetrics().density;
        ButterKnife.inject(this);
        setupFab();
        setTitle(null);
        mIsLarge = getResources().getInteger(R.integer.shot_column) > 1;
        mRangeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                rangeChange(progress, fromUser);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mRangeBar.setProgress(30 / 5);

        updateColor();
    }

    private void updateColor() {
        if (TextUtils.isEmpty(mQuery)) {
            mQuery = DEFAULT_COLOR;
        }
        mColorPickerBtn.setImageBitmap(ThemeListPreference.getPreviewBitmap(mDensity, "#" + mQuery));
    }


    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        super.onPostMyCreate(savedInstanceState);
        mSearchFragment = (SearchBaseFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_fragment);
        if (savedInstanceState != null) {
            mQuery = savedInstanceState.getString(UiUtils.ARG_QUERY);
        } else {
            String query = getIntent().getStringExtra(UiUtils.ARG_QUERY);
            if (!TextUtils.isEmpty(query)) {
                try {
                    int color = Color.parseColor("#" + query);
                    mQuery = query;
                    mColorPickerBtn.setImageBitmap(ThemeListPreference.getPreviewBitmap(mDensity, color));
                    onSearchClick(null);
                } catch (Exception e) {
                }
            }
        }

    }

    private void rangeChange(int value, boolean user) {
        mPercent = value * 5;
        String text = getString(R.string.color_minimum, mPercent);
        mColorMinView.setText(text);
    }

    private void setupFab() {
        if (UiUtils.hasLollipop()) {
//            mFab.setColorRippleResId(R.color.ripple_material_light);
//            mFab.setColorPressed(ThemeUtil.getThemeColor(this, R.attr.navdrawerTintColor));
        } else {
//            mFab.setColorPressed(ThemeUtil.getThemeColor(this, R.attr.colorAccent));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UiUtils.ARG_QUERY, mQuery);
        outState.putString(UiUtils.ARG_TYPE, mType);
    }

    @OnClick(R.id.fab)
    void onFabClick(View view) {
        UiUtils.launchCreateActivity(this, UiUtils.TYPE_SHOT);

    }

    @OnClick(R.id.color_picker)
    void onPickColorClick(View view) {
        ColorPickerDialogBuilder.with(this)
                .setTitle(getString(R.string.pick_color_title))
                .initialColor(getIntColor())
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .lightnessSliderOnly()
                .setPositiveButton(getString(android.R.string.ok), new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        mQuery = String.format("%06X", 0xFFFFFF & lastSelectedColor);
                        updateColor();
                    }
                })
                .setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .build().show();
    }

    private int getIntColor() {
        if (TextUtils.isEmpty(mQuery)) {
            return Color.parseColor("#" + DEFAULT_COLOR);
        }
        return Color.parseColor("#" + mQuery);
    }

    @OnClick(R.id.search_button)
    void onSearchClick(View view) {
        mSearchFragment.updateSearch(mQuery, mPercent);
    }

    @OnLongClick(R.id.fab)
    boolean onFabLongClick(View view) {
        UiUtils.launchCreateActivity(this, UiUtils.TYPE_BUCKET);
        return true;
    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        if(Pref.needShowTagTips(this) || BuildConfig.DEBUG) {
//            View view = findViewById(R.id.main_content1);
//            Snackbar.make(view, R.string.tag_search_tips, Snackbar.LENGTH_INDEFINITE)
//                    .setAction(R.string.got_it, new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            Pref.setShowTagStatus(getApplicationContext(), false);
//                        }
//                    })
//                    .show();
//        }
//    }
}
