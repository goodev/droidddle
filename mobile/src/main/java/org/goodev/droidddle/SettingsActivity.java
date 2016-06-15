package org.goodev.droidddle;

import android.os.Bundle;

import org.goodev.droidddle.frag.SettingsFragment;

import butterknife.ButterKnife;

public class SettingsActivity extends UpActivity {
    private SettingsFragment mSettingsFragment;

    @Override
    protected void onMyCreate(Bundle savedInstanceBundle) {
        setContentView(R.layout.activity_settings);
        ButterKnife.inject(this);
        setNavDrawerItem(R.id.drawer_settings);
        if (savedInstanceBundle == null) {
            SettingsFragment fragment = new SettingsFragment();
            mSettingsFragment = fragment;
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        } else {
            mSettingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentById(R.id.container);
        }
        //        int type = UiUtils.getAdsBannerType(this);
        //        AppFlood.showBanner(this, AppFlood.BANNER_POSITION_BOTTOM, type);
    }

    public void updateDonatePreference() {
    }
}
