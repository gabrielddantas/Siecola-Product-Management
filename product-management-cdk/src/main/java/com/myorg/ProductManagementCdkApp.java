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

    RDSStack rdsStack = new RDSStack(app, "RDS-product-management", vpcStack.getVpc());
    rdsStack.addDependency(vpcStack);

    ServiceStack serviceStack =
        new ServiceStack(app, "ALB-product-management", clusterStack.getCluster(), rdsStack);
    serviceStack.addDependency(clusterStack);
    serviceStack.addDependency(rdsStack);

    app.synth();
  }
}
