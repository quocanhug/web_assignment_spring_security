package vn.iotstar.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.dto.*;
import vn.iotstar.entity.Role;
import vn.iotstar.entity.User;
import vn.iotstar.mapper.UserMapper;
import vn.iotstar.repository.RoleRepository;
import vn.iotstar.repository.UserRepository;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.OtpService;
import vn.iotstar.service.UserService;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final OtpService otpService;

    @Override
    @Transactional
    public void register(RegisterDTO dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập '" + dto.getUsername() + "' đã được sử dụng.");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email '" + dto.getEmail() + "' đã được sử dụng.");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        Role userRole = roleRepository.findByNameIgnoreCase("ROLE_USER")
                .or(() -> roleRepository.findByNameIgnoreCase("USER"))
                .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

        String avatarUrl = "/images/avatar-default.png";
        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            String uploaded = cloudinaryService.uploadImage(dto.getAvatarFile());
            if (uploaded != null) {
                avatarUrl = uploaded;
            }
        }

        User user = User.builder()
                .username(dto.getUsername().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName().trim())
                .images(avatarUrl)
                .role(userRole)
                .enabled(false) // Wait for OTP confirmation
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);

        // Send OTP
        otpService.generateAndSendOtp(user.getEmail(), "REGISTER");
    }

    @Override
    @Transactional
    public boolean verifyRegistrationOtp(String email, String token) {
        boolean valid = otpService.verifyOtp(email, token, "REGISTER");
        if (valid) {
            userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                user.setEnabled(true);
                userRepository.save(user);
                log.info("User {} activated via OTP", user.getEmail());
            });
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void resendOtp(String email, String type) {
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với email: " + email));
        otpService.generateAndSendOtp(user.getEmail(), type);
    }

    @Override
    @Transactional
    public void sendForgotPasswordOtp(String email) {
        User user = userRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new IllegalArgumentException("Email '" + email + "' chưa được đăng ký trong hệ thống."));
        otpService.generateAndSendOtp(user.getEmail(), "FORGOT_PASSWORD");
    }

    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp.");
        }

        boolean valid = otpService.verifyOtp(dto.getEmail(), dto.getOtp(), "FORGOT_PASSWORD");
        if (!valid) {
            return false;
        }

        User user = userRepository.findByEmailIgnoreCase(dto.getEmail().trim())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản."));

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        user.setEnabled(true);
        userRepository.save(user);
        log.info("Password reset successfully and account enabled for user: {}", user.getEmail());
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDTO> findAll(String keyword, Pageable pageable) {
        Page<User> users;
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            users = userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(
                    kw, kw, kw, pageable
            );
        } else {
            users = userRepository.findAll(pageable);
        }
        return users.map(userMapper::toDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserFormDTO findFormById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với id: " + id));

        return UserFormDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .roleId(user.getRole() != null ? user.getRole().getId() : null)
                .enabled(user.isEnabled())
                .images(user.getImages())
                .build();
    }

    @Override
    @Transactional
    public UserDTO createUser(UserFormDTO dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại.");
        }
        if (userRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email đã tồn tại.");
        }

        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ."));

        String avatarUrl = "/images/avatar-default.png";
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String uploaded = cloudinaryService.uploadImage(dto.getImageFile());
            if (uploaded != null) {
                avatarUrl = uploaded;
            }
        }

        String password = dto.getPassword() != null && !dto.getPassword().isBlank()
                ? dto.getPassword()
                : "123456";

        User user = User.builder()
                .username(dto.getUsername().trim())
                .email(dto.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(password))
                .fullName(dto.getFullName().trim())
                .images(avatarUrl)
                .role(role)
                .enabled(dto.isEnabled())
                .createdAt(LocalDateTime.now())
                .build();

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UserFormDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));

        // If email changed, check uniqueness
        if (!user.getEmail().equalsIgnoreCase(dto.getEmail().trim())
                && userRepository.existsByEmailIgnoreCase(dto.getEmail().trim())) {
            throw new IllegalArgumentException("Email mới đã tồn tại trên hệ thống.");
        }

        // If username changed, check uniqueness
        if (!user.getUsername().equalsIgnoreCase(dto.getUsername().trim())
                && userRepository.existsByUsernameIgnoreCase(dto.getUsername().trim())) {
            throw new IllegalArgumentException("Tên đăng nhập mới đã tồn tại trên hệ thống.");
        }

        user.setUsername(dto.getUsername().trim());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setFullName(dto.getFullName().trim());
        user.setEnabled(dto.isEnabled());

        if (dto.getRoleId() != null) {
            Role role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new IllegalArgumentException("Vai trò không hợp lệ."));
            user.setRole(role);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String uploaded = cloudinaryService.uploadImage(dto.getImageFile());
            if (uploaded != null) {
                user.setImages(uploaded);
            }
        }

        user = userRepository.save(user);
        return userMapper.toDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng cần xóa."));
        userRepository.delete(user);
    }

    @Override
    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng."));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public long countUsers() {
        return userRepository.count();
    }
}
