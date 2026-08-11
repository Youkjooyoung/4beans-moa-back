package com.moa.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class ApiResponseContractTest {

    @Test
    void successResponseKeepsFrontendContract() {
        ApiResponse<Map<String, Object>> response = ApiResponse.success(Map.of("id", 1));

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("id", 1);
        assertThat(response.getError()).isNull();
    }

    @Test
    void errorResponseKeepsFrontendContract() {
        ApiResponse<Void> response = ApiResponse.error(ErrorCode.BAD_REQUEST, "Invalid request");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getData()).isNull();
        assertThat(response.getError()).isNotNull();
        assertThat(response.getError().getCode()).isEqualTo(ErrorCode.BAD_REQUEST.getCode());
        assertThat(response.getError().getMessage()).isEqualTo("Invalid request");
    }
}
