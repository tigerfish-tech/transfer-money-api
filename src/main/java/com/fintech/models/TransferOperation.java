package com.fintech.models;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferOperation {

  @SerializedName("from")
  private String accountFrom;
  @SerializedName("to")
  private String accountTo;
  @SerializedName("amount")
  private BigDecimal amount;

}
