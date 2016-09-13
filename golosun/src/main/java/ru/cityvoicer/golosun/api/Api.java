package ru.cityvoicer.golosun.api;

import retrofit.Call;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

public interface Api {
    @POST("/api/web/users/create")
    Call<NetProfileResponse> createUser(@Query("expand") String expand);

    @FormUrlEncoded
    @POST("/api/web/users/update")
    Call<NetProfileResponse> updateProfile(@Header("Authorization") String authorization, @Query("expand") String expand, @Field("age") Integer age, @Field("sex") String sex, @Field("push_token") String push_token, @Field("ap_mac_address") String ap_mac_address);

    @GET("/api/web/users/my")
    Call<NetProfileResponse> getProfile(@Header("Authorization") String authorization, @Query("expand") String expand);

    @GET("/api/web/results/my")
    Call<NetAdListResponse> getAdList(@Header("Authorization") String authorization, @Query("per-page") int per_page, @Query("page") int page);

    @FormUrlEncoded
    @POST("/api/web/responses/create")
    Call<NetResponseBase> adVote(@Header("Authorization") String authorization,  @Field("id_results") String id_results, @Field("comment") String comment, @Field("type_response") int type_response, @Field("id_place") String id_place);

    @FormUrlEncoded
    @POST("/api/web/money-outputs/create")
    Call<NetMoneyToPhoneResponse> moneyToPhone(@Header("Authorization") String authorization, @Field("sum") int sum, @Field("phone") String phone);

    @FormUrlEncoded
    @POST("/test/api.php")
    Call<NetGetCardResponse> getCard(@Field("secret") String secret, @Field("user_id") String userId, @Field("type") String type);

    @FormUrlEncoded
    @POST("/test/api.php")
    Call<NetResponseBase> sendCardUsedNotification(@Field("secret") String secret, @Field("user_id") String userId, @Field("type") String type, @Field("latitude") Double latitude, @Field("longitude") Double longitude);
}
