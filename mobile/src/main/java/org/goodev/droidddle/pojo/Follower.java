package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Follower implements Parcelable {
    public static final Creator<Follower> CREATOR = new Creator<Follower>() {
        public Follower createFromParcel(Parcel source) {
            return new Follower(source);
        }

        public Follower[] newArray(int size) {
            return new Follower[size];
        }
    };
    /**
     * 粉丝的id
     */
    //    public Long followeeId;
    public Long id;
    /**
     * 用户的id
     */
    //    public Long followerId;
    public Date createdAt;
    public User follower;

    public Follower() {
    }

    private Follower(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        this.follower = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeParcelable(this.follower, 0);
    }
}
