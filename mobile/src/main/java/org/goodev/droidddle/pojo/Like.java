package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Like implements Parcelable {
    public static final Creator<Like> CREATOR = new Creator<Like>() {
        public Like createFromParcel(Parcel source) {
            return new Like(source);
        }

        public Like[] newArray(int size) {
            return new Like[size];
        }
    };
    public Long id;
    public Long shotId;
    /**
     * 用户的id
     */
    public Long userId;
    public Date createdAt;
    public User user;

    public Like() {
    }

    private Like(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.shotId = (Long) in.readValue(Long.class.getClassLoader());
        this.userId = (Long) in.readValue(Long.class.getClassLoader());
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        this.user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeValue(this.shotId);
        dest.writeValue(this.userId);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeParcelable(this.user, 0);
    }
}
