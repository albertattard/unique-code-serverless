# Unique Code Serverless Application

A serverless application that uses AWS Lambda Functions and DynamoDB to create unique code that can be used to identify entities within an application. The serverless application features the following technologies.

1. Java 11
1. Gradle
1. Micronaut Framework
1. GraalVM (coming soon)
1. AWS Lambda Functions
1. AWS API Gateway (coming soon)
1. DynamoDB
1. LocalDynamoDb (for testing)

Useful resources

- [https://guides.micronaut.io/mn-application-aws-lambda-graalvm/guide/index.html](https://guides.micronaut.io/mn-application-aws-lambda-graalvm/guide/index.html)
- [https://byegor.github.io/2020/04/10/micronaut-dynamodb-async.html](https://byegor.github.io/2020/04/10/micronaut-dynamodb-async.html)

## Commands

1. Build application

   ```console
   $ ./gradlew clean test shadowJar
   ```

1. Set the AWS profile that will be used

   ```console
   $ export AWS_PROFILE="albertattard-demo"
   ```

   The above profile has teh following policy

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "DemoDynamoDbFullAccess",
         "Effect": "Allow",
         "Action": "dynamodb:*",
         "Resource": "*"
       }
     ]
   }
   ```

1. Verify access to AWS console

   List all DynamoDB Tables

   ```console
   $ aws dynamodb list-tables
   ```

   I have no tables available

   ```json
   {
     "TableNames": []
   }
   ```

1. Create the infrastructure

   Initialize the environment if not already done.

   ```console
   $ terraform init
   ```

   Apply the changes

   ```console
   $ terraform fmt -recursive && terraform apply
   ```

   The following error may appear when applying the changes again.

   ```text
   Error: error updating DynamoDB Table (UniqueCodes) time to live: error updating DynamoDB Table (UniqueCodes) Time To Live: ValidationException: TimeToLive is already disabled
   status code: 400, request id: 9HOFPEUK893E8PM15B388LSGC3VV4KQNSO5AEMVJF66Q9ASUAAJG
   ```

   This seems to be a known issue and nothing to worry about ([reference](https://github.com/hashicorp/terraform-provider-aws/issues/10304)).

1. Test Lambda

   ```json
   {
     "body": {},
     "resource": "/",
     "path": "/",
     "httpMethod": "POST",
     "isBase64Encoded": false,
     "queryStringParameters": {},
     "multiValueQueryStringParameters": {},
     "pathParameters": {},
     "stageVariables": {},
     "headers": {
       "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
       "Accept-Encoding": "gzip, deflate, sdch",
       "Accept-Language": "en-US,en;q=0.8",
       "Cache-Control": "max-age=0",
       "CloudFront-Forwarded-Proto": "https",
       "CloudFront-Is-Desktop-Viewer": "true",
       "CloudFront-Is-Mobile-Viewer": "false",
       "CloudFront-Is-SmartTV-Viewer": "false",
       "CloudFront-Is-Tablet-Viewer": "false",
       "CloudFront-Viewer-Country": "US",
       "Host": "1234567890.execute-api.eu-central-1.amazonaws.com",
       "Upgrade-Insecure-Requests": "1",
       "User-Agent": "Custom User Agent String",
       "Via": "1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)",
       "X-Amz-Cf-Id": "cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA==",
       "X-Forwarded-For": "127.0.0.1, 127.0.0.2",
       "X-Forwarded-Port": "443",
       "X-Forwarded-Proto": "https"
     },
     "multiValueHeaders": {
       "Accept": [
         "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
       ],
       "Accept-Encoding": ["gzip, deflate, sdch"],
       "Accept-Language": ["en-US,en;q=0.8"],
       "Cache-Control": ["max-age=0"],
       "CloudFront-Forwarded-Proto": ["https"],
       "CloudFront-Is-Desktop-Viewer": ["true"],
       "CloudFront-Is-Mobile-Viewer": ["false"],
       "CloudFront-Is-SmartTV-Viewer": ["false"],
       "CloudFront-Is-Tablet-Viewer": ["false"],
       "CloudFront-Viewer-Country": ["US"],
       "Host": ["0123456789.execute-api.eu-central-1.amazonaws.com"],
       "Upgrade-Insecure-Requests": ["1"],
       "User-Agent": ["Custom User Agent String"],
       "Via": [
         "1.1 08f323deadbeefa7af34d5feb414ce27.cloudfront.net (CloudFront)"
       ],
       "X-Amz-Cf-Id": [
         "cDehVQoZnx43VYQb9j2-nvCh-9z396Uhbp027Y2JvkCPNLmGJHqlaA=="
       ],
       "X-Forwarded-For": ["127.0.0.1, 127.0.0.2"],
       "X-Forwarded-Port": ["443"],
       "X-Forwarded-Proto": ["https"]
     },
     "requestContext": {
       "accountId": "123456789012",
       "resourceId": "123456",
       "stage": "prod",
       "requestId": "c6af9ac6-7b61-11e6-9a41-93e8deadbeef",
       "requestTime": "09/Apr/2015:12:34:56 +0000",
       "requestTimeEpoch": 1428582896000,
       "identity": {
         "cognitoIdentityPoolId": null,
         "accountId": null,
         "cognitoIdentityId": null,
         "caller": null,
         "accessKey": null,
         "sourceIp": "127.0.0.1",
         "cognitoAuthenticationType": null,
         "cognitoAuthenticationProvider": null,
         "userArn": null,
         "userAgent": "Custom User Agent String",
         "user": null
       },
       "path": "/prod/path/to/resource",
       "resourcePath": "/{proxy+}",
       "httpMethod": "POST",
       "apiId": "1234567890",
       "protocol": "HTTP/1.1"
     }
   }
   ```

   ```text
   Calling the invoke API action failed with this message: Lambda was unable to decrypt the environment variables because KMS access was denied. Please check the function's KMS key settings. KMS Exception: UnrecognizedClientExceptionKMS Message: The security token included in the request is invalid.
   ```
