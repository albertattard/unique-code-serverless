variable "lambda_function_name" {
  description = "The lambda function name"
  type        = string
  default     = "unique_code"
}

variable "lambda_zip_file" {
  description = "The application JAR file"
  type        = string
  default     = "../build/libs/unique-code-serverless-all.jar"
}
