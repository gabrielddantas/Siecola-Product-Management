package com.gabrielddantas.productManagementService.model.sns;

import com.gabrielddantas.productManagementService.model.enums.EventType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Enveloper {
  private EventType eventType;
  private String data;
}
