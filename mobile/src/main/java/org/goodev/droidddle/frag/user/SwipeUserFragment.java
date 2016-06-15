package org.goodev.droidddle.frag.user;

import android.os.Handler;
import android.os.Parcelable;

import org.goodev.droidddle.utils.UiUtils;

/**
 * Created by goodev on 2015/1/5.
 */
public abstract class SwipeUserFragment<E, T extends Parcelable> extends BaseUserFragment<T>/* implements UndoBar.Listener*/ {

    private Handler mHandler = new Handler();
    private Runnable mRemoveRunnable = () -> {
        removeDelayedItem();
    };
    private E mRemoveData;


    protected void removeDelayedItem() {
        if (mRemoveData != null) {
            removeItem(mRemoveData);
            mRemoveData = null;
        }
    }

    protected void removeItemDelayed(E data) {
        showUndoBar();
        if (mRemoveData != null) {
            mHandler.removeCallbacks(mRemoveRunnable);
            removeItem(mRemoveData);
            mRemoveData = null;
        }
        mRemoveData = data;
        mHandler.postDelayed(mRemoveRunnable, UiUtils.DELAYED_TIME);

    }

    protected abstract void removeItem(E data);

    protected abstract int getUndoMessage();

    protected void showUndoBar() {
//        new UndoBar.Builder(getActivity())//
//                .setMessage(getUndoMessage())//
//                .setListener(this)//
//                .setDuration(UiUtils.UNDO_BAR_TIME).show();
    }

//    @Override
//    public void onHide() {
//
//    }
//
//    @Override
//    public void onUndo(Parcelable parcelable) {
//        if (mRemoveData != null) {
//            getAdapter().addData(mRemoveData);
//        }
//        mRemoveData = null;
//        mHandler.removeCallbacks(mRemoveRunnable);
//    }

}
