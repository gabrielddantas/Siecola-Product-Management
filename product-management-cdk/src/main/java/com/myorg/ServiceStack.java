package com.myorg;

import software.amazon.awscdk.Duration;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.services.applicationautoscaling.EnableScalingProps;
import software.amazon.awscdk.services.ecs.*;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedFargateService;
import software.amazon.awscdk.services.ecs.patterns.ApplicationLoadBalancedTaskImageOptions;
import software.amazon.awscdk.services.elasticloadbalancingv2.HealthCheck;
import software.amazon.awscdk.services.logs.LogGroup;
import software.constructs.Construct;

public class ServiceStack extends Stack {
  public ServiceStack(final Construct scope, final String id, Cluster cluster) {
    this(scope, id, null, cluster);
  }

  public ServiceStack(
      final Construct scope, final String id, final StackProps props, Cluster cluster) {
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
                            "gabrielddantas/product-management-service:1.1.0"))
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
  }
}
