package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Attachment implements Parcelable {
    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        public Attachment createFromParcel(Parcel source) {
            return new Attachment(source);
        }

        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };
    public Long id;
    public String url;
    public String thumbnailUrl;
    public Long size;
    /**
     * z
     * image/jpeg image/png  video/mp4  application/octet-stream  application/zip
     */
    public String contentType;
    public Integer viewsCount;
    public Date createdAt;

    public Attachment() {
    }

    private Attachment(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.url = in.readString();
        this.thumbnailUrl = in.readString();
        this.size = (Long) in.readValue(Long.class.getClassLoader());
        this.contentType = in.readString();
        this.viewsCount = (Integer) in.readValue(Integer.class.getClassLoader());
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.url);
        dest.writeString(this.thumbnailUrl);
        dest.writeValue(this.size);
        dest.writeString(this.contentType);
        dest.writeValue(this.viewsCount);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
    }
}
