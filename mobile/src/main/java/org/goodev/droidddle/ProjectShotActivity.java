package org.goodev.droidddle;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import org.goodev.droidddle.frag.ProjectShotFragment;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;


public class ProjectShotActivity extends UpActivity {

    private long mId;
    private String mName;
    private int mType;
    private boolean mIsSelf;

    private String mTitle;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_project_shot);
        ButterKnife.inject(this);
        Bundle extra = getIntent().getExtras();
        mIsSelf = extra.getBoolean(UiUtils.ARG_SELF, false);
        mId = extra.getLong(UiUtils.ARG_ID);
        mName = extra.getString(UiUtils.ARG_NAME);
        mType = extra.getInt(UiUtils.ARG_TYPE);
        int resId = mType == UiUtils.TYPE_BUCKET ? R.string.title_activity_bucket_shot : R.string.title_activity_project_shot;
        mTitle = getString(resId, mName);

        if (savedInstanceState == null) {
            Fragment fragment = ProjectShotFragment.newInstance(mId, mType, mIsSelf);
            getSupportFragmentManager().beginTransaction().add(R.id.container, fragment).commit();
        }

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setTitle(mTitle);
    }
}
