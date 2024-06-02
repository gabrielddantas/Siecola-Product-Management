package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.StackProps;

import java.util.Arrays;

public class ProductManagementCdkApp {
  public static void main(final String[] args) {
    App app = new App();

    VpcStack vpcStack = new VpcStack(app, "vpc-product-management");

    ClusterStack clusterStack =
        new ClusterStack(app, "cluster-product-management", vpcStack.getVpc());
    clusterStack.addDependency(vpcStack);

    ServiceStack serviceStack =
        new ServiceStack(app, "ALB-product-management", clusterStack.getCluster());
    serviceStack.addDependency(clusterStack);

    app.synth();
  }
}
