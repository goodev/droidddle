package org.goodev.droidddle.frag.shot;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.goodev.droidddle.R;
import org.goodev.droidddle.holder.AttachmentViewHolder;
import org.goodev.droidddle.pojo.Attachment;
import org.goodev.droidddle.widget.BaseAdapter;

/**
 * Created by ADMIN on 2014/12/28.
 */
public class ShotAttachmentAdatper extends BaseAdapter<Attachment> {
    private boolean mIsSelf;
    private long mShotId;

    public ShotAttachmentAdatper(Activity context, long shotId, boolean isSelf) {
        super(context);
        mIsSelf = isSelf;
        mShotId = shotId;
    }

    @Override
    public long getContentItemId(int position) {
        return mDataList.get(position).id;
    }


    @Override
    protected RecyclerView.ViewHolder onCreateContentViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.shot_attachment_item, parent, false);
        return new AttachmentViewHolder(view, mContext);
    }

    @Override
    public void onBindContentViewHolder(RecyclerView.ViewHolder holder, int position) {
        Attachment data = getItem(position);
        AttachmentViewHolder h = (AttachmentViewHolder) holder;
        h.setData(data, mIsSelf, mShotId);

    }

}
