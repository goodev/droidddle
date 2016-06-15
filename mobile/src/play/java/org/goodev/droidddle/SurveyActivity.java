package org.goodev.droidddle;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;

import org.goodev.droidddle.frag.ShotsAdapter;
import org.goodev.droidddle.utils.Ads;

import butterknife.ButterKnife;
import butterknife.InjectView;


public class SurveyActivity extends UpActivity {

    @InjectView(R.id.container)
    FrameLayout mContainer;

    @InjectView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_survey);
        ButterKnife.inject(this);
        setNavDrawerItem(R.id.drawer_rewards);
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layout);
        mRecyclerView.setHasFixedSize(false);
        ShotsAdapter mAdapter = new ShotsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);
        Ads.setSupportAds(this, mAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Ads.onBackKey(this);
    }

}
