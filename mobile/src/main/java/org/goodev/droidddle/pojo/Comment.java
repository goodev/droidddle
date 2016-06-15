package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Comment implements Parcelable {
    public static final Creator<Comment> CREATOR = new Creator<Comment>() {
        public Comment createFromParcel(Parcel source) {
            return new Comment(source);
        }

        public Comment[] newArray(int size) {
            return new Comment[size];
        }
    };
    public Long id;
    public String body;
    public Integer likesCount;
    public String likesUrl;
    public Date createdAt;
    public Date updatedAt;
    public User user;

    public Comment() {
    }

    private Comment(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.body = in.readString();
        this.likesCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.likesUrl = in.readString();
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.user = in.readParcelable(User.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.body);
        dest.writeValue(this.likesCount);
        dest.writeString(this.likesUrl);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeParcelable(this.user, 0);
    }
}
