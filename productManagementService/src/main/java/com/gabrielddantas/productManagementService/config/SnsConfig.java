package com.gabrielddantas.productManagementService.config;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.Topic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!local")
public class SnsConfig {

  @Value("${aws.region}")
  private String awsRegion;

  @Value("${aws.sns.topic.product.events.arn}")
  private String arn;

  @Bean
  public AmazonSNS getSnsClient() {
    return AmazonSNSClientBuilder.standard()
        .withRegion(awsRegion)
        .withCredentials(new DefaultAWSCredentialsProviderChain())
        .build();
  }

  @Bean(name = "productEventsTopic")
  public Topic getTopic() {
    return new Topic().withTopicArn(arn);
  }
}