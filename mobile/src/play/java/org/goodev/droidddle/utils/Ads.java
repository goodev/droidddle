package org.goodev.droidddle.utils;

import android.app.Activity;
import android.content.Intent;
import android.widget.RelativeLayout;

import com.adxmi.customizedad.AdManager;
import com.adxmi.customizedad.ContentAdModel;
import com.adxmi.customizedad.ContentAdRequestListener;

import org.goodev.AdItem;
import org.goodev.droidddle.App;
import org.goodev.droidddle.BuildConfig;
import org.goodev.droidddle.R;
import org.goodev.droidddle.SurveyActivity;
import org.goodev.droidddle.frag.ShotsAdapter;
import org.goodev.droidddle.pojo.Shot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Ads {
    public static void setupAds(Activity activity, RelativeLayout vg) {

    }

    public static void openServey(Activity activity) {
        Intent intent = new Intent(activity, SurveyActivity.class);
        activity.startActivity(intent);
    }


    static List<Shot> sAdShots = new ArrayList<>();
    static Set<AdItem> sAds = new HashSet<>();

    public static Set<AdItem> getAds() {
        return sAds;
    }

    public static AdItem toAdItem(ContentAdModel adModel) {
        AdItem item = new AdItem();
        item.action = adModel.getBtn();
        item.category = adModel.getCategory();
        item.des = adModel.getDes();
        item.icon = adModel.getIcon();
        item.id = adModel.getId();
        item.image = adModel.getCreatives();
        item.pkg = adModel.getPgn();
        item.name = adModel.getName();
        item.rating = adModel.getRating();
        item.size = adModel.getSize();
        return item;
    }

    public static Shot toAdShot(ContentAdModel adModel, long id) {

        Shot shot = new Shot();
        shot.id = id;
        shot.attachmentsUrl = adModel.getBtn();
        shot.bucketsUrl = adModel.getCategory();
        ;
        shot.description = adModel.getDes();
        shot.commentsUrl = adModel.getIcon();
        shot.title = adModel.getName();
        shot.htmlUrl = adModel.getId();
        shot.reboundsUrl = adModel.getCreatives();
        shot.reboundSourceUrl = adModel.getPgn();
        shot.likesCount = (int) (adModel.getRating() * 100);
        shot.projectsUrl = adModel.getSize();

        return shot;
    }

    public static boolean isAd(long id) {
        return id <= sAdStartId;
    }

    public static void setSupportAds(Activity context, ShotsAdapter adapter) {
        AdManager.getInstance(context).registerRequestAdListener(new ContentAdRequestListener() {
            @Override
            public void onRequestResult(List<ContentAdModel> list) {
                if (list == null) {
                    return;
                }
                List<Shot> shots = new ArrayList<Shot>();
                for (int i = 0; i < list.size(); i++) {
                    shots.add(toAdShot(list.get(i), sAdStartId - i));
                }
                adapter.addData(shots);
            }
        });
        AdManager.getInstance(context).requestAd(16);
    }

    public static long sAdStartId = -3111L;

    public static void checkUpdate(Activity context) {
        AdManager.getInstance(context).init(BuildConfig.AppId, BuildConfig.AppSecret, AdManager.TYPE_CONTENT);
        AdManager.getInstance(context).setEnableDebugLog(BuildConfig.DEBUG);
        AdManager.getInstance(context).registerRequestAdListener(new ContentAdRequestListener() {
            @Override
            public void onRequestResult(List<ContentAdModel> list) {
                if (list == null) {
                    return;
                }
                sAdShots.clear();
                for (int i = 0; i < list.size(); i++) {
                    sAdShots.add(toAdShot(list.get(i), sAdStartId - i));
                }
            }
        });
        AdManager.getInstance(context).requestAd(6);
    }

    static Random sRandom = new Random();

    public static void addAdToShot(List<Shot> shots, List<Shot> all) {
        if (sAdShots.isEmpty()) {
            return;
        }
        Shot shot = sAdShots.get(sRandom.nextInt(sAdShots.size()));
        if (all == null) {
            shots.add(shot);
            return;
        }
        if (!Utils.isAppInstalled(App.getContext(), shot.reboundSourceUrl) && !all.contains(shot)) {
            shots.add(shot);
        }
    }

    public static void onClickAd(Activity context, Shot shot) {
        AdManager.getInstance(context).onClickAd(shot.htmlUrl, context.getResources().getColor(
                R.color.system_bar_color_blue));
    }

    public static void onBackKey(Activity context) {
        AdManager.getInstance(context).onKeyBack();
    }
}
