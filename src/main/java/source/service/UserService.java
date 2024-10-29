package source.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import source.exception.UserNotFoundException;
import source.repository.User;
import source.repository.UserDto;
import source.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<UserDto> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .stream()
                .map(UserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserDto getUserMerchant(Long userId, Long merchantId) {
        User user = userRepository.findByIdAndMerchantId(userId, merchantId)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User ID %d with Merchant ID %d not found", userId, merchantId)));
        return UserDto.fromEntity(user);
    }

    @Transactional
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto updatedUser) {
        User user = getUserEntityById(id);
        updateUserFields(user, updatedUser);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDto partiallyUpdateUser(Long id, UserDto partialUpdate) {
        User user = getUserEntityById(id);
        updateUserFieldsIfPresent(user, partialUpdate);
        return UserDto.fromEntity(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUserStatus(Long id, boolean active) {
        User user = getUserEntityById(id);
        user.setActive(active);
        return UserDto.fromEntity(userRepository.save(user));
    }

    public byte[] getUserPaymentsPdf(Long userId) {
        // Kullanıcının varlığını kontrol et
        getUserEntityById(userId);

        // PDF oluşturma mantığı burada implemente edilecek
        return new byte[0];
    }

    public boolean verifyUserOrders(Long userId, byte[] ordersPdf) {
        // Kullanıcının varlığını kontrol et
        getUserEntityById(userId);

        // Doğrulama mantığı burada implemente edilecek
        return true;
    }

    // Private yardımcı metodlar

    private User getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private void updateUserFields(User user, UserDto updatedUser) {
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setMerchantId(updatedUser.getMerchantId());
        // Diğer alanlar burada güncellenebilir
    }

    private void updateUserFieldsIfPresent(User user, UserDto partialUpdate) {
        if (partialUpdate.getName() != null) {
            user.setName(partialUpdate.getName());
        }
        if (partialUpdate.getEmail() != null) {
            user.setEmail(partialUpdate.getEmail());
        }
        if (partialUpdate.getMerchantId() != null) {
            user.setMerchantId(partialUpdate.getMerchantId());
        }
        // Diğer alanlar burada şartlı olarak güncellenebilir
    }
}