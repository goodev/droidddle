package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Bucket implements Parcelable {

    public static final Creator<Bucket> CREATOR = new Creator<Bucket>() {
        public Bucket createFromParcel(Parcel source) {
            return new Bucket(source);
        }

        public Bucket[] newArray(int size) {
            return new Bucket[size];
        }
    };
    public Long id;
    public String name;
    public String description;
    public Integer shotsCount;
    public Date createdAt;
    public Date updatedAt;
    public User user;

    public Bucket() {
    }

    private Bucket(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.name = in.readString();
        this.description = in.readString();
        this.shotsCount = (Integer) in.readValue(Integer.class.getClassLoader());
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
        dest.writeString(this.name);
        dest.writeString(this.description);
        dest.writeValue(this.shotsCount);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeParcelable(this.user, 0);
    }
}
