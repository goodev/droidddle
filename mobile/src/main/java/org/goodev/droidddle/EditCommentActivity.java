package org.goodev.droidddle;

import android.os.Bundle;

import org.goodev.droidddle.frag.EditCommentFragment;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.utils.UiUtils;

import butterknife.ButterKnife;


public class EditCommentActivity extends UpActivity {

    private long mShotId;
    private Comment mComment;

    @Override
    protected void onMyCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_edit_comment);
        ButterKnife.inject(this);
        Bundle extra = getIntent().getExtras();
        mShotId = extra.getLong(UiUtils.ARG_SHOT_ID);
        mComment = extra.getParcelable(UiUtils.ARG_COMMENT);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, EditCommentFragment.newInstance(mShotId, mComment))
                    .commit();
        }
    }

}
