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

    SnsStack snsStack = new SnsStack(app, "SNS-product-management");

    ServiceStack serviceStack =
        new ServiceStack(
            app,
            "ALB-product-management",
            clusterStack.getCluster(),
            rdsStack,
            snsStack.getProductEventsTopic());
    serviceStack.addDependency(clusterStack);
    serviceStack.addDependency(rdsStack);
    serviceStack.addDependency(snsStack);

    app.synth();
  }
}
