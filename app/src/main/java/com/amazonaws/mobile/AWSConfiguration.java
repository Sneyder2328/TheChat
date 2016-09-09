//
// Copyright 2016 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//
// Source code generated from template: aws-my-sample-app-android v0.8
//
package com.amazonaws.mobile;

import com.amazonaws.regions.Regions;

/**
 * This class defines constants for the developer's resource
 * identifiers and API keys. This configuration should not
 * be shared or posted to any public source code repository.
 */
public class AWSConfiguration {

    // AWS MobileHub user agent string

    public static final String AWS_MOBILEHUB_USER_AGENT = "MobileHub 7df63bf9-0727-4be4-8571-5d36a1ceadcf aws-my-sample-app-android-v0.9";
    // AMAZON COGNITO
    public static final Regions AMAZON_COGNITO_REGION = Regions.fromName("us-east-1");
    public static final String  AMAZON_COGNITO_IDENTITY_POOL_ID = "us-east-1:a4132ec0-abc2-4cd5-8c24-0dbd58d3ee8c";
    public static final Regions AMAZON_DYNAMODB_REGION = Regions.fromName("us-east-1");

    public static final String AMAZON_S3_USER_FILES_BUCKET = "thechat-userfiles-mobilehub-1444704093";
    // S3 BUCKET REGION
    public static final Regions AMAZON_S3_USER_FILES_BUCKET_REGION = Regions.fromName("us-east-1");
}
