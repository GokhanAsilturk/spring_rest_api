package source.controller;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
    private final UserControllerFallback fallbackHandler = new UserControllerFallback();


    @Operation(summary = "Tüm kullanıcıları listeler")
    @ApiResponse(responseCode = "200", description = "Kullanıcılar başarıyla listelendi")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "getAllUsersFallback")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<UserDto>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Fetching all users with page: {} and size: {}", page, size);
        return ResponseEntity.ok(userService.getAllUsers(page, size));
    }

    public ResponseEntity<List<UserDto>> getAllUsersFallback(int page, int size, Exception ex) {
        log.warn("Rate limit exceeded for getAllUsers. Page: {}, Size: {}", page, size);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "ID ile kullanıcı bilgilerini getirir")
    @ApiResponse(responseCode = "200", description = "Kullanıcı bulundu")
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RateLimiter(name = "userApi", fallbackMethod = "getUserByIdFallback")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        log.info("Fetching user with id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    public ResponseEntity<UserDto> getUserByIdFallback(Long id, Exception ex) {
        log.warn("Rate limit exceeded for getUserById. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Merchant ID ile kullanıcı bilgilerini getirir")
    @ApiResponse(responseCode = "200", description = "Kullanıcı bulundu")
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "getUserByMerchantIdFallback")
    @GetMapping(value = "/merchant/{merchantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> getUserByMerchantId(@PathVariable Long merchantId) {
        log.info("Fetching user with merchant id: {}", merchantId);
        return ResponseEntity.ok(userService.getUserByMerchantId(merchantId));
    }

    public ResponseEntity<UserDto> getUserByMerchantIdFallback(Long merchantId, Exception ex) {
        log.warn("Rate limit exceeded for getUserByMerchantId. MerchantId: {}", merchantId);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Kullanıcıyı siler")
    @ApiResponse(responseCode = "204", description = "Kullanıcı başarıyla silindi")
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "deleteUserByIdFallback")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteUserById(id);
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<Void> deleteUserByIdFallback(Long id, Exception ex) {
        log.warn("Rate limit exceeded for deleteUserById. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Kullanıcı bilgilerini günceller")
    @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz veri")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "updateUserFallback")
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto updatedUser) {
        log.info("Updating user with id: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, updatedUser));
    }

    public ResponseEntity<UserDto> updateUserFallback(Long id, UserDto updatedUser, Exception ex) {
        log.warn("Rate limit exceeded for updateUser. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Kullanıcı bilgilerini kısmen günceller")
    @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla güncellendi")
    @ApiResponse(responseCode = "400", description = "Geçersiz veri")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RateLimiter(name = "userApi", fallbackMethod = "partiallyUpdateUserFallback")
    @PatchMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> partiallyUpdateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDto partialUpdate) {
        log.info("Partially updating user with id: {}", id);
        return ResponseEntity.ok(userService.partiallyUpdateUser(id, partialUpdate));
    }

    public ResponseEntity<UserDto> partiallyUpdateUserFallback(Long id, UserDto partialUpdate, Exception ex) {
        log.warn("Rate limit exceeded for partiallyUpdateUser. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Kullanıcıyı pasif yapar")
    @ApiResponse(responseCode = "200", description = "Kullanıcı başarıyla pasif yapıldı")
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "deactivateUserFallback")
    @PatchMapping(value = "/{id}/deactivate", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserDto> deactivateUser(@PathVariable Long id) {
        log.info("Deactivating user with id: {}", id);
        return ResponseEntity.ok(userService.deactivateUser(id));
    }

    public ResponseEntity<UserDto> deactivateUserFallback(Long id, Exception ex) {
        log.warn("Rate limit exceeded for deactivateUser. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Kullanıcı ödemelerini PDF olarak indirir")
    @ApiResponse(responseCode = "200", description = "PDF başarıyla oluşturuldu")
    @ApiResponse(responseCode = "404", description = "Kullanıcı veya ödemeler bulunamadı")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @RateLimiter(name = "userApi", fallbackMethod = "downloadUserPaymentsFallback")
    @GetMapping(value = "/{id}/payments/download", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<Resource> downloadUserPayments(@PathVariable Long id) {
        log.info("Downloading payments for user with id: {}", id);
        byte[] fileData = userService.downloadUserPayments(id);
        ByteArrayResource resource = new ByteArrayResource(fileData);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=payments.pdf")
                .contentLength(fileData.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }

    public ResponseEntity<Resource> downloadUserPaymentsFallback(Long id, Exception ex) {
        log.warn("Rate limit exceeded for downloadUserPayments. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }

    @Operation(summary = "Kullanıcı siparişlerinin mutabakatını yapar")
    @ApiResponse(responseCode = "200", description = "Mutabakat başarılı")
    @ApiResponse(responseCode = "400", description = "Geçersiz PDF")
    @ApiResponse(responseCode = "404", description = "Kullanıcı bulunamadı")
    @ApiResponse(responseCode = "429", description = "Çok fazla istek yapıldı")
    @PreAuthorize("hasRole('ADMIN')")
    @RateLimiter(name = "userApi", fallbackMethod = "reconcileUserOrdersFallback")
    @PostMapping(value = "/{id}/orders/reconciliation",
            consumes = MediaType.APPLICATION_PDF_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReconciliationResponse> reconcileUserOrders(
            @PathVariable Long id,
            @RequestBody byte[] uploadedPdf) {
        log.info("Reconciling orders for user with id: {}", id);
        boolean result = userService.reconcileUserOrders(id, uploadedPdf);
        ReconciliationResponse response = new ReconciliationResponse(
                result,
                result ? "Mutabakat başarılı" : "Mutabakat başarısız"
        );
        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ReconciliationResponse> reconcileUserOrdersFallback(Long id, byte[] uploadedPdf, Exception ex) {
        log.warn("Rate limit exceeded for reconcileUserOrders. Id: {}", id);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "60")
                .build();
    }
}