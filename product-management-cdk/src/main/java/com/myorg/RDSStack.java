package com.myorg;

import software.amazon.awscdk.*;
// import software.amazon.awscdk.Duration;
// import software.amazon.awscdk.services.sqs.Queue;
import software.amazon.awscdk.services.ec2.*;
import software.amazon.awscdk.services.ec2.InstanceType;
import software.amazon.awscdk.services.rds.*;
import software.constructs.Construct;

import java.util.Collections;

public class RDSStack extends Stack {
  public RDSStack(final Construct scope, final String id, Vpc vpc) {
    this(scope, id, null, vpc);
  }

  public RDSStack(final Construct scope, final String id, final StackProps props, Vpc vpc) {
    super(scope, id, props);

    CfnParameter databasePassoword =
        CfnParameter.Builder.create(this, "databasePassword")
            .type("String")
            .description("The RDS instance password")
            .build();

    ISecurityGroup iSecurityGroup =
        SecurityGroup.fromSecurityGroupId(this, id, vpc.getVpcDefaultSecurityGroup());
    iSecurityGroup.addIngressRule(Peer.anyIpv4(), Port.tcp(5432));

    DatabaseInstance databaseInstance =
        DatabaseInstance.Builder.create(this, "postgres-product-management")
            .instanceIdentifier("postgres-product-management")
            .engine(
                DatabaseInstanceEngine.postgres(
                    PostgresInstanceEngineProps.builder()
                        .version(PostgresEngineVersion.VER_15)
                        .build()))
            .vpc(vpc)
            .databaseName("db_aws_product_management")
            .credentials(
                Credentials.fromUsername(
                    getUsername(),
                    CredentialsFromUsernameOptions.builder()
                        .password(SecretValue.unsafePlainText(databasePassoword.getValueAsString()))
                        .build()))
            .instanceType(InstanceType.of(InstanceClass.BURSTABLE3, InstanceSize.MICRO))
            .multiAz(false)
            .allocatedStorage(10)
            .securityGroups(Collections.singletonList(iSecurityGroup))
            .vpcSubnets(SubnetSelection.builder().subnets(vpc.getPrivateSubnets()).build())
            .build();

    CfnOutput.Builder.create(this, "RDS-endpoint")
        .exportName("RDS-endpoint")
        .value(databaseInstance.getDbInstanceEndpointAddress())
        .build();

    CfnOutput.Builder.create(this, "RDS-password")
        .exportName("RDS-password")
        .value(databasePassoword.getValueAsString())
        .build();
  }

  public String getUsername() {
    return "postgres";
  }
}
