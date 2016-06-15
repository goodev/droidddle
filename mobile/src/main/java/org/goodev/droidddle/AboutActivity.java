package org.goodev.droidddle;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.goodev.droidddle.utils.Pref;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class AboutActivity extends UpActivity {

    @InjectView(R.id.version_view)
    TextView mVersionView;
    @InjectView(R.id.launch_icon_view)
    TextView mIconView;
    @InjectView(R.id.translate_view)
    TextView mTranslateView;
    @InjectView(R.id.ads_layout)
    FrameLayout mAdsLayout;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_about);
        ButterKnife.inject(this);
        setNavDrawerItem(R.id.drawer_about);
        String about = getString(R.string.about_summary, UiUtils.getVersion(this));
        mVersionView.setText(about);

        if (Pref.isShowAd(this)) {
            int type = UiUtils.getAdsBannerType(this);
        }
    }


    @OnClick(R.id.changelog_view)
    void showChangeLog(View view) {
        UiUtils.showChangeLog(this);

    }

    @OnClick(R.id.translate_view)
    void showTranslate(View view) {
        UiUtils.showTranslate(this);

    }

    @OnClick(R.id.icon)
    void copyAddress(View view) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        String label = getResources().getString(R.string.donation_label);
        String text = getResources().getString(R.string.donation_address);
        android.content.ClipData clip = android.content.ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, R.string.donation_copy_tip, Toast.LENGTH_LONG).show();
    }
}
