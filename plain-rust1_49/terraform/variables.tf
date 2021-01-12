variable "lambda_function_name" {
  description = "The lambda function name"
  type        = string
  default     = "unique_code"
}

variable "lambda_handler" {
  description = "The lambda function handler"
  type        = string
  default     = "bootstrap"
}

variable "lambda_runtime" {
  description = "The lambda runtime environment"
  type        = string
  default     = "provided"
}

variable "lambda_memory_size" {
  description = "The lambda function memory size in MB (which also affects the CPU type)"
  type        = number
  default     = 512
}

variable "lambda_timeout" {
  description = "The lambda function timeout in seconds"
  type        = number
  default     = 3
}

variable "lambda_zip_file" {
  description = "The application JAR file"
  type        = string
  default     = "../target/x86_64-unknown-linux-musl/release/bootstrap.zip"
}
