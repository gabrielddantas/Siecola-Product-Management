package com.myorg;

import software.amazon.awscdk.*;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.events.targets.SnsTopic;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

import java.util.HashMap;
import java.util.Map;

public class ServiceStack extends Stack {

  public ServiceStack(
      final Construct scope,
      final String id,
      Cluster cluster,
      RDSStack rdsStack,
      SnsTopic productEvents) {
    this(scope, id, null, cluster, rdsStack, productEvents);
  }

  public ServiceStack(
      final Construct scope,
      final String id,
      final StackProps props,
      Cluster cluster,
      RDSStack rdsStack,
      SnsTopic productEvents) {
    super(scope, id, props);

    ApplicationLoadBalancedFargateService service =
        ApplicationLoadBalancedFargateService.Builder.create(this, "ALB-product-management")
            .serviceName("service-product-management")
            .cluster(cluster)
            .cpu(512)
            .desiredCount(2)
            .listenerPort(8080)
            .memoryLimitMiB(1024)
            .taskImageOptions(
                ApplicationLoadBalancedTaskImageOptions.builder()
                    .containerName("aws-product-management")
                    .image(
                        ContainerImage.fromRegistry(
                            "gabrielddantas/product-management-service:1.3.0"))
                    .containerPort(8080)
                    .logDriver(
                        LogDriver.awsLogs(
                            AwsLogDriverProps.builder()
                                .logGroup(
                                    LogGroup.Builder.create(this, "LogG-service-product-management")
                                        .logGroupName("LogG-service-product-management")
                                        .removalPolicy(RemovalPolicy.DESTROY)
                                        .build())
                                .streamPrefix("product-management")
                                .build()))
                    // Env variables
                    .environment(
                        createEnvVariables(
                            rdsStack.getUsername(), productEvents.getTopic().getTopicArn()))
                    .build())
            .publicLoadBalancer(true)
            .build();

    service
        .getTargetGroup()
        .configureHealthCheck(
            new HealthCheck.Builder()
                .path("/api/actuator/health")
                .port("8080")
                .healthyHttpCodes("200")
                .build());

    ScalableTaskCount scalableTaskCount =
        service
            .getService()
            .autoScaleTaskCount(EnableScalingProps.builder().minCapacity(2).maxCapacity(4).build());

    scalableTaskCount.scaleOnCpuUtilization(
        "auto-scale-service-product-management",
        CpuUtilizationScalingProps.builder()
            .targetUtilizationPercent(50)
            .scaleInCooldown(Duration.seconds(60))
            .scaleOutCooldown(Duration.seconds(60))
            .build());

    productEvents.getTopic().grantPublish(service.getTaskDefinition().getTaskRole());
  }

  private String concatDataSourceUrl(String rdsEndpoint) {
    return "jdbc:postgresql://" + rdsEndpoint + ":5432/db_aws_product_management";
  }

  private Map<String, String> createEnvVariables(String username, String topic) {
    Map<String, String> envVars = new HashMap<>();
    envVars.put("SPRING_DATASOURCE_URL", concatDataSourceUrl(Fn.importValue("RDS-endpoint")));
    envVars.put("SPRING_DATASOURCE_USERNAME", username);
    envVars.put("SPRING_DATASOURCE_PASSWORD", Fn.importValue("RDS-password"));
    envVars.put("AWS_REGION", "sa-east-1");
    envVars.put("AWS_SNS_TOPIC_PRODUCT_EVENTS_ARN", topic);

    return envVars;
  }
}
