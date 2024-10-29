package source.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import source.repository.UserDto;
import source.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Kullanıcı Yönetimi", description = "Kullanıcı işlemleri için API endpoints")
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "Tüm kullanıcıları listeler")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all users with page: {} and size: {}", page, size);
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    @Operation(summary = "ID ile kullanıcı bilgilerini getirir")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Kullanıcının merchant bilgilerini getirir")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @GetMapping(value = "/{userId}/merchants/{merchantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUserMerchant(
            @PathVariable Long userId,
            @PathVariable Long merchantId) {
        log.info("Fetching merchant {} for user {}", merchantId, userId);
        return ResponseEntity.ok(userService.getUserMerchant(userId, merchantId));
    }

    @Operation(summary = "Kullanıcıyı siler")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Kullanıcı bilgilerini günceller")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto updatedUser) {
        log.info("Updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    @Operation(summary = "Kullanıcı bilgilerini kısmen günceller")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> partiallyUpdateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto partialUpdate) {
        log.info("Partially updating user with id: {}", id);
        return ResponseEntity.ok(userService.partiallyUpdateUser(id, partialUpdate));
    }

    @Operation(summary = "Kullanıcının durumunu günceller")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @PatchMapping(value = "/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        log.info("Updating status for user with id: {}", id);
        return ResponseEntity.ok(userService.updateUserStatus(id, active));
    }

    @Operation(summary = "Kullanıcı ödemelerini PDF olarak alır")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @GetMapping(value = "/{id}/payments", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> getUserPaymentsPdf(@PathVariable Long id) {
        log.info("Getting payments PDF for user with id: {}", id);
        byte[] fileData = userService.getUserPaymentsPdf(id);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=payments.pdf")
                .contentLength(fileData.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    @Operation(summary = "Kullanıcı siparişlerinin mutabakatını kontrol eder")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "rateLimitFallback")
    @PostMapping(value = "/{id}/orders/verification",
            consumes = MediaType.APPLICATION_PDF_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReconciliationResponse> verifyUserOrders(
            @PathVariable Long id,
            @RequestBody byte[] ordersPdf) {
        log.info("Verifying orders for user with id: {}", id);
        boolean result = userService.verifyUserOrders(id, ordersPdf);
        ReconciliationResponse response = new ReconciliationResponse(
                result,
                result ? "Doğrulama başarılı" : "Doğrulama başarısız"
        );
        return ResponseEntity.ok(response);
    }

    private <T> ResponseEntity<T> rateLimitFallback(Exception ex) {
        log.warn("Rate limit exceeded for request");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }
}