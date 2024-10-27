package source.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserControllerFallback {

    public <T> ResponseEntity<T> buildFallbackResponse(String method, Object... params) {
        log.warn("Rate limit exceeded for {}. Params: {}", method, params);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, "60") // Retry after 60 seconds
                .build();
    }
}
