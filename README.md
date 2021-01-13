# Unique Code Serverless Application

A serverless application that uses AWS Lambda Functions and DynamoDB to create unique code that can be used to identify entities within an application.

The scope of the project is to create the same serverless application using different technologies and compare them. Following is a list of technologies used (or will be using).

1. [Java 11 (no frameworks)](plain-java11)
1. [Micronaut with Java 11](micronaut-java11)
1. Micronaut with GraalVM (coming soon)
1. Quarkus with Java 11 (coming soon)
1. Quarkus with GraalVM (coming soon)
1. Spring Boot with Java 11 (coming soon)
1. Spring Boot with GraalVM (coming soon)
1. [Rust 1.49 (no frameworks)](plain-rust1_49)

A summary will be included here, Once I have a better picture of how these technologies compare, when it comes to serverless (AWS Lambda and DynamoDB).

| Technology                                  |    Size | Memory Allocated | Memory Used | First Time | Best Time |
| ------------------------------------------- | ------: | ---------------: | ----------: | ---------: | --------: |
| [Java 11 (no frameworks)](plain-java11)     | 11.2 MB |           512 MB |      149 MB |    5817 ms |     69 ms |
| [Micronaut with Java 11](micronaut-java11)  | 18.4 MB |           512 MB |      212 MB |    5817 ms |     88 ms |
| Micronaut with GraalVM (coming soon)        |         |                  |             |            |           |
| Quarkus with Java 11 (coming soon)          |         |                  |             |            |           |
| Quarkus with GraalVM (coming soon)          |         |                  |             |            |           |
| Spring Boot with Java 11 (coming soon)      |         |                  |             |            |           |
| Spring Boot with GraalVM (coming soon)      |         |                  |             |            |           |
| [Rust 1.49 (no frameworks)](plain-rust1_49) |  4.6 MB |           512 MB |       37 MB |     219 ms |     48 ms |
