package main

import (
	"context"
	"errors"
	"fmt"
	"math/rand"
	"time"

	"github.com/aws/aws-lambda-go/lambda"
	"github.com/aws/aws-sdk-go/aws"
	"github.com/aws/aws-sdk-go/aws/session"
	"github.com/aws/aws-sdk-go/service/dynamodb"
	"github.com/aws/aws-sdk-go/service/dynamodb/dynamodbattribute"
)

func HandleRequest(_ context.Context, request CreateUniqueCodeRequest) (UniqueCode, error) {
	code, _ := GenerateRandomCode(8)
	response := UniqueCode{Code: code}

	sess := session.Must(session.NewSessionWithOptions(session.Options{
		SharedConfigState: session.SharedConfigEnable,
	}))

	svc := dynamodb.New(sess)

	item := Item{
		Code:      code,
		CreatedOn: CreatedOn(),
	}

	av, err := dynamodbattribute.MarshalMap(item)
	if err != nil {
		fmt.Println("Got error marshalling map:")
		fmt.Println(err.Error())
		return response, errors.New("Failed to marshal item to map")
	}

	// Create item in table Movies
	input := &dynamodb.PutItemInput{
		Item:      av,
		TableName: aws.String("UniqueCodes"),
	}

	_, err = svc.PutItem(input)
	if err != nil {
		fmt.Println("Got error calling PutItem:")
		fmt.Println(err.Error())
		return response, errors.New("Failed to save item to DynamoDB")
	}

	return response, nil
}

func GenerateRandomCode(length uint8) (string, error) {
	if length < 1 {
		return "", errors.New("Length must be between 1 and 255 both inclusive")
	}

	letters := "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	lettersLength := len(letters)
	bytes := make([]byte, length)
	for i := range bytes {
		bytes[i] = letters[rand.Intn(lettersLength)]
	}
	return string(bytes), nil
}

func CreatedOn() string {
	return time.Now().String()
}

func main() {
	lambda.Start(HandleRequest)
}

type CreateUniqueCodeRequest struct {
	UsedBy      string `json:"usedBy"`
	Length      uint8  `json:"length"`
	Reference   string `json:"reference"`
	Description string `json:"description"`
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
