package org.goodev.droidddle;

import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import org.goodev.droidddle.utils.Pref;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class AboutAdActivity extends UpActivity implements CompoundButton.OnCheckedChangeListener {

    @InjectView(R.id.ads_switch)
    SwitchCompat mSwitchCompat;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_about_ad);
        ButterKnife.inject(this);
    }

    @Override
    protected void onPostMyCreate(Bundle savedInstanceState) {
        super.onPostMyCreate(savedInstanceState);
        mSwitchCompat.setChecked(Pref.isShowHomeAds(this));
        mSwitchCompat.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Pref.saveShowAdsStatus(this, isChecked);
    }
}
