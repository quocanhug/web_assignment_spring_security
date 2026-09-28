package vn.iotstar.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.iotstar.dto.*;

public interface UserService {

    void register(RegisterDTO dto);

    boolean verifyRegistrationOtp(String email, String token);

    void resendOtp(String email, String type);

    void sendForgotPasswordOtp(String email);

    boolean resetPassword(ResetPasswordDTO dto);

    Page<UserDTO> findAll(String keyword, Pageable pageable);

    UserDTO findById(Long id);

    UserFormDTO findFormById(Long id);

    UserDTO createUser(UserFormDTO dto);

    UserDTO updateUser(Long id, UserFormDTO dto);

    void deleteUser(Long id);

    void toggleUserStatus(Long id);

    long countUsers();
}
