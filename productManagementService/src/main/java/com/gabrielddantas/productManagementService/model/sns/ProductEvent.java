package com.gabrielddantas.productManagementService.model.sns;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductEvent {
  private Long productId;
  private String code;
  private String username;
}
