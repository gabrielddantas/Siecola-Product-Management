package com.gabrielddantas.productManagementService.service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.Topic;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gabrielddantas.productManagementService.entity.Product;
import com.gabrielddantas.productManagementService.model.enums.EventType;
import com.gabrielddantas.productManagementService.model.sns.Enveloper;
import com.gabrielddantas.productManagementService.model.sns.ProductEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductPublisherService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProductPublisherService.class);
  private final AmazonSNS snsClient;
  private final Topic topic;
  private final ObjectMapper objectMapper;

  public ProductPublisherService(
      AmazonSNS snsClient,
      @Qualifier("productEventsTopic") Topic topic,
      ObjectMapper objectMapper) {
    this.snsClient = snsClient;
    this.topic = topic;
    this.objectMapper = objectMapper;
  }

  public void publishProductEvent(Product product, EventType eventType, String username) {
    ProductEvent productEvent =
        ProductEvent.builder()
            .productId(product.getId())
            .code(product.getCode())
            .username(username)
            .build();

    try {
      Enveloper enveloper =
          Enveloper.builder()
              .eventType(eventType)
              .data(objectMapper.writeValueAsString(productEvent))
              .build();

      snsClient.publish(topic.getTopicArn(), objectMapper.writeValueAsString(enveloper));
    } catch (JsonProcessingException e) {
      LOGGER.error("Failed to create a product event message: {}", e.getMessage());
    }
  }
}
