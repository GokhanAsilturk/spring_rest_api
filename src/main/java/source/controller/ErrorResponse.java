package source.controller;

import lombok.Value;

@Value
class ErrorResponse {
    String code;
    String message;
}
