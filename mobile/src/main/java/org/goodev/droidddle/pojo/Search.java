package org.goodev.droidddle.pojo;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by goodev on 2015/1/7.
 */
public class Search implements Parcelable {
    public static final String Q = "q";
    public static final String PAGE = "page";
    public static final Creator<Search> CREATOR = new Creator<Search>() {
        public Search createFromParcel(Parcel source) {
            return new Search(source);
        }

        public Search[] newArray(int size) {
            return new Search[size];
        }
    };
    public String q;
    public int page;

    public Search() {
    }

    private Search(Parcel in) {
        this.q = in.readString();
        this.page = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.q);
        dest.writeInt(this.page);
    }
}
