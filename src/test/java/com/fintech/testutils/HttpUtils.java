package com.fintech.testutils;

import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.http.HttpEntity;

public class HttpUtils {

  public static <T> T readBody(HttpEntity httpEntity, Class<T> clazz) throws IOException {
    Gson gson = new Gson();

    return gson.fromJson(readBodyAsString(httpEntity), clazz);
  }

  public static String readBodyAsString(HttpEntity httpEntity) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(httpEntity.getContent()));

    return reader.readLine();
  }

}
