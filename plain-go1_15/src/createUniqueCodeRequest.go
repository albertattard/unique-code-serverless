package main

type CreateUniqueCodeRequest struct {
	UsedBy      string `json:"usedBy"`
	Length      uint8  `json:"length"`
	Reference   string `json:"reference"`
	Description string `json:"description"`
}

func (request CreateUniqueCodeRequest) withDefaults() CreateUniqueCodeRequest {
	var length uint8 = 8
	if request.Length != 0 {
		length = request.Length
	}

	return CreateUniqueCodeRequest{
		UsedBy:      request.UsedBy,
		Length:      length,
		Reference:   request.Reference,
		Description: request.Description,
	}
}
