package ru.didim99.tstu.core.mp;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by didim99 on 01.03.19.
 */
public interface WebAPI {
  String URL_JOURNAL = "http://ntv.ifmo.ru/";
  String URL_RADIO = "http://media.ifmo.ru/";
  String API_LOGIN = "4707login";
  String API_PASSW = "4707pass";

  @FormUrlEncoded
  @POST("/api_get_current_song.php")
  Call<ResponseBody> getSong(@Field("login") String login, @Field("password") String pass);

  @GET("/file/journal/{id}.pdf")
  Call<ResponseBody> getJournal(@Path("id") int id);
}
