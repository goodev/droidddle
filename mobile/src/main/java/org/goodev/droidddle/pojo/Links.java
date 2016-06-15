package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Links implements Parcelable {

    public String web;
    public String twitter;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.web);
        dest.writeString(this.twitter);
    }

    public Links() {
    }

    private Links(Parcel in) {
        this.web = in.readString();
        this.twitter = in.readString();
    }

    public static final Creator<Links> CREATOR = new Creator<Links>() {
        public Links createFromParcel(Parcel source) {
            return new Links(source);
        }

        public Links[] newArray(int size) {
            return new Links[size];
        }
    };
}
