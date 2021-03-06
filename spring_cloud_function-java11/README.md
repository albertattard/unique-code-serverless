# Unique Code Serverless Application

A serverless application that uses AWS Lambda Functions and DynamoDB to create unique code that can be used to identify
entities within an application. The serverless application features the following technologies.

1. Java 11
1. Spring Cloud Functions + Spring Boot
1. Gradle
1. AWS Lambda Functions
1. DynamoDB
1. LocalDynamoDb (for testing)

## Conclusion

This Spring Cloud Functions application has a slow cold start. It takes about 26 seconds for the application to serve
the first request, and subsequent requests are served within 400 milliseconds. This is very expensive when compared to
other Java alternatives, such as [Micronaut](../micronaut-java11) or [Plain Java](../plain-java11).

It is always great to work with the Spring framework and its rich ecosystem, as there is an infinite amount of material
available. With that being said, I am still missing an end-to-end test where the test sends a JSON object and receives a
JSON object as response.

## Useful resources

- [https://spring.io/projects/spring-cloud-function](https://spring.io/projects/spring-cloud-function)
- [https://cloud.spring.io/spring-cloud-static/spring-cloud-function/3.0.0.M1/home.html](https://cloud.spring.io/spring-cloud-static/spring-cloud-function/3.0.0.M1/home.html)

## Commands

1. Build application

   ```console
   $ ./gradlew clean test shadowJar
   ```

1. Set the AWS profile that will be used

   Note that the lambda function has tighter access control as it only allowed access to specific resources, such as the
   DynamoDB table being used. Please refer to the [`terraform/main.tf` terraform script](terraform/main.tf) for more
   information about this.

   Set the profile to be used to deploy the lambda function

   ```console
   $ export AWS_PROFILE="albertattard-demo"
   ```

   The above profile only has the following policy attached.

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "DemoDynamoDbListAllTables",
         "Effect": "Allow",
         "Action": ["dynamodb:ListTables"],
         "Resource": "arn:aws:dynamodb:eu-central-1:000000000000:table/*"
       },
       {
         "Sid": "DemoDynamoDbFullAccess",
         "Effect": "Allow",
         "Action": ["dynamodb:*"],
         "Resource": "arn:aws:dynamodb:eu-central-1:000000000000:table/UniqueCodes"
       },
       {
         "Sid": "DemoIamFullAccessIamRole",
         "Effect": "Allow",
         "Action": ["iam:*"],
         "Resource": "arn:aws:iam::000000000000:role/DemoUniqueCodeLambdaFunction"
       },
       {
         "Sid": "DemoIamFullAccessIamPolicy",
         "Effect": "Allow",
         "Action": ["iam:*"],
         "Resource": "arn:aws:iam::000000000000:policy/DemoUniqueCodeLambdaFunctionRestrictedAccess"
       },
       {
         "Sid": "DemoLambdaFullAccessLambda",
         "Effect": "Allow",
         "Action": ["lambda:*"],
         "Resource": "arn:aws:lambda:eu-central-1:000000000000:function:unique_code"
       },
       {
         "Sid": "DemoLogsFullAccessLogs",
         "Effect": "Allow",
         "Action": ["logs:*"],
         "Resource": "arn:aws:logs:eu-central-1:000000000000:log-group:/aws/lambda/unique_code:*"
       },
       {
         "Sid": "DemoLogsRestrictiveAccessLogs",
         "Effect": "Allow",
         "Action": ["logs:DescribeLogGroups"],
         "Resource": "arn:aws:logs:eu-central-1:000000000000:log-group::log-stream:"
       }
     ]
   }
   ```

   Please note that the account id is masked `000000000000` and needs to be replaced by a valid account id.

   The policy grants admin access to the resources used by this demo. Further restrictions can be applied, but it's
   beyond the scope of this demo.

   Please note that the following policy is not required to deploy the lambda function and can be removed. It is only
   needed to test the connection to AWS by listing all DynamoDb tables.

   ```json
       {
         "Sid": "DemoDynamoDbListAllTables",
         "Effect": "Allow",
         "Action": ["dynamodb:ListTables"],
         "Resource": "arn:aws:dynamodb:eu-central-1:000000000000:table/*"
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

   Kindly note that for this command to work, the profile we are using need to be able to list the DynamoDb tables.

1. Create the infrastructure

   The following terraform commands needs to be executed from within the `terraform` directory.

   ```console
   $ cd terraform
   ```

   Initialize the environment if not already done.

   ```console
   $ terraform init
   ```

   Apply the changes

   ```console
   $ terraform apply
   ```

   The following error may appear when applying the changes for a subsequent time, due to a known problem.

   ```text
   Error: error updating DynamoDB Table (UniqueCodes) time to live: error updating DynamoDB Table (UniqueCodes) Time To Live: ValidationException: TimeToLive is already disabled
   status code: 400, request id: 9HOFPEUK893E8PM15B388LSGC3VV4KQNSO5AEMVJF66Q9ASUAAJG
   ```

   This seems to be a known issue and nothing to worry
   about ([reference](https://github.com/hashicorp/terraform-provider-aws/issues/10304)).

1. Configure the Lambda test event

   Create a _BlankRequest_ test template, if one does not already exist.

   Select the _Amazon API Gateway AWS Proxy_ (`apigateway-aws-proxy`) template and update it as shown next. No need to
   modify the `headers`.

   ```json
   {
     "body": "{}",
     "resource": "/",
     "path": "/",
     "httpMethod": "POST",
     "isBase64Encoded": false,
     "queryStringParameters": {},
     "multiValueQueryStringParameters": {},
     "pathParameters": {},
     "stageVariables": {},
     "headers": {}
   }
   ```

   ![Configure Lambda test event](assets/images/Configure-Lambda-Blank-Test-Event.png)

   Create a _CustomRequest_ test template, if one does not already exist.

   Select the _Amazon API Gateway AWS Proxy_ (`apigateway-aws-proxy`) template and update it as shown next. No need to
   modify the `headers`.

   ```json
   {
     "body": "{\"length\": 12, \"usedBy\": \"test-event-used-by\", \"reference\": \"test-event-reference\", \"description\": \"test-event-description\"}",
     "resource": "/",
     "path": "/",
     "httpMethod": "POST",
     "isBase64Encoded": false,
     "queryStringParameters": {},
     "multiValueQueryStringParameters": {},
     "pathParameters": {},
     "stageVariables": {},
     "headers": {}
   }
   ```

   ![Configure Lambda test event](assets/images/Configure-Lambda-Custom-Test-Event.png)

1. Run the Lambda test

   The first time Lambda is executed will take about 10 seconds as the Lambda function is being prepared.

   ![Successful Initial Lambda Test](assets/images/Successful-Initial-Lambda-Test.png)

   Subsequent tests will run faster.

   ![Successful Subsequent Lambda Test](assets/images/Successful-Subsequent-Lambda-Test.png)

   There can be cases where the lambda function fails to run, such as

   ```text
   Calling the invoke API action failed with this message: Lambda was unable to decrypt the environment variables because KMS access was denied. Please check the function's KMS key settings. KMS Exception: UnrecognizedClientExceptionKMS Message: The security token included in the request is invalid.
   ```

   ![Failed Lambda Test](assets/images/Failed-Lambda-Test-1.png)

   or

   ```text
   Calling the invoke API action failed with this message: The role defined for the function cannot be assumed by Lambda.
   ```

   ![Failed Lambda Test](assets/images/Failed-Lambda-Test-2.png)

   I never got to the bottom of this, but usually works when I modify the code slightly and redeploy.

1. View the data in DynamoDB

   ![UniqueCodes DynamoDb Table](assets/images/UniqueCodes-DynamoDb-Table.png)

1. Cleanup resources from AWS

   When done, it is a good idea to delete any resources from AWS that are not required any more.

   ```console
   $ terraform destroy
   ```

   Once completed, double check through the AWS console to make sure that all the resources, including the logs were
   deleted.

## Performance

| Measurement          | 1st Request | 2nd Request | 3rd Request | 4th Request | 5th Request |
| -------------------- | ----------: | ----------: | ----------: | ----------: | ----------: |
| Init duration        |   678.07 ms |           - |           - |           - |           - |
| Duration             | 25727.40 ms |   487.20 ms |   219.70 ms |   212.64 ms |   433.48 ms |
| Billed duration      | 25728.00 ms |   488.00 ms |   220.00 ms |   213.00 ms |   434.00 ms |
| Resources configured |      512 MB |      512 MB |      512 MB |      512 MB |      512 MB |
| Max memory used      |      212 MB |      213 MB |      214 MB |      214 MB |      218 MB |
