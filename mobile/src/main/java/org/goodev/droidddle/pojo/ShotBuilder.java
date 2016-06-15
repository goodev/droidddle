package org.goodev.droidddle.pojo;

/**
 * Created by yfcheng on 2015/4/14.
 */
public class ShotBuilder {

    private Shot mShot;

    public ShotBuilder() {
        mShot = new Shot();
    }

    public ShotBuilder title(String title) {
        mShot.title = title;
        return this;
    }

}
