package org.goodev.droidddle.pojo;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Shot implements Parcelable {
    public static final Parcelable.Creator<Shot> CREATOR = new Parcelable.Creator<Shot>() {
        public Shot createFromParcel(Parcel source) {
            return new Shot(source);
        }

        public Shot[] newArray(int size) {
            return new Shot[size];
        }
    };
    private static final String KEY_IMAGE_URI = "imageUri";
    private static final String KEY_TITLE = "title";
    private static final String KEY_BYLINE = "byline";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_VIEW_INTENT = "viewIntent";
    private static final String KEY_DETAILS_URI = "detailsUri";
    private static final String KEY_DES = "des";
    public Long id;
    public String title;
    public String description;
    public Integer width;
    public Integer height;
    public Image images;
    public Integer viewsCount;
    public Integer likesCount;
    public Integer commentsCount;
    public Integer attachmentsCount;
    public Integer reboundsCount;
    public Integer bucketsCount;
    public Date createdAt;
    public Date updatedAt;
    public String htmlUrl;
    public String attachmentsUrl;
    public String bucketsUrl;
    public String commentsUrl;
    public String likesUrl;
    public String projectsUrl;
    public String reboundsUrl;
    public String reboundSourceUrl;
    public List<String> tags = new ArrayList<String>();
    public User user;
    public Team team;

    public Shot() {
    }

    private Shot(Parcel in) {
        this.id = (Long) in.readValue(Long.class.getClassLoader());
        this.title = in.readString();
        this.description = in.readString();
        this.width = (Integer) in.readValue(Integer.class.getClassLoader());
        this.height = (Integer) in.readValue(Integer.class.getClassLoader());
        this.images = in.readParcelable(Image.class.getClassLoader());
        this.viewsCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.likesCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.commentsCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.attachmentsCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.reboundsCount = (Integer) in.readValue(Integer.class.getClassLoader());
        this.bucketsCount = (Integer) in.readValue(Integer.class.getClassLoader());
        long tmpCreatedAt = in.readLong();
        this.createdAt = tmpCreatedAt == -1 ? null : new Date(tmpCreatedAt);
        long tmpUpdatedAt = in.readLong();
        this.updatedAt = tmpUpdatedAt == -1 ? null : new Date(tmpUpdatedAt);
        this.htmlUrl = in.readString();
        this.attachmentsUrl = in.readString();
        this.bucketsUrl = in.readString();
        this.commentsUrl = in.readString();
        this.likesUrl = in.readString();
        this.projectsUrl = in.readString();
        this.reboundsUrl = in.readString();
        this.reboundSourceUrl = in.readString();
        this.tags = new ArrayList<String>();
        in.readList(this.tags, List.class.getClassLoader());
        this.user = in.readParcelable(User.class.getClassLoader());
        this.team = in.readParcelable(Team.class.getClassLoader());
    }

    /**
     * Deserializes an artwork object from a {@link Bundle}.
     */
    public static Shot fromBundle(Bundle bundle) {
        Shot shot = new Shot();
        shot.title = bundle.getString(KEY_TITLE);
        shot.description = bundle.getString(KEY_DES);
        shot.htmlUrl = bundle.getString(KEY_DETAILS_URI);
        shot.id = bundle.getLong(KEY_TOKEN);

        String imageUri = bundle.getString(KEY_IMAGE_URI);
        if (!TextUtils.isEmpty(imageUri)) {
//            builder.imageUri(Uri.parse(imageUri));
        }

//        try {
//            String viewIntent = bundle.getString(KEY_VIEW_INTENT);
//            if (!TextUtils.isEmpty(viewIntent)) {
//                builder.viewIntent(Intent.parseUri(viewIntent, Intent.URI_INTENT_SCHEME));
//            }
//        } catch (URISyntaxException ignored) {
//        }

        return shot;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeValue(this.width);
        dest.writeValue(this.height);
        dest.writeParcelable(this.images, flags);
        dest.writeValue(this.viewsCount);
        dest.writeValue(this.likesCount);
        dest.writeValue(this.commentsCount);
        dest.writeValue(this.attachmentsCount);
        dest.writeValue(this.reboundsCount);
        dest.writeValue(this.bucketsCount);
        dest.writeLong(createdAt != null ? createdAt.getTime() : -1);
        dest.writeLong(updatedAt != null ? updatedAt.getTime() : -1);
        dest.writeString(this.htmlUrl);
        dest.writeString(this.attachmentsUrl);
        dest.writeString(this.bucketsUrl);
        dest.writeString(this.commentsUrl);
        dest.writeString(this.likesUrl);
        dest.writeString(this.projectsUrl);
        dest.writeString(this.reboundsUrl);
        dest.writeString(this.reboundSourceUrl);
        dest.writeList(this.tags);
        dest.writeParcelable(this.user, 0);
        dest.writeParcelable(this.team, flags);
    }

    /**
     * Serializes this artwork object to a {@link android.os.Bundle} representation.
     */
    public Bundle toBundle() {
        Bundle bundle = new Bundle();

        bundle.putString(KEY_IMAGE_URI, Utils.getShotImageUrl(this));
        bundle.putString(KEY_TITLE, title);
        bundle.putString(KEY_DETAILS_URI, htmlUrl);
        bundle.putString(KEY_DES, description);
        bundle.putString(KEY_BYLINE, Utils.getShotUserName(this));
        bundle.putLong(KEY_TOKEN, id);
        bundle.putString(KEY_VIEW_INTENT, Utils.getShotViewIntent(this));
        return bundle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Shot shot = (Shot) o;
        if (id == null) {
            return false;
        }

        return id.equals(shot.id);

    }

    @Override
    public int hashCode() {
        if (id == null) {
            return super.hashCode();
        }
        return id.hashCode();
    }
}
