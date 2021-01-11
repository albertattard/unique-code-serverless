data "aws_caller_identity" "current" {}

resource "aws_dynamodb_table" "UniqueCodes" {
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
    Name = "Demo - Unique Codes"
    Demo = "true"
  }
}

resource "aws_iam_role" "UniqueCodesLambda" {
  name = "unique_code"

  assume_role_policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Action": "sts:AssumeRole",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Effect": "Allow",
      "Sid": ""
    }
  ]
}
EOF
}

resource "aws_lambda_function" "UniqueCodesLambda" {
  filename         = var.lambda_zip_file
  function_name    = "unique_code"
  description      = "Demo lambda function that generates a random, yet unique code of a given length"
  role             = aws_iam_role.UniqueCodesLambda.arn
  handler          = "io.micronaut.function.aws.proxy.MicronautLambdaHandler"
  source_code_hash = filebase64sha256(var.lambda_zip_file)

  runtime     = "java11"
  memory_size = 512
  timeout     = 15

  environment {
    variables = {
      NAME = "Demo - Unique Codes"
      DEMO = "true"
    }
  }

  tags = {
    Name = "Demo - Unique Codes"
    Demo = "true"
  }
}

resource "aws_iam_policy" "lab_lambda_logging" {
  name        = "unique_code_logging"
  path        = "/"
  description = "Enable the lambda function to write logs in CloudWatch"

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
      "Resource": "arn:aws:logs:eu-central-1:${data.aws_caller_identity.current.account_id}:log-group:/aws/lambda/${aws_lambda_function.UniqueCodesLambda.function_name}:*"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "lab_lambda_logging_policy_attachment" {
  role       = aws_iam_role.UniqueCodesLambda.name
  policy_arn = aws_iam_policy.lab_lambda_logging.arn
}

resource "aws_iam_policy" "UniqueCode_lambda_DynamoDB_policy" {
  name        = "unique_code_dynamodb"
  path        = "/"
  description = "Enable the lambda function to access DynamoDB"

  policy = <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "dynamoDb:PutItem"
      ],
      "Resource": "arn:aws:dynamodb:eu-central-1:${data.aws_caller_identity.current.account_id}:table/${aws_dynamodb_table.UniqueCodes.name}"
    }
  ]
}
EOF
}

resource "aws_iam_role_policy_attachment" "UniqueCode_lambda_DynamoDB_policy_attachment" {
  role       = aws_iam_role.UniqueCodesLambda.name
  policy_arn = aws_iam_policy.UniqueCode_lambda_DynamoDB_policy.arn
}
