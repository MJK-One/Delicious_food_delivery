package com.dfdt.delivery.domain.address.presentation.controller;

public interface AddressErrorDocs {
    String ADDRESS_NOT_FOUND = "{\"status\": 404, \"errorCode\": \"ADDRESS-001\", \"message\": \"배송지를 찾을 수 없습니다.\"}";
    String ADDRESS_ACCESS_DENIED = "{\"status\": 403, \"errorCode\": \"ADDRESS-002\", \"message\": \"배송지에 대한 권한이 없습니다.\"}";
    String REGION_NOT_FOUND = "{\"status\": 404, \"errorCode\": \"REGION-4040\", \"message\": \"해당 Region이 존재하지 않습니다.\"}";
    String INVALID_INPUT_VALUE = "{\"status\": 400, \"errorCode\": \"INVALID_REQUEST\", \"message\": \"@Valid 형식 에러\"}";
}