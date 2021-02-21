package main

import (
	"context"
	"time"

	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
	"github.com/aws/aws-sdk-go/service/dynamodb/dynamodbattribute"
)

func generateRandomCode(request CreateUniqueCodeRequest, randomCode RandomCode) (UniqueCode, error) {
	var response UniqueCode

	request = request.withDefaults()
	code, err := randomCode.Generate(request.Length)
	if err != nil {
		return response, err
	}

	sess := session.Must(session.NewSessionWithOptions(session.Options{SharedConfigState: session.SharedConfigEnable}))
	conn := dynamodb.New(sess)

	item := Item{
		Code:      code,
		CreatedOn: CreatedOn(),
	}
	attributeValue, err := dynamodbattribute.MarshalMap(item)
	if err != nil {
		return response, err
	}

	// Create item in table UniqueCodes
	input := &dynamodb.PutItemInput{
		Item:      attributeValue,
		TableName: aws.String("UniqueCodes"),
	}

	_, err = conn.PutItem(input)
	if err != nil {
		return response, err
	}

	response = UniqueCode{Code: code}
	return response, nil
}

func HandleRequest(_ context.Context, request CreateUniqueCodeRequest) (UniqueCode, error) {
	return generateRandomCode(request, NewRandomCode())
}

func CreatedOn() string {
	return time.Now().String()
}

func main() {
	lambda.Start(HandleRequest)
}

type UniqueCode struct {
	Code string `json:"code"`
}

type Item struct {
	Code        string
	CreatedOn   string
	UsedBy      *string
	Reference   *string
	Description *string
}
