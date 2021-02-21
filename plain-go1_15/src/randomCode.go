package main

import (
	"errors"
	"math/rand"
	"time"
)

type RandomCode interface {
	Generate(length uint8) (string, error)
}

type randomCode struct {
	random *rand.Rand
}

func NewRandomCode() RandomCode {
	return NewRandomCodeWithSeed(time.Now().UnixNano())
}

func NewRandomCodeWithSeed(seed int64) RandomCode {
	return randomCode{
		random: rand.New(rand.NewSource(seed)),
	}
}

func (r randomCode) Generate(length uint8) (string, error) {
	if length < 1 {
		return "", errors.New("Length must be between 1 and 255 both inclusive")
	}

	letters := "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
	lettersLength := len(letters)
	bytes := make([]byte, length)
	for i := range bytes {
		bytes[i] = letters[r.random.Intn(lettersLength)]
	}
	return string(bytes), nil
}
