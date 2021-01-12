resource "aws_dynamodb_table" "unique_code" {
  name           = "UniqueCodes"
  billing_mode   = "PROVISIONED"
  read_capacity  = 3
  write_capacity = 3
  hash_key       = "Code"

  attribute {
    name = "Code"
    type = "S"
  }

  ttl {
    attribute_name = "TimeToExist"
    enabled        = false
  }

  tags = {
    Name = "Demo - Unique Codes Serverless Application"
    Demo = "true"
  }
}

resource "aws_iam_role" "unique_code" {
  name        = "DemoUniqueCodeLambdaFunction"
  description = "The role that is assumed by the Unique Code Lambda function"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow"
    }
  ]
}
EOF
}

resource "aws_lambda_function" "unique_code" {
  filename         = var.lambda_zip_file
  function_name    = var.lambda_function_name
  description      = "Demo lambda function that generates a random, yet unique code of a given length"
  role             = aws_iam_role.unique_code.arn
  handler          = "demo.albertattard.uniquecode.UniqueCodeController::handleRequest"
  source_code_hash = filebase64sha256(var.lambda_zip_file)

  runtime     = var.lambda_runtime
  memory_size = var.lambda_memory_size
  timeout     = var.lambda_timeout

  environment {
    variables = {
      NAME = "Demo - Unique Codes Serverless Application"
      DEMO = "true"
    }
  }

  tags = {
    Name = "Demo - Unique Codes Serverless Application"
    Demo = "true"
  }
}

resource "aws_cloudwatch_log_group" "unique_code" {
  name              = "/aws/lambda/${var.lambda_function_name}"
  retention_in_days = 1

  tags = {
    Name = "Demo - Unique Codes Serverless Application"
    Demo = "true"
  }
}

resource "aws_iam_policy" "unique_code" {
  name        = "DemoUniqueCodeLambdaFunctionRestrictedAccess"
  path        = "/"
  description = "Enable the Unique Code lambda function to write logs in CloudWatch and put items to DynamoDB"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "logs:CreateLogGroup",
        "logs:CreateLogStream",
        "logs:PutLogEvents"
      ],
      "Resource": "${aws_cloudwatch_log_group.unique_code.arn}:*"
    },
    {
      "Effect": "Allow",
      "Action": [
        "dynamodb:PutItem"
      ],
      "Resource": "${aws_dynamodb_table.unique_code.arn}"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "unique_code" {
  role       = aws_iam_role.unique_code.name
  policy_arn = aws_iam_policy.unique_code.arn
}
