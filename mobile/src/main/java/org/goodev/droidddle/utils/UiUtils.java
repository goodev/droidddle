package org.goodev.droidddle.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Base64;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.goodev.ct.CustomTabActivityHelper;
import org.goodev.ct.CustomTabsHelper;
import org.goodev.droidddle.AboutActivity;
import org.goodev.droidddle.AddToBucketActivity;
import org.goodev.droidddle.BaseActivity;
import org.goodev.droidddle.ColorSearchActivity;
import org.goodev.droidddle.CreateActivity;
import org.goodev.droidddle.DownloadedActivity;
import org.goodev.droidddle.EditCommentActivity;
import org.goodev.droidddle.MainActivity;
import org.goodev.droidddle.ProjectShotActivity;
import org.goodev.droidddle.R;
import org.goodev.droidddle.SearchActivity;
import org.goodev.droidddle.SettingsActivity;
import org.goodev.droidddle.ShotDetailsActivity;
import org.goodev.droidddle.TagActivity;
import org.goodev.droidddle.TeamActivity;
import org.goodev.droidddle.UserActivity;
import org.goodev.droidddle.UserItemsActivity;
import org.goodev.droidddle.ViewImageActivity;
import org.goodev.droidddle.WebActivity;
import org.goodev.droidddle.api.ApiFactory;
import org.goodev.droidddle.frag.ShotFragment;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.pojo.Image;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.pojo.Search;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;
import org.goodev.droidddle.widget.CircularProgressDrawable;
import org.goodev.droidddle.widget.DividerItemDecoration;
import org.goodev.droidddle.widget.GoURLSpan;
import org.goodev.droidddle.widget.OnFilterListener;
import org.goodev.droidddle.widget.OnOperationListener;
import org.goodev.droidddle.widget.TintedBitmapDrawable;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by goodev on 2014/12/22.
 */
public class UiUtils {

    //该版本时效的时间
    private static final int YEAR = 2016;

    public static final String ACTION_OPEN_SHOT = "goodev.action.OPEN_SHOT";

    public static final String ACTION_OPEN_FOLLOWING = "goodev.action.OPEN_FOLLOWING";

    public static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    public static final String PATH_SHOTS = "/shots";

    public static final String PARAM_LIST = "list";

    public static final String PARAM_SORT = "sort";

    public static final String PARAM_TIMEFRAME = "timeframe";

    public static final String PATH_SHOT = "/shots/";

    public static final String ARG_SHOT_ID = "extra_shot_id";

    public static final String ARG_SHOT = "extra_shot";

    public static final String ARG_URL = "extra_url";

    public static final String ARG_THUMB_URL = "extra_thumb_url";

    public static final String ARG_ID = "extra_id";

    public static final String ARG_NAME = "extra_name";

    public static final String KEY_USER = "extra_user";

    public static final String ARG_USER = KEY_USER;

    public static final String ARG_TEAM = "extra_team";

    public static final String ARG_SEARCH = "extra_search";

    public static final String ARG_PROJECT = "extra_project";

    public static final String ARG_TYPE = "extra_type";

    public static final String ARG_BUCKET = "extra_bucket";

    public static final String ARG_SELF = "extra_self";

    public static final String ARG_COMMENT = "extra_comment";

    public static final String ARG_CURRENT_PAGE = "extra_page";

    public static final String ARG_CURRENT_LIST_INDEX = "extra_list_index";

    public static final String ARG_DATA_LIST = "extra_data_list";

    public static final int TYPE_USER = 1;

    public static final int TYPE_TEAM = 2;

    public static final int TYPE_PROJECT = 3;

    public static final int TYPE_BUCKET = 4;

    public static final int TYPE_SHOT = 5;

    public static final String ZERO = "0";

    public static final long NO_ID = -1L;

    public static final int SMALL_WIDTH = 400;

    public static final int SMALL_HEIGHT = 300;

    public static final int BIG_WIDTH = 800;

    public static final int BIG_HEIGHT = 600;

    public static final long DELAYED_TIME = 4000;

    public static final int UNDO_BAR_TIME = 3000;

    public static final String ARG_QUERY = "extra_query";

    public static final String ARG_SHOT_SORT = "extra_shot_sort";

    public static final String ARG_SHOT_LIST = "extra_shot_list";

    public static final String ARG_SHOT_TIMEFRAME = "extra_shot_timeframe";

    /**
     * Factor applied to session color to derive the background color on panels and when
     * a session photo could not be downloaded (or while it is being downloaded)
     */
    public static final float SESSION_BG_COLOR_SCALE_FACTOR = 0.75f;

    private static final String MIME_IMAGES = "image/";

    private static final String MY_URL = "https://dribbble.com/goodev";

    private static final String PLAY_URL
            = "https://play.google.com/store/apps/details?id=org.goodev.droidddle";

    private static final int BRIGHTNESS_THRESHOLD = 130;

    private static final int TINT_COLOR = Color.parseColor("#20000000");

    private static String PKG = "b3JnLmdvb2Rldi5kcm9pZGRkbG";//U= org.goodev.droidddle

    private static String getPackageName = "Z2V0UGFja2FnZU5hbW";//U=

    private static String finishe = "ZmluaXNoZQ=";//=

    public static String getFormat(int data1, int data2, int data3) {
        int data = Math.max(data1, data2);
        data = Math.max(data, data3);
        int length = String.valueOf(data).length();
        return "%" + length + "d";
    }

    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    //TODO search activity
    public static void luanchSearch(Context activity, Search data) {
        Intent intent = new Intent(activity, SearchActivity.class);
        intent.putExtra(UiUtils.ARG_SEARCH, data);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static void launchColorSearchActivity(Context activity) {
        Intent intent = new Intent(activity, ColorSearchActivity.class);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static void launchColorSearchActivity(Context activity, String color) {
        Intent intent = new Intent(activity, ColorSearchActivity.class);
        intent.putExtra(ARG_QUERY, color);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static void launchUser(Context activity, User data) {
        Intent intent = new Intent(activity, UserActivity.class);
        intent.putExtra(UiUtils.ARG_USER, data);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static void launchUser(Activity activity, User data) {
        Intent intent = new Intent(activity, UserActivity.class);
        intent.putExtra(UiUtils.ARG_USER, data);
        activity.startActivity(intent);
    }

    public static void launchUser(Activity activity, User data, View view) {
        //new Pair<View, String>(mUserNameView, Scene.USER_NAME)
        ActivityOptionsCompat opts = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                new Pair<View, String>(view, Scene.USER_IMAGE));
        Intent intent = new Intent(activity, UserActivity.class);
        intent.putExtra(UiUtils.ARG_USER, data);
        ActivityCompat.startActivity(activity, intent, opts.toBundle());
        //        activity.startActivity(intent);
    }

    public static void launchTeam(Context context, Team data) {
        Intent intent = new Intent(context, TeamActivity.class);
        intent.putExtra(UiUtils.ARG_TEAM, data);
        context.startActivity(intent);
    }

    public static void launchShot(Activity activity, Shot shot) {
        launchShot(activity, shot, false);
    }

    public static void launchShot(Activity activity, Shot shot, boolean openComment) {
        Intent intent = new Intent(activity, ShotDetailsActivity.class);
        intent.putExtra(ShotFragment.ARG_SHOT, shot);
        intent.putExtra(UiUtils.ARG_COMMENT, openComment);
        activity.startActivity(intent);
    }

    public static void launchShot(Context activity, Shot shot) {
        Intent intent = new Intent(activity, ShotDetailsActivity.class);
        intent.putExtra(ShotFragment.ARG_SHOT, shot);
        intent.putExtra(UiUtils.ARG_COMMENT, false);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static void launchShot(Context activity, long shotId) {
        Intent intent = new Intent(activity, ShotDetailsActivity.class);
        intent.putExtra(ARG_SHOT_ID, shotId);
        intent.putExtra(UiUtils.ARG_COMMENT, false);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static void showFileDirPicker(Fragment f, int code) {
        Intent intent = new Intent(f.getActivity(), FilePickerActivity.class);
        intent.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);
        // Configure initial directory by specifying a String.
        // You could specify a String like "/storage/emulated/0/", but that can
        // dangerous. Always use Android's API calls to get paths to the SD-card or
        // internal memory.
        File dir = UiUtils.getDownloadFilePath(f.getActivity());
        String path = dir == null ? Environment.getExternalStorageDirectory().getPath() : dir.getAbsolutePath();
        intent.putExtra(FilePickerActivity.EXTRA_START_PATH, path);

        f.startActivityForResult(intent, code);
    }

    public static void launchShot(Activity activity, Shot shot, View view) {
        ActivityOptionsCompat opts = ActivityOptionsCompat
                .makeSceneTransitionAnimation((Activity) activity,
                        new Pair<View, String>(view, Scene.SHOT_IMAGE));
        Intent intent = new Intent(activity, ShotDetailsActivity.class);
        intent.putExtra(ShotFragment.ARG_SHOT, shot);
        //        intent.putExtra(UiUtils.ARG_COMMENT, openComment);
        //        activity.startActivity(intent);
        ActivityCompat.startActivity(activity, intent, opts.toBundle());
    }

    public static void launchShotImage(Activity activity, Shot shot) {
        String url = shot.images.hidpi;
        if (TextUtils.isEmpty(url)) {
            url = shot.images.normal;
        }
        launchImage(activity, url, url);
        //        Intent intent = new Intent(activity, ViewImageActivity.class);
        //        intent.putExtra(UiUtils.ARG_SHOT, shot);
        //        activity.startActivity(intent);
    }

    public static void launchShot(Activity activity, long id) {
        Intent intent = new Intent(activity, ShotDetailsActivity.class);
        intent.putExtra(ARG_SHOT_ID, id);
        activity.startActivity(intent);
    }

    public static boolean hasLollipop() {
        return Build.VERSION.SDK_INT >= 21;
    }

    public static String intToString(Integer value) {
        if (value == null) {
            return ZERO;
        }
        return String.valueOf(value.intValue());
    }

    public static boolean isImage(String mime) {
        if (TextUtils.isEmpty(mime)) {
            return false;
        }

        return mime.toLowerCase(Locale.ENGLISH).startsWith(MIME_IMAGES);
    }

    public static String getFileName(String url) {
        int index = url.lastIndexOf("/");
        return url.substring(index + 1);
    }

    public static String getFileSizeString(Long size) {
        if (size == null) {
            return "0 B";
        }
        long bytes = size.longValue();
        return getFileSizeString1(bytes);
    }

    public static String getFileSizeString1(long bytes) {
        if (bytes == 0) {
            return "0 B";
        }
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = ("KMGTPE").charAt(exp - 1) + ("i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static long getReboundSourceId(Shot shot) {
        final String url = shot.reboundSourceUrl;
        int index = url.lastIndexOf("/");
        try {
            return Long.valueOf(url.substring(index + 1));
        } catch (NumberFormatException e) {
            return NO_ID;
        }
    }

    public static void setValueOrHidden(TextView view, Integer count, int resId) {
        if (count == null || count.intValue() == 0) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            String text = view.getResources().getQuantityString(resId, count, count);
            view.setText(text);
        }
    }

    public static int getCountValue(Integer value) {
        if (value == null) {
            return 0;
        }
        return value.intValue();
    }

    public static void setValueOrHidden(TextView view, String text) {
        setValueOrHidden(view, text, false);
    }

    public static void setValueOrHidden(TextView view, String text, boolean html) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
            if (html) {
                Spanned spannable = GoURLSpan.hackURLSpan(Html.fromHtml(text));
                view.setText(spannable);
                view.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                view.setText(text);
            }
        }
    }

    public static void hiddenIfNoValue(TextView view, String text) {
        if (TextUtils.isEmpty(text)) {
            view.setVisibility(View.GONE);
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    public static DividerItemDecoration getDividerItemDecoration(Resources resources) {
        Drawable drawable = resources.getDrawable(R.drawable.abc_list_divider_mtrl_alpha);
        int color = resources.getColor(R.color.gray_background);
        drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        DividerItemDecoration divider = new DividerItemDecoration(drawable);
        int padding = (int) resources.getDimension(R.dimen.keyline_1);
        divider.setPadding(padding, padding);
        return divider;
    }

    public static void launchMap(Activity activity, String location) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri = Uri.parse("geo:0,0?q=" + location);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(activity, R.string.cannot_handle_map, Toast.LENGTH_LONG).show();
        }
    }

    public static void openUrl(Context activity, String web, CustomTabActivityHelper helper) {
        if (Build.VERSION.SDK_INT >= 16) {
        }
    }

    public static void openUrl(Context context, String web) {
        String packageName = CustomTabsHelper.getPackageNameToUse(context);
        Uri uri = Uri.parse(web);
        if (Build.VERSION.SDK_INT >= 16 && packageName != null && context instanceof Activity) {
            CustomTabActivityHelper.openCustomTab((Activity) context, uri, null);
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        String title = context.getString(R.string.open_in_browser);
        context.startActivity(Intent.createChooser(intent, title));
        Toast.makeText(context, R.string.tips_open_in_browser, Toast.LENGTH_LONG).show();
    }

    public static void launchUserShots(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_SHOT);
    }

    public static void launchUserLikes(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_LIKE);
    }

    public static void launchUserTeams(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_TEAM);
    }

    public static void launchUserBuckets(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_BUCKET);
    }

    public static void launchUserFollowings(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_FOLLOWING);
    }

    public static void launchUserFollowers(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_FOLLOWER);
    }

    public static void launchUserProjects(Activity activity, User user) {
        luanchUserItems(activity, user, UserItemsActivity.TYPE_PROJECT);
    }

    public static void luanchUserItems(Activity activity, User user, int type) {
        Intent intent = new Intent(activity, UserItemsActivity.class);
        intent.putExtra(UiUtils.ARG_USER, user);
        intent.putExtra(UiUtils.ARG_TYPE, type);
        activity.startActivity(intent);

    }

    public static void launchTeamShots(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_SHOT);
    }

    public static void launchTeamMembers(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_MEMBER);
    }

    public static void luanchTeamItems(Activity activity, Team user, int type) {
        Intent intent = new Intent(activity, UserItemsActivity.class);
        intent.putExtra(UiUtils.ARG_TEAM, user);
        intent.putExtra(UiUtils.ARG_TYPE, type);
        activity.startActivity(intent);

    }

    public static void launchTeamProjects(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_PROJECT);
    }

    public static void launchTeamFollowers(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_FOLLOWER);
    }

    public static void launchTeamFollowings(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_FOLLOWING);
    }

    public static void launchTeamBuckets(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_BUCKET);
    }

    public static void launchTeamLikes(Activity activity, Team user) {
        luanchTeamItems(activity, user, UserItemsActivity.TYPE_LIKE);
    }

    public static void launchProjectShots(Context activity, Project data) {
        Intent intent = new Intent(activity, ProjectShotActivity.class);
        intent.putExtra(UiUtils.ARG_NAME, data.name);
        intent.putExtra(UiUtils.ARG_ID, data.id);
        intent.putExtra(UiUtils.ARG_TYPE, TYPE_PROJECT);
        activity.startActivity(intent);
    }

    public static void launchBucketShots(Context activity, Bucket data) {
        Intent intent = new Intent(activity, ProjectShotActivity.class);
        intent.putExtra(UiUtils.ARG_NAME, data.name);
        intent.putExtra(UiUtils.ARG_ID, data.id);
        intent.putExtra(UiUtils.ARG_TYPE, TYPE_BUCKET);
        activity.startActivity(intent);
    }

    public static void launchBucketShots(Context activity, Bucket data, boolean self) {
        Intent intent = new Intent(activity, ProjectShotActivity.class);
        intent.putExtra(UiUtils.ARG_NAME, data.name);
        intent.putExtra(UiUtils.ARG_ID, data.id);
        intent.putExtra(UiUtils.ARG_SELF, self);
        intent.putExtra(UiUtils.ARG_TYPE, TYPE_BUCKET);
        activity.startActivity(intent);
    }

    public static ProgressDialog showProgressDialog(Context context, String message,
                                                    DialogInterface.OnCancelListener listener) {
        int color = ThemeUtil.getThemeColor(context, R.attr.navdrawerTintColor);
        return showProgressDialog(context, null, message, color, listener);
    }

    public static ProgressDialog showProgressDialog(Context context, String message) {
        return showProgressDialog(context, message, null);
    }

    public static ProgressDialog showProgressDialog(Context context, int msg) {
        return showProgressDialog(context, context.getString(msg), null);
    }

    public static ProgressDialog showProgressDialog(Context context, String message, int color,
                                                    DialogInterface.OnCancelListener listener) {
        return showProgressDialog(context, null, message, color, listener);
    }

    public static ProgressDialog showProgressDialog(Context context, String title, String message,
                                                    int color, DialogInterface.OnCancelListener listener) {
        int width = (int) context.getResources().getDimension(R.dimen.progress_width);
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setIndeterminate(true);
        dialog.setMessage(message);
        if (!TextUtils.isEmpty(title)) {
            dialog.setTitle(title);
        }
        Drawable d = new CircularProgressDrawable(color, width);
        dialog.setIndeterminateDrawable(d);
        dialog.setCanceledOnTouchOutside(false);
        if (listener != null) {
            dialog.setOnCancelListener(listener);
        }
        dialog.show();

        return dialog;
    }

    public static void dismissDialog(Dialog dialog) {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    public static void toastServerErrorMessage(Context context, RetrofitError error) {
        String msg = ApiFactory.getServerErrorMessage(error);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    public static void toastError(Context context, Throwable error) {
        if (error instanceof RetrofitError) {
            RetrofitError retrofitError = (RetrofitError) error;
            try {
                if (retrofitError.getResponse().getStatus() == 401) {
                    OAuthUtils.clearOAuthCredential(context);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            toastServerErrorMessage(context, (RetrofitError) error);
        } else {
            showToast(context, R.string.general_error);
        }
    }

    public static void showToast(Context context, int res) {
        Toast.makeText(context, res, Toast.LENGTH_SHORT).show();
    }

    public static void showToast(Context context, String res) {
        Toast.makeText(context, res, Toast.LENGTH_LONG).show();
    }

    public static void showCommentItemMenu(Context context, String[] items,
                                           DialogInterface.OnClickListener callback) {
        //@f:off
        new AlertDialog.Builder(context).setTitle(R.string.comment_options)
                .setItems(items, callback).show();
        //        new MaterialDialog.Builder(context)
        //                .title(R.string.comment_options)
        //                .items(items)
        //                .itemsCallback(callback).show();
        //@f:on
    }

    public static String[] getCommentMenu(Context context, boolean isSelf, int likesCount,
                                          boolean showWhyLike) {
        int size = 1; // Copy
        ArrayList<String> list = new ArrayList<>(7);
        list.add(context.getString(R.string.comment_options_copy));
        if (isSelf) {
            list.add(context.getString(R.string.comment_options_edit));
            list.add(context.getString(R.string.comment_options_delete));
            size += 2; // Edit and Delete
        }
        list.add(context.getString(R.string.comment_options_like));
        if (likesCount == 0) {
            size += 1; // Like
        } else if (showWhyLike) {
            size += 4; // Like Unlike and who liked , why like
            list.add(context.getString(R.string.comment_options_unlike));
            list.add(context.getString(R.string.comment_options_like_why));
            //            list.add(context.getString(R.string.comment_options_who_likes));
        } else {
            size += 3;// Like Unlike and who liked
            list.add(context.getString(R.string.comment_options_unlike));
            //            list.add(context.getString(R.string.comment_options_who_likes));
        }

        size -= 1; // not add Who like this
        String[] items = new String[size];
        items = list.toArray(items);
        return items;
    }

    public static void copyToClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
                .getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("comment", text);
        clipboard.setPrimaryClip(clip);
    }

    public static String removePTag(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("<p>|</p>", "");
    }

    public static void showWhyLikeAndUnlikeDialog(final Context context) {
        //@f:off
        new AlertDialog.Builder(context).setTitle(R.string.title_like_unlike_comment)
                .setMessage(R.string.message_like_unlike_comment)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Pref.hideFirstLikeAndUnlike(context);
                    }
                }).setCancelable(false).show();
        //        new MaterialDialog.Builder(context)
        //                .title(R.string.title_like_unlike_comment)
        //                .content(R.string.message_like_unlike_comment)
        //                .positiveText(R.string.got_it)
        //                .callback(new MaterialDialog.ButtonCallback() {
        //                    @Override
        //                    public void onPositive(MaterialDialog dialog) {
        //                        super.onPositive(dialog);
        //                        Pref.hideFirstLikeAndUnlike(context);
        //                    }
        //                })
        //                .cancelable(false)
        //                .show();
        //@f:on
    }

    public static void showShotLikeAndUnlikeDialog(final Activity context) {
        //@formatter:off
        new AlertDialog.Builder(context).setMessage(R.string.message_like_status)
                .setPositiveButton(R.string.got_it, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Pref.hideFirstLikeAndUnlike(context);
                    }
                })
                .setCancelable(false).show();
//        new MaterialDialog.Builder(context)
//                .content(R.string.message_like_status)
//                .positiveText(R.string.got_it)
//                .callback(new MaterialDialog.ButtonCallback() {
//                    @Override
//                    public void onPositive(MaterialDialog dialog) {
//                        super.onPositive(dialog);
////                        Pref.hideFirstLikeAndUnlike(context);
//                    }
//                })
//                .cancelable(false)
//                .show();
        //@formatter:on
    }

    public static void showConfirmDialog(final Context context, int msg,
                                         DialogInterface.OnClickListener callback) {
        //@f:off
        new AlertDialog.Builder(context).setMessage(msg)
                .setPositiveButton(R.string.delete, callback)
                .setNegativeButton(android.R.string.cancel, null).setCancelable(true).show();
        //        new MaterialDialog.Builder(context)
        //                .content(msg)
        //                .positiveText(R.string.delete)
        //                .negativeText(android.R.string.cancel)
        //                .callback(callback)
        //                .cancelable(true)
        //                .show();
        //@f:on
    }

    public static void editComment(Context context, long shotId, Comment comment) {
        Intent intent = new Intent(context, EditCommentActivity.class);
        intent.putExtra(UiUtils.ARG_SHOT_ID, shotId);
        intent.putExtra(UiUtils.ARG_COMMENT, comment);
        context.startActivity(intent);
    }

    public static void shareShot(Context context, Shot shot) {
        String title;
        if (shot.user == null) {
            title = context.getString(R.string.title_share_shot, shot.title);
        } else {
            title = context
                    .getString(R.string.title_share_shot_with_name, shot.title, shot.user.name);
        }
        String url = shot.htmlUrl;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TITLE, title);
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, url);
        intent.setData(Uri.parse(url));
        intent.setType("text/plain");

        String shareTitle = context.getString(R.string.share_shot_dialog_title, shot.title);
        context.startActivity(Intent.createChooser(intent, shareTitle));

    }

    public static void launchImage(Context context, String url, String thumb) {
        Intent intent = new Intent(context, ViewImageActivity.class);
        intent.putExtra(UiUtils.ARG_URL, url);
        intent.putExtra(UiUtils.ARG_THUMB_URL, thumb);
        context.startActivity(intent);
    }

    public static void launchAddToBucket(Activity activity, Shot shot) {
        Intent intent = new Intent(activity, AddToBucketActivity.class);
        intent.putExtra(UiUtils.ARG_SHOT_ID, shot.id);
        activity.startActivity(intent);
    }

    //TODO dabaodang
    public static void checkPackageName(BaseActivity activity) {
        String pkgNameMethod = new String(
                Base64.decode(getPackageName + "u".toUpperCase() + "=", Base64.DEFAULT));
        try {
            Method method = activity.getClass().getMethod(pkgNameMethod);
            Object obj = method.invoke(activity);
            String pkg = new String(Base64.decode(PKG + "u".toUpperCase() + "=", Base64.DEFAULT));
            if (!pkg.equals(obj)) {
                String finish = new String(Base64.decode(finishe + "=", Base64.DEFAULT));
                finish = finish.substring(0, finish.length() - 1);
                Method finishMethod = activity.getClass().getMethod(finish);
                //                finishMethod.invoke(activity);
                showDownloadFromPlayDialog(activity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        Date date = new Date();
//        boolean after = date.after(new Date(YEAR - 1900, 6, 29));
//        if (after) {
//            showDownloadFromPlayDialog(activity);
//        }
    }

    private static void showDownloadFromPlayDialog(BaseActivity activity) {
        //@formatter:off
        new AlertDialog.Builder(activity)
                .setTitle(R.string.download_from_play_title)
                .setMessage(R.string.download_from_play_message)
                .setPositiveButton(R.string.download, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                        UiUtils.openUrl(activity, UiUtils.PLAY_URL);
                    }
                }).setCancelable(false).show();

//        new MaterialDialog.Builder(activity).title(R.string.download_from_play_title).content(R.string.download_from_play_message).positiveText(R.string.download).callback(new MaterialDialog.Callback() {
//            @Override
//            public void onNegative(MaterialDialog materialDialog) {
//
//            }
//
//            @Override
//            public void onPositive(MaterialDialog materialDialog) {
//                activity.finish();
//                UiUtils.openUrl(activity, UiUtils.PLAY_URL);
//            }
//        }).cancelable(false).show();
        //@formatter:on
    }

    public static void openSettings(Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        activity.startActivity(intent);
    }

    public static void openAbout(Activity activity) {
        Intent intent = new Intent(activity, AboutActivity.class);
        activity.startActivity(intent);
    }

    public static void openDownloadedImages(Activity activity) {
        Intent intent = new Intent(activity, DownloadedActivity.class);
        activity.startActivity(intent);
    }

    public static String getVersion(Activity activity) {
        PackageManager pm = activity.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(activity.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0";
    }

    @SuppressLint("NewApi")
    public static void initSystemBarTint(Activity activity) {
        int color = ThemeUtil.getThemeColor(activity, R.attr.colorPrimaryDark);
        if (hasLollipop()) {
//            activity.getWindow().setNavigationBarColor(color);
            //            activity.getWindow().setStatusBarColor(color);
            //            activity.getWindow().setStatusBarColor(Color.TRANSPARENT);
            return;
        }
        int orientation = activity.getResources().getConfiguration().orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (!activity.getResources().getBoolean(R.bool.enable_system_bar)) {
                return;
            }
        }
        // create our manager instance after the content view is set
        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
        // enable status bar tint
        tintManager.setStatusBarTintEnabled(true);
        // enable navigation bar tint
        tintManager.setNavigationBarTintEnabled(false);

        // set a custom tint color for all system bars
        tintManager.setTintColor(TINT_COLOR);
        // set a custom navigation bar resource
        ColorDrawable drawable = new ColorDrawable(color);
        tintManager.setNavigationBarTintDrawable(drawable);
        // set a custom status bar drawable
        tintManager.setStatusBarTintDrawable(drawable);
    }

    public static File getDownloadFile(Context context, String url, long id) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            showToast(context, R.string.sdcard_not_available);
            return null;
        }
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            showToast(context, R.string.sdcard_not_available);
            return null;
        }
        dir = getDownloadFilePath(context);
        return new File(dir, id + "-" + getFileName(url));

    }

    public static File getDownloadFilePath(Context context) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            showToast(context, R.string.sdcard_not_available);
            return null;
        }
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (dir == null) {
            showToast(context, R.string.sdcard_not_available);
            return null;
        }
        String path = Pref.getDownloadDir(context, dir);
        dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;

    }

    public static void downloadFile(Context context, String url, long id) {
        DownloadManager mgr = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.addRequestHeader("droidddle", "1.1");
        File file = getDownloadFile(context, url, id);

        Uri destination = Uri.fromFile(file);
        if (file.exists()) {
            file.delete();
        }
        request.setDestinationUri(destination);
//         request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
//         path);
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(file.getName());
        request.setVisibleInDownloadsUi(true);

        long downId = mgr.enqueue(request);
        String tips = context
                .getString(R.string.download_tips, file.getParentFile().getAbsolutePath());
        showToast(context, tips);
    }

    @SuppressLint("NewApi")
    public static void grantPermission(Context context, Uri uri, int flag) {
        if (hasKitkat()) {
            final int takeFlags = flag & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            // Check for the freshest data.
            context.getContentResolver().takePersistableUriPermission(uri, takeFlags);
        }
    }

    private static boolean hasKitkat() {
        return Build.VERSION.SDK_INT >= 19;
    }

    public static File getTempFile(Context context) {
        File dir = context.getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, "temp_shot.png");
        return file;
    }

    public static void showChangeLog(Activity activity) {
        new AlertDialog.Builder(activity).setView(R.layout.changelog)
                .setPositiveButton(android.R.string.ok, null).show();
        //        new MaterialDialog.Builder(activity).customView(R.layout.changelog, false).positiveText(android.R.string.ok).build().show();
    }

    public static void showTranslate(Activity activity) {
        new AlertDialog.Builder(activity).setView(R.layout.translates)
                .setPositiveButton(android.R.string.ok, null).show();
        //        new MaterialDialog.Builder(activity).customView(R.layout.translates, false).positiveText(android.R.string.ok).build().show();
    }

    public static String getTagsString(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        int size = tags.size();
        for (int i = 0; i < size; i++) {
            stringBuilder.append(tags.get(i));
            if (i != size - 1) {
                stringBuilder.append(",");
            }
        }

        return stringBuilder.toString();
    }

    public static void openDroidddleLinks(Context ctx, final String url) {
        if (!Utils.hasInternet(ctx)) {
            Toast.makeText(ctx, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog dialog = showProgressDialog(ctx, R.string.opening);
        if (url.startsWith(Utils.SEARCH_URL)) {
            Search search = Utils.getSearchQuery(url);
            if (TextUtils.isEmpty(search.q)) {
                openUrl(ctx, url);
            } else {
                luanchSearch(ctx, search);
            }
            //handler search
        } else if (url.startsWith(Utils.SHOTS_URL) || url.contains(Utils.DRBL_URL)) {
            //handler shots browser
            String id = Utils.getShotId(url);
            if (TextUtils.isEmpty(id)) {
                //http://drbl.in/oRYR
                //https://dribbble.com/shots/oRxy short url
                //                Toast.makeText(ctx, R.string.parseShotUrlError, Toast.LENGTH_LONG).show();
                Observable<String> getId = Observable.create(new Observable.OnSubscribe<String>() {

                    @Override
                    public void call(Subscriber<? super String> subscriber) {
                        try {
                            String location = getRedirectLocation(url);
                            if (location != null) {
                                String id = Utils.getShotId(location);
                                if (!TextUtils.isEmpty(id)) {
                                    subscriber.onNext(id);
                                    return;
                                } else {
                                    //http://drbl.in/oRYR
                                    String again = getRedirectLocation(location);
                                    if (again != null) {
                                        id = Utils.getShotId(again);
                                        if (!TextUtils.isEmpty(id)) {
                                            subscriber.onNext(id);
                                            return;
                                        }
                                    }
                                }
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            subscriber.onError(e);
                            return;
                        } finally {

                        }
                        subscriber.onError(new Exception());
                    }
                });
                getId.map(new Func1<String, Shot>() {
                    @Override
                    public Shot call(String s) {
                        return ApiFactory.getService(ctx).getShotSync(s);
                    }
                }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe((u) -> {
                            dismissDialog(dialog);
                            launchShot(ctx, u);
                        }, (e) -> {
                            dismissDialog(dialog);
                            openUrl(ctx, url);
                        });
                //                openUrl(ctx, url);
                return;
            }
            Observable<Shot> observable = ApiFactory.getService(ctx).getShot(id);
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe((u) -> {
                        dismissDialog(dialog);
                        launchShot(ctx, u);
                    }, (e) -> {
                        dismissDialog(dialog);
                        openUrl(ctx, url);
                    });
        } else {
            String name = Utils.getUserIdOrName(url);
            if (name == null) {
                dismissDialog(dialog);
                openUrl(ctx, url);
            }
            Observable<User> observable = ApiFactory.getService(ctx).getUser(name);
            observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                    .subscribe((u) -> {
                        dismissDialog(dialog);
                        launchUser(ctx, u);
                    }, (e) -> {
                        dismissDialog(dialog);
                        openUrl(ctx, url);
                    });
        }
        //        showToast(ctx, url);
    }

    public static String getRedirectLocation(String url) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setInstanceFollowRedirects(false);
        con.connect();
        int code = con.getResponseCode();
        String location = con.getHeaderField("Location");
        con.disconnect();
        return location;
    }

    public static void launchUser(Activity activity, String name, View view) {
        if (!Utils.hasInternet(activity)) {
            Toast.makeText(activity, R.string.check_network, Toast.LENGTH_SHORT).show();
            return;
        }
        final Dialog dialog = showProgressDialog(activity, R.string.opening);

        Observable<User> observable = ApiFactory.getService(activity).getUser(name);
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe((u) -> {
                    dismissDialog(dialog);
                    launchUser(activity, u, view);
                }, (e) -> {
                    dismissDialog(dialog);
                    showToast(activity, R.string.launch_user_failed);
                });
    }

    public static void launchSearch(Activity activity) {
        Intent intent = new Intent(activity, SearchActivity.class);
        activity.startActivity(intent);
    }

    public static void launchCreateActivity(Activity activity, int type) {
        Intent intent = new Intent(activity, CreateActivity.class);
        intent.putExtra(UiUtils.ARG_TYPE, type);
        activity.startActivity(intent);
    }

    public static void launchWebView(Activity activity, String url) {
        Intent intent = new Intent(activity, WebActivity.class);
        intent.putExtra(UiUtils.ARG_URL, url);
        activity.startActivity(intent);
    }

    /**
     * Calculate whether a color is light or dark, based on a commonly known
     * brightness formula.
     *
     * @see {@literal http://en.wikipedia.org/wiki/HSV_color_space%23Lightness}
     */
    public static boolean isColorDark(int color) {
        return ((30 * Color.red(color) +
                59 * Color.green(color) +
                11 * Color.blue(color)) / 100) <= BRIGHTNESS_THRESHOLD;
    }

    public static int setColorAlpha(int color, float alpha) {
        int alpha_int = Math.min(Math.max((int) (alpha * 255.0f), 0), 255);
        return Color.argb(alpha_int, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int scaleColor(int color, float factor, boolean scaleAlpha) {
        return Color
                .argb(scaleAlpha ? (Math.round(Color.alpha(color) * factor)) : Color.alpha(color),
                        Math.round(Color.red(color) * factor),
                        Math.round(Color.green(color) * factor),
                        Math.round(Color.blue(color) * factor));
    }

    public static int scaleSessionColorToDefaultBG(int color) {
        return scaleColor(color, SESSION_BG_COLOR_SCALE_FACTOR, false);
    }

    public static <T extends Parcelable> void setupOverflowEditMenu(Activity context, View anchor,
                                                                    OnOperationListener<T> listener, final T data, final int position) {
        setupOverflowEditMenu(context, anchor, listener, R.menu.menu_edit_delete, data, position);
    }

    public static <T extends Parcelable> void setupOverflowEditMenu(Activity context, View anchor,
                                                                    OnOperationListener<T> listener, int res, final T data, final int position) {
        final PopupMenu menu = new PopupMenu(context, anchor);
        menu.inflate(res);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_edit:
                        if (listener != null) {
                            listener.update(data, position);
                        }
                        return true;
                    case R.id.action_delete:
                        if (listener != null) {
                            listener.delete(data, position);
                        }
                        return true;
                }
                return false;
            }
        });
        anchor.setOnTouchListener(menu.getDragToOpenListener());
        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });
    }

    public static void setupFilterPopupMenu(Activity context, View anchor,
                                            OnFilterListener listener, final String[] data) {
        //        final ListPopupWindow menu = new ListPopupWindow(context);
        //        ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(context, R.layout.support_simple_spinner_dropdown_item, data);
        //        menu.setAdapter(listAdapter);
        //        menu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //            @Override
        //            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //                listener.onItemClick(parent,view,position,id);
        //                menu.dismiss();
        //            }
        //        });
        final PopupMenu menu = new PopupMenu(context, anchor);
        //        menu.inflate(res);
        for (int i = 0; i < data.length; i++) {
            menu.getMenu().add(0, i, i, data[i]);
        }
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                listener.update(menuItem.getItemId());
                return true;
            }
        });
        anchor.setOnTouchListener(menu.getDragToOpenListener());
        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.show();
            }
        });
    }

    public static void setupCommentPopupTips(Activity context, final EditText anchor,
                                             OnFilterListener listener, final String[] data) {
        final PopupMenu menu = new PopupMenu(context, anchor);
        //        menu.inflate(res);
        for (int i = 0; i < data.length; i++) {
            menu.getMenu().add(0, i, i, data[i]);
        }
        menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                InputMethodManager keyboard = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(anchor, InputMethodManager.SHOW_FORCED);
            }
        });
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                listener.update(menuItem.getItemId());
                return true;
            }
        });
//        anchor.setOnTouchListener(menu.getDragToOpenListener());
        anchor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(anchor.getText().toString())) {
                    menu.show();
                }
            }
        });
    }


    public static int getAdsBannerType(Activity activity) {
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        return 1;
        //        return width >= 800 ? AppFlood.BANNER_MIDDLE : AppFlood.BANNER_SMALL;
    }

    public static boolean isGif(Image images) {
        if (images == null) {
            return false;
        }
        if (!TextUtils.isEmpty(images.hidpi)) {
            return images.hidpi.endsWith(".gif") || images.hidpi.endsWith(".GIF");
        }
        if (!TextUtils.isEmpty(images.normal)) {
            return images.normal.endsWith(".gif") || images.normal.endsWith(".GIF");
        }
        return false;
    }

    public static final String EXTRA_MENU_ID = "extra.menu.id";

    public static void openMain(Activity activity, int id) {
        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(EXTRA_MENU_ID, id);
        activity.startActivity(intent);
    }


    public static void openTagShotActivity(Activity activity, String s) {
        Intent intent = new Intent(activity, TagActivity.class);
        intent.putExtra(UiUtils.ARG_QUERY, s);
        if (!(activity instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        activity.startActivity(intent);
    }

    public static boolean checkSelfPermission(Context context, String permission) {
        int res = ContextCompat.checkSelfPermission(context, permission);
        return PackageManager.PERMISSION_GRANTED == res;
    }

    public static void requestPermissions(Context context, String[] permissions, int code) {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context, permissions, code);
        }
    }

    public static void setProfileCoverImage(ImageView view, int color, Resources resources) {
        if (color == resources.getColor(R.color.primary_darker_color)) {
            view.setImageResource(R.drawable.default_cover);
        } else if (color == Color.BLACK) {
            view.setImageResource(R.drawable.default_cover1);
        } else {
            TintedBitmapDrawable drawable = new TintedBitmapDrawable(resources, R.drawable.default_cover1, color);
            view.setImageDrawable(drawable);
        }
    }

    //@formatter:on
}
