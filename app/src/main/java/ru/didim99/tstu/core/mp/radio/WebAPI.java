package ru.didim99.tstu.core.mp.radio;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by didim99 on 01.03.19.
 */
public interface WebAPI {
  String URL_BASE = "http://media.ifmo.ru/";
  String API_LOGIN = "4707login";
  String API_PASSW = "4707pass";

  @FormUrlEncoded
  @POST("/api_get_current_song.php")
  Call<ResponseBody> getSong(@Field("login") String login, @Field("password") String pass);
}
