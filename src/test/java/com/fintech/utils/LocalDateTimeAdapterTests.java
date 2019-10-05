package com.fintech.utils;

import com.google.gson.JsonSerializer;
import java.time.LocalDateTime;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class LocalDateTimeAdapterTests {

  private JsonSerializer<LocalDateTime> adapter = new LocalDateTimeAdapter();

  @Test
  public void serialize_SuccessTest() {
    LocalDateTime date = LocalDateTime.of(2019, 10, 1, 0, 0, 0);

    Assert.assertEquals("2019-10-01T00:00:00",
        adapter.serialize(date, null, null).getAsString());
  }

}
