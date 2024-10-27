package source.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import source.repository.User;
import source.repository.UserDto;
import source.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    public UserDto getUserByMerchantId(Long merchantId) {
        return userRepository.findByMerchantId(merchantId)
                .map(UserDto::fromEntity)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
    }

    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Kullanıcı bulunamadı");
        }
        userRepository.deleteById(id);
    }

    public UserDto updateUser(Long id, UserDto updatedUser) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());
        user.setMerchantId(updatedUser.getMerchantId());
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    public UserDto partiallyUpdateUser(Long id, UserDto partialUpdate) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        if (partialUpdate.getName() != null) user.setName(partialUpdate.getName());
        if (partialUpdate.getEmail() != null) user.setEmail(partialUpdate.getEmail());
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    public UserDto deactivateUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        user.setActive(false);
        userRepository.save(user);
        return UserDto.fromEntity(user);
    }

    public byte[] downloadUserPayments(Long userId) {
        return new byte[0];
    }

    public boolean reconcileUserOrders(Long userId, byte[] uploadedPdf) {
        return true;
    }
}
