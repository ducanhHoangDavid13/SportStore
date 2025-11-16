package sd_04.datn_fstore.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice // Đánh dấu đây là file xử lý lỗi toàn cục
public class GlobalExceptionHandler {

    /**
     * Bắt tất cả các lỗi RuntimeException (ví dụ: Hết hàng, Voucher sai,...)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<?> handleRuntimeException(RuntimeException ex, WebRequest request) {
        ex.printStackTrace(); // In lỗi ra console server

        // Trả về lỗi 400 (Bad Request) cho client
        // Client sẽ nhận được JSON: { "message": "Sản phẩm ABC không đủ số lượng." }
        return ResponseEntity
                .badRequest()
                .body(Map.of("message", ex.getMessage()));
    }
}