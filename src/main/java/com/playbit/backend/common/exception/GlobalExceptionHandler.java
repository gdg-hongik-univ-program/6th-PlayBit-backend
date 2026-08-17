package com.playbit.backend.common.exception;

import com.playbit.backend.common.dto.ApiResponse;
import com.playbit.backend.common.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 공통 예외 처리

    // MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {

        // 첫 번째 에러 메시지 추출
        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(new ErrorResponse("BAD_REQUEST", message)));
    }

    // 500
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleUnknownException(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(new ErrorResponse("SERVER_ERROR", "서버 내부에서 오류가 발생하였습니다.")));
    }

    // 방 동시 입장 예외 핸들러
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleOptimisticLockingFailure(ObjectOptimisticLockingFailureException e) {
        // HTTP 409 Conflict 상태 코드와 함께 예쁜 메시지 반환
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(new ErrorResponse("BAD_REQUEST", "동시 요청이 발생했거나 이미 처리된 작업입니다. 새로고침 후 다시 시도해주세요.")));
    }

    // 커스텀 예외 처리

    // 404
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleBadRequestException(BadRequestException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(new ErrorResponse(errorCode.getCode(), errorCode.getMessage())));
    }

    // 404
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorResponse>> handleNotFoundException(NotFoundException e) {
        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getStatus())
                .body(ApiResponse.error(new ErrorResponse(errorCode.getCode(), errorCode.getMessage())));
    }
}
