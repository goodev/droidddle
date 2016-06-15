package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by goodev on 2014/12/30.
 */
public class Following implements Parcelable {
    public static final Creator<Following> CREATOR = new Creator<Following>() {
        public Following createFromParcel(Parcel source) {
            return new Following(source);
        }

        public Following[] newArray(int size) {
            return new Following[size];
        }
    };
    public Long id;
    public Date createdAt;
    public User followee;

    public Following() {
    }

    private Following(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        this.followee = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeParcelable(this.followee, 0);
    }
}
