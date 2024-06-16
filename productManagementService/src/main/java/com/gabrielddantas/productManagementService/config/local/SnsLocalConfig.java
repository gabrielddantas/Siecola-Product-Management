package com.gabrielddantas.productManagementService.config.local;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("local")
public class SnsLocalConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(SnsLocalConfig.class);
  private final String arn;
  private final AmazonSNS snsClient;

  public SnsLocalConfig() {
    this.snsClient =
        AmazonSNSClient.builder()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(
                    "http://localhost:4566", Regions.SA_EAST_1.getName()))
            .build();

    CreateTopicRequest createTopicRequest = new CreateTopicRequest("product-events");
    this.arn = this.snsClient.createTopic(createTopicRequest).getTopicArn();

    LOGGER.info("Sns topic created: {}", this.arn);
  }

  @Bean
  public AmazonSNS getSnsClient() {
    return this.snsClient;
  }

  @Bean(name = "productEventsTopic")
  public Topic getTopic() {
    return new Topic().withTopicArn(this.arn);
  }
}
