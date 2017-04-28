package org.goodev.droidddle.api;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import org.goodev.droidddle.App;
import org.goodev.droidddle.BuildConfig;
import org.goodev.droidddle.R;
import org.goodev.droidddle.pojo.Image;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.utils.ConnUtils;
import org.goodev.droidddle.utils.OAuthUtils;
import org.goodev.droidddle.utils.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.OkClient;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;

/**
 * Created by goodev on 2014/12/19.
 */
public class ApiFactory {

    public static final String SHOTS = "/shots/";
    public static final int SHOTS_COUNT = 12;
    public static final String POPULAR = "popular";
    public static final String LATEST = "latest";
    static RestAdapter sRestAdapter;
    static ApiService sApiService;

    public static void resetApiService() {
        sRestAdapter = null;
        sApiService = null;
    }

    //https://dribbble.com/search?page=2&q=ui
    //<li id="screenshot-829195" class="group "> search result  829195  shot Id
    public static String search(Context context, String query, int page) {
        try {
            OkHttpClient client = getOkHttpClient(context);
            String url = "https://dribbble.com/search?page=" + page + "&q=" + URLEncoder.encode(query, "UTF-8");
            Request request = new Request.Builder().url(url)
                    .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 5.0.1; Nexus 4 Build/LRX22C)").build();
            com.squareup.okhttp.Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] downloadAco(Context context, long id) {
        try {
            OkHttpClient client = getOkHttpClient(context);
            //https://dribbble.com/shots/2206903/colors.aco
            String url = "https://dribbble.com/shots/" + id + "/colors.aco";
            Request request = new Request.Builder().url(url)
//                    .header("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 5.0.1; Nexus 4 Build/LRX22C)")
                    .build();
            com.squareup.okhttp.Response response = client.newCall(request).execute();
            return response.body().bytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Shot> searchShot(Context context, String query, String type, int page) {
        return searchShot(context, query, type, page, null);
    }

    public static final String TAG_SEARCH = "tag:";

    public static List<Shot> tagShot(Context context, String query, String type, int page) {
        //https://dribbble.com/tags/test?page=4&per_page=12&_=1439204310141
        String url = "https://dribbble.com/tags/" + query + "?utf8=%E2%9C%93&per_page=12&_=" + System.currentTimeMillis() + "&page=" + page;
        return searchShot(context, query, type, page, url);
    }

    public static List<Shot> colorShot(Context context, String query, int percent, int page) {
        //https://dribbble.com/tags/test?page=4&per_page=12&_=1439204310141
        //https://dribbble.com/colors/CE5265?percent=35
        String url = "https://dribbble.com/colors/" + query + "?percent=" + percent + "&utf8=%E2%9C%93&per_page=12&_=" + System.currentTimeMillis() + "&page=" + page;
        return searchShot(context, query, null, page, url);
    }

    public static List<Shot> searchShot(Context context, String query, String type, int page, String curl) {
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        String s = null;
        if (!TextUtils.isEmpty(type)) {
            if (type.equalsIgnoreCase(LATEST)) {
                s = "&s=latest";
            }
        }
        String url = curl == null ? ("https://dribbble.com/search?utf8=%E2%9C%93&per_page=12&_=" + System.currentTimeMillis() + "&q=" + query + "&page=" + page + (s == null ? "" : s)) : curl;
        ArrayList<Shot> shots = new ArrayList<Shot>();
        try {
            Elements elements = Jsoup.parse(new URL(url), 20000).select("li[id^=screenshot].group");
            for (Element element : elements) {
                Element shotElement = element;
                //                Log.e("shot",element.html()+"\n\n");
                Element shotImgElement = shotElement.select("div.dribbble-shot").first();
                Element link = shotImgElement.select("a.dribbble-link").first();
                final String shotUrl = link.attr("href");
                String imageUrl = link.select("div[data-src]").get(1).attr("data-src");
                String des = shotImgElement.select("span.comment").html();
                //                String title = shotImgElement.select(".dribbble-over string").html();
                //                String time = shotImgElement.select("em.timestamp").html();
                //                L.d("time "+ time);
                String title = shotImgElement.select("div[data-picture]").attr("data-alt");
                int start = shotUrl.indexOf(SHOTS) + SHOTS.length();
                long shotId = Long.parseLong(shotUrl.substring(start, shotUrl.indexOf("-")));

                Element shotToolsElement = shotImgElement.select("[class=tools group]").first();
                int likes = 0;
                try {
                    likes = Integer
                            .parseInt(shotToolsElement.select(".fav a").html().replaceAll(",", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                int comments = 0;
                try {
                    comments = Integer.parseInt(
                            shotToolsElement.select(".cmnt > span").html().replaceAll(",", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
                int views = 0;
                try {
                    views = Integer.parseInt(
                            shotToolsElement.select(".views > span").html().replaceAll(",", ""));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                //                Element shotExtrasElement = shotElement.select(".extras").first();
                //                boolean hasRebounds = Integer.parseInt(shotExtrasElement.select("a span").html().substring(0, 1)) > 0;
                Element shotUserElement = shotElement.select("span.attribution-user").first();
                Element userNameElement = shotUserElement.select("a.url").first();
                String userId = userNameElement.attr("href").substring(1);
                String userName = userNameElement.attr("title");
                String userIcon = userNameElement.select("img.photo").attr("src");
                User user = new User();
                user.username = userId;
                user.name = userName;
                user.avatarUrl = userIcon;
                Shot shot = new Shot();
                shot.id = shotId;
                shot.title = title;
                shot.likesCount = likes;
                shot.viewsCount = views;
                shot.commentsCount = comments;
                shot.description = des;
                shot.user = user;
                Image img = new Image();
                img.normal = imageUrl;
                img.hidpi = imageUrl;
                //                L.d("time "+time);
                shot.images = img;
                shot.htmlUrl = Utils.SITE_URL + shotUrl;
                shots.add(shot);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shots;
    }

    public static List<User> searchUser(Context context, String query, int page, String url) {

        ArrayList<User> shots = new ArrayList<User>();
        try {
            Element e = Jsoup.parse(new URL(url), 20000).select("ol.player-cards").first();
            Elements elements = e.select("li.player");
            for (Element element : elements) {
                Element shotElement = element.select("div.player-info").first();
                Element a = shotElement.select("h2 > a.url").first();
                String city = shotElement.select("a.locality").first().html();
                String imageUrl = a.select("div[data-src]").get(1).attr("data-src");
                String userId = a.attr("href").substring(1);
                String userName = a.attr("title");

                Element data = shotElement.select("ul.player-stats").first();
                int views = 0;
//                try {
//                    views = Integer.parseInt(
//                            data.select("li.stat-shots > a").html().replaceAll(",", ""));
//                } catch (NumberFormatException e1) {
//                    e1.printStackTrace();
//                }
                int f = 0;
//                try {
//                    f = Integer.parseInt(
//                            data.select("li.stat-followers > a").html().replaceAll(",", ""));
//                } catch (NumberFormatException e1) {
//                    e1.printStackTrace();
//                }
                User user = new User();
                user.username = userId;
                user.name = userName;
                user.avatarUrl = imageUrl;
                user.location = city;
                user.shotsCount = views;
                user.followersCount = f;

                shots.add(user);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shots;
    }

    public static List<User> searchUser(Context context, String query, int page) {
        //https://dribbble.com/search/users?page=2&q=brian
        //https://dribbble.com/tags/test?page=4&per_page=12&_=1439204310141
        try {
            query = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
        String url = "https://dribbble.com/search/users?utf8=%E2%9C%93&page=" + page + "&q=" + query;
        return searchUser(context, query, page, url);
    }

    public static <E> boolean hasNextPage(List<E> data) {
        return data != null && data.size() >= ApiService.PAGE_NUM;
    }

    public static OkHttpClient getOkHttpClient(final Context context) {
        File httpCacheDirectory = new File(context.getCacheDir(), "responses");

        Cache httpResponseCache = null;
        try {
            httpResponseCache = new Cache(httpCacheDirectory, 10 * 1024 * 1024);
        } catch (Exception e) {
            Log.e("Retrofit", "Could not create http cache", e);
        }

        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setCache(httpResponseCache);

        return okHttpClient;
    }


    //@f:off
    public static ApiService getService(final Context context) {
        String token = OAuthUtils.getAccessToken(context.getApplicationContext());
        return getService(context.getApplicationContext(), token);
    }

    public static ApiService getMuzeiService(final Context context) {
        String token = OAuthUtils.getAccessToken(context.getApplicationContext());
        return getService(context.getApplicationContext(), token, true);
    }

    public static ApiService getService(final Context context, final String token) {
        return getService(context, token, false);
    }

    public static ApiService getService(final Context context, final String token, final boolean muzei) {
        if (sApiService != null) {
            return sApiService;
        }

        if (sRestAdapter == null) {
            Gson gson = new GsonBuilder()
                    .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
//                    .registerTypeAdapter(Date.class, new DateTypeAdapter())
//                    .registerTypeAdapter(List.class, new List)
                    .create();

            RequestInterceptor requestInterceptor = new RequestInterceptor() {
                @Override
                public void intercept(RequestFacade request) {
//                    request.addHeader("Accept", "application/vnd.dribbble.v1.html+json");
//                    request.addHeader("Accept", "application/vnd.dribbble.v1.text+json");
                    request.addHeader("Authorization", "Bearer " + token);
                    request.addQueryParam("per_page", String.valueOf(muzei ? ApiService.PAGE_NUM : ApiService.PAGE_NUM));
                    //TODO how to using cache when offline
                    if (ConnUtils.isNetworkAvaliable(context)) {
                        int maxAge = 60 * 60; // read from cache for 60 minute
                        request.addHeader("Cache-Control", "public, max-age=" + maxAge);
                    } else {
                        int maxStale = 60 * 60 * 24 * 28; // tolerate 4-weeks stale
                        request.addHeader("Cache-Control", "public, only-if-cached, max-stale=" + maxStale);
                    }
                }
            };
            sRestAdapter = new RestAdapter.Builder()
                    .setClient(new OkClient(getOkHttpClient(context)))
                    .setEndpoint("https://api.dribbble.com/v1")
                    .setRequestInterceptor(requestInterceptor)
                    .setConverter(new GsonConverter(gson)).setLogLevel(
                            BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL
                                    : RestAdapter.LogLevel.NONE)

                    .build();
        }
        sApiService = sRestAdapter.create(ApiService.class);

        return sApiService;
    }
    //@f:on

    public static String responseToString(Response response) {
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String result = sb.toString();
        return result;
    }

    public static String getServerErrorMessage(RetrofitError error) {
        String msg = getErrorMessage(error.getResponse());
        if (msg == null) {
            switch (error.getKind()) {
                case NETWORK:
                    msg = "check your network!";
                    break;
                default:
                    msg = error.getMessage();
            }
        }
        return msg;
    }

    public static String getErrorMessage(Response response) {
        String text = responseToString(response);
        try {
            JSONObject object = new JSONObject(text);
            if (object.has("errors")) {
                JSONArray errors = object.getJSONArray("errors");
                if (errors.length() > 0) {
                    JSONObject error = errors.getJSONObject(0);
                    if (error.has("message")) {
                        return error.getString("message");
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("Api", "getErrorMessage: "+text );
        }
        if (response.getStatus() > 500) {
            return App.getContext().getString(R.string.check_network);
        }
        return null;
    }
}
