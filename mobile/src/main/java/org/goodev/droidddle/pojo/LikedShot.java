package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

/**
 * Created by goodev on 2014/12/30.
 */
public class LikedShot implements Parcelable {
    public static final Creator<LikedShot> CREATOR = new Creator<LikedShot>() {
        public LikedShot createFromParcel(Parcel source) {
            return new LikedShot(source);
        }

        public LikedShot[] newArray(int size) {
            return new LikedShot[size];
        }
    };
    public Long id;
    public Date createdAt;
    public User user;
    public Shot shot;

    public LikedShot() {
    }

    private LikedShot(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        this.user = in.readParcelable(User.class.getClassLoader());
        this.shot = in.readParcelable(Shot.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeParcelable(this.user, 0);
        dest.writeParcelable(this.shot, 0);
    }
}
