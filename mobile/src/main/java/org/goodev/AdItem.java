package org.goodev;

/**
 * Created by ADMIN on 2015/12/17.
 */
public class AdItem {
    public String id;
    public String name;
    public String pkg;
    public String icon;
    public String image;
    public String des;
    public String category;
    public double rating;
    public String action;
    public String size;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AdItem adItem = (AdItem) o;

        return !(pkg != null ? !pkg.equals(adItem.pkg) : adItem.pkg != null);

    }

    @Override
    public int hashCode() {
        return pkg != null ? pkg.hashCode() : 0;
    }
}
