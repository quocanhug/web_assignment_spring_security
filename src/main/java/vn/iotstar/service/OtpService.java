package vn.iotstar.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.iotstar.entity.OtpToken;
import vn.iotstar.repository.OtpTokenRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String generateAndSendOtp(String email, String type) {
        String token = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(5);

        OtpToken otpToken = OtpToken.builder()
                .email(email.toLowerCase().trim())
                .token(token)
                .type(type.toUpperCase())
                .expiryDate(expiryDate)
                .used(false)
                .createdAt(LocalDateTime.now())
                .build();

        otpTokenRepository.save(otpToken);

        emailService.sendOtpEmail(email, token, type);

        return token;
    }

    @Transactional
    public boolean verifyOtp(String email, String token, String type) {
        String cleanEmail = email.toLowerCase().trim();
        var opt = otpTokenRepository.findTopByEmailAndTypeAndUsedFalseOrderByCreatedAtDesc(cleanEmail, type.toUpperCase());

        if (opt.isEmpty()) {
            log.warn("No active OTP found for email: {} and type: {}", cleanEmail, type);
            return false;
        }

        OtpToken otpToken = opt.get();

        if (otpToken.isExpired()) {
            log.warn("OTP has expired for email: {}", cleanEmail);
            return false;
        }

        if (!otpToken.getToken().equals(token.trim())) {
            log.warn("OTP mismatch for email: {}. Provided: {}, Expected: {}", cleanEmail, token, otpToken.getToken());
            return false;
        }

        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        log.info("OTP verified successfully for email: {} and type: {}", cleanEmail, type);
        return true;
    }
}
