package org.goodev.droidddle.widget;

import android.os.Parcelable;

/**
 * Created by goodev on 2015/1/13.
 */
public interface OnOperationListener<T extends Parcelable> {
    /**
     * @param data
     * @param position maybe -1
     */
    void update(T data, int position);

    /**
     * @param data
     * @param position maybe -1
     */
    void delete(T data, int position);
}
