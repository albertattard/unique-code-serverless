package main

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestCreateUniqueCodeRequest_withDefaults(t *testing.T) {
	request := CreateUniqueCodeRequest{}.withDefaults()
	assert.Equal(t, uint8(8), request.Length, "Expected a default value of 8")

	request = CreateUniqueCodeRequest{Length: 10}.withDefaults()
	assert.Equal(t, uint8(10), request.Length, "Expected the value of 10")
}
