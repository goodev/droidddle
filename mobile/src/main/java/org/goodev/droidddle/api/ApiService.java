package org.goodev.droidddle.api;

import org.goodev.droidddle.pojo.Attachment;
import org.goodev.droidddle.pojo.Bucket;
import org.goodev.droidddle.pojo.Comment;
import org.goodev.droidddle.pojo.Follower;
import org.goodev.droidddle.pojo.Following;
import org.goodev.droidddle.pojo.Like;
import org.goodev.droidddle.pojo.LikedShot;
import org.goodev.droidddle.pojo.Project;
import org.goodev.droidddle.pojo.Shot;
import org.goodev.droidddle.pojo.Team;
import org.goodev.droidddle.pojo.User;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Query;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedString;
import rx.Observable;

public interface ApiService {
    public static final int PAGE_NUM = 12;
    public static final int MUZEI_PAGE_NUM = 3;
    public static final long MAX_ATTACHMENT_LENGTH = 1024 * 1024 * 10;

    //https://dribbble.com/search?page=2&q=ui
    @GET("/search")
    Observable<Response> search();

    //https://dribbble.com/shots/2206903/colors.aco
    @GET("/shots/{id}/colors.aco")
    Observable<Response> downloadAco(@Path("id") String id);

    @GET("/user")
    Observable<User> getOAuthUser();

    @GET("/users/{user}")
    Observable<User> getUser(@Path("user") String idOrName);

    //TODO not this api now.
    @GET("/teams/{user}")
    Observable<Team> getTeam(@Path("user") String idOrName);

    @PUT("/users/{user}/follow")
    Observable<Response> followUser(@Path("user") String idOrName);

    @DELETE("/users/{user}/follow")
    Observable<Response> unfollowUser(@Path("user") String idOrName);

    /**
     * Status: 422 Unprocessable Entity
     * {
     * "message": "Validation failed.",
     * "errors": [
     * {
     * "attribute": "base",
     * "message": "You cannot follow yourself."
     * }
     * ]
     * }
     *
     * @param idOrName
     * @return
     */
    @GET("/user/following/{user}")
    Observable<Response> checkFollowingUser(@Path("user") String idOrName);

    @GET("/user/following/shots")
    Observable<List<Shot>> getUserFollowingShots(@Query("page") int page);

    @GET("/user/following/shots")
    List<Shot> getUserFollowingShotsSync(@Query("page") int page);

    /**
     * @param list      animated,attachments,debuts,playoffs,rebounds,teams; Default: Results of any type.
     * @param sort      comments,recent,views; Default: Results are sorted by popularity.
     * @param timeframe week,month,year,ever ; Default: Results from now. Note that the value is ignored when sorting with recent.
     * @return
     */
    @GET("/shots")
    Observable<List<Shot>> getShots(@Query("list") String list, @Query("sort") String sort, @Query("timeframe") String timeframe, @Query("page") int page);

    @GET("/shots")
    List<Shot> getShotsSync(@Query("list") String list, @Query("sort") String sort, @Query("timeframe") String timeframe, @Query("page") int page);

    @GET("/buckets/{id}/shots")
    List<Shot> getBucketShotsSync(@Path("id") long id, @Query("page") int page);

    @GET("/shots/{id}")
    Observable<Shot> getShot(@Path("id") long id);

    @GET("/shots/{id}")
    Observable<Shot> getShot(@Path("id") String id);

    @GET("/shots/{id}")
    Shot getShotSync(@Path("id") String id);

    @POST("/shots/{id}/like")
    Observable<Response> likeShot(@Path("id") long id);

    @DELETE("/shots/{id}/like")
    Observable<Response> unlikeShot(@Path("id") long id);

    @GET("/shots/{id}/rebounds")
    Observable<List<Shot>> getShotRebounds(@Path("id") long id, @Query("page") int page);

    @GET("/shots/{id}/comments")
    Observable<List<Comment>> getShotComments(@Path("id") long id, @Query("page") int page);

    @FormUrlEncoded
    @POST("/shots/{id}/comments")
    Observable<Comment> postShotComments(@Path("id") long id, @Field("body") String body);

    @FormUrlEncoded
    @POST("/buckets")
    Observable<Bucket> postBucket(@Field("name") String name, @Field("description") String description);

    @FormUrlEncoded
    @PUT("/buckets/{id}")
    Observable<Bucket> editBucket(@Path("id") long id, @Field("name") String name, @Field("description") String description);

    @DELETE("/buckets/{id}")
    Observable<Response> deleteBucket(@Path("id") long id);

    @FormUrlEncoded
    @PUT("/buckets/{id}/shots")
    Observable<Response> addShotToBucket(@Path("id") long id, @Field("shot_id") long shot_id);

    @DELETE("/buckets/{id}/shots")
    Observable<Response> deleteShotFromBucket(@Path("id") long id, @Query("shot_id") long shot_id);

    @FormUrlEncoded
    @PUT("/shots/{id}/comments/{cid}")
    Observable<Comment> editShotComments(@Path("id") long id, @Path("cid") long cid, @Field("body") String body);

    @POST("/shots/{id}/comments/{cid}/like")
    Observable<Like> postLikeShotComments(@Path("id") long id, @Path("cid") long cid);

    @DELETE("/shots/{id}/comments/{cid}/like")
    Observable<Response> postUnlikeShotComments(@Path("id") long id, @Path("cid") long cid);

    @DELETE("/shots/{id}")
    Observable<Response> deleteShot(@Path("id") long id);

    @Multipart
    @POST("/shots/{id}/attachments")
    Observable<Response> createShotAttachments(@Path("id") long id, @Part("file") TypedFile file);

    @GET("/shots/{id}/attachments/{aid}")
    Observable<Attachment> getAttachments(@Path("id") long id, @Path("aid") long aid);

    @DELETE("/shots/{id}/attachments/{aid}")
    Observable<Response> deleteAttachments(@Path("id") long id, @Path("aid") long aid);

    @Multipart
    @POST("/shots")
    Observable<Response> createShot(@Part("image") TypedFile image, @Part("title") TypedString title, @Part("description") TypedString description);

    @Multipart
    @POST("/shots")
    Observable<Response> createShot(@Part("image") TypedFile image, @Part("title") TypedString title, @Part("description") TypedString description, @Part("tags") TypedString tags);

    @FormUrlEncoded
    @PUT("/shots/{id}")
    Observable<Shot> updateShot(@Path("id") long id, @Field("title") String title, @Field("description") String description, @Field("tags") String tags);

    @DELETE("/shots/{id}/comments/{cid}")
    Observable<Response> deleteShotComments(@Path("id") long id, @Path("cid") long cid);

    @GET("/shots/{id}/projects")
    Observable<List<Project>> getShotProjects(@Path("id") long id, @Query("page") int page);

    @GET("/shots/{id}/likes")
    Observable<List<Like>> getShotLikes(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/likes")
    Observable<List<LikedShot>> getUserLikedShot(@Path("id") long id, @Query("page") int page);

    @GET("/shots/{id}/buckets")
    Observable<List<Bucket>> getShotBuckets(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/shots")
    Observable<List<Shot>> getUserShots(@Path("id") long id, @Query("page") int page);

    @GET("/projects/{id}/shots")
    Observable<List<Shot>> getProjectShots(@Path("id") long id, @Query("page") int page);

    @GET("/buckets/{id}/shots")
    Observable<List<Shot>> getBucketShots(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/buckets")
    Observable<List<Bucket>> getUserBuckets(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/projects")
    Observable<List<Project>> getUserProjects(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/teams")
    Observable<List<Team>> getUserTeams(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/followers")
    Observable<List<Follower>> getUserFollowers(@Path("id") long id, @Query("page") int page);

    @GET("/users/{id}/following")
    Observable<List<Following>> getUserFollowings(@Path("id") long id, @Query("page") int page);

    @GET("/shots/{id}/attachments")
    Observable<List<Attachment>> getShotAttachments(@Path("id") long id, @Query("page") int page);

    @GET("/teams/{id}/members")
    Observable<List<User>> getTeamMembers(@Path("id") long id, @Query("page") int page);

    @GET("/teams/{id}/shots")
    Observable<List<Shot>> getTeamShots(@Path("id") long id, @Query("page") int page);
}
