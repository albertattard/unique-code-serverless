package main

import (
	"github.com/stretchr/testify/assert"
	"testing"
)

func Test_generateRandomCode(t *testing.T) {
	var length uint8 = 12
	code, err := NewRandomCode().Generate(length)

	assert.Nil(t, err, "Was expecting no errors")
	assert.Equal(t, int(length), len(code), "Was expecting a string of length %v", length)

	assert.NotEqual(t, "FPLLNGZIEYOH", code, "The random code is using the default seed, making it predictable")
}
