package vn.iotstar.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.iotstar.dto.ForgotPasswordDTO;
import vn.iotstar.dto.RegisterDTO;
import vn.iotstar.dto.ResetPasswordDTO;
import vn.iotstar.dto.VerifyOtpDTO;
import vn.iotstar.service.UserService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    // ==============================================================
    // LOGIN
    // ==============================================================
    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    // ==============================================================
    // REGISTER WITH OTP
    // ==============================================================
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerDTO")) {
            model.addAttribute("registerDTO", new RegisterDTO());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(
            @Valid @ModelAttribute("registerDTO") RegisterDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            userService.register(dto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đăng ký thành công! Mã OTP đã được gửi đến email " + dto.getEmail() + ". Vui lòng kiểm tra và xác thực.");
            return "redirect:/verify-otp?email=" + dto.getEmail() + "&type=REGISTER";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register";
        } catch (Exception e) {
            log.error("Register error", e);
            model.addAttribute("errorMessage", "Đã có lỗi xảy ra trong quá trình đăng ký. Vui lòng thử lại!");
            return "auth/register";
        }
    }

    // ==============================================================
    // VERIFY OTP
    // ==============================================================
    @GetMapping("/verify-otp")
    public String showVerifyOtpForm(
            @RequestParam(name = "email", required = false, defaultValue = "") String email,
            @RequestParam(name = "type", required = false, defaultValue = "REGISTER") String type,
            Model model
    ) {
        VerifyOtpDTO dto = VerifyOtpDTO.builder()
                .email(email)
                .type(type)
                .build();
        model.addAttribute("verifyOtpDTO", dto);
        return "auth/verify-otp";
    }

    @PostMapping("/verify-otp")
    public String processVerifyOtp(
            @Valid @ModelAttribute("verifyOtpDTO") VerifyOtpDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/verify-otp";
        }

        boolean verified = userService.verifyRegistrationOtp(dto.getEmail(), dto.getOtp());
        if (verified) {
            redirectAttributes.addFlashAttribute("successMessage", "Xác thực tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ.");
            return "redirect:/login?verified=true";
        } else {
            model.addAttribute("errorMessage", "Mã OTP không chính xác hoặc đã hết hạn. Vui lòng kiểm tra lại!");
            return "auth/verify-otp";
        }
    }

    @GetMapping("/resend-otp")
    public String resendOtp(
            @RequestParam("email") String email,
            @RequestParam(name = "type", defaultValue = "REGISTER") String type,
            RedirectAttributes redirectAttributes
    ) {
        try {
            userService.resendOtp(email, type);
            redirectAttributes.addFlashAttribute("successMessage", "Mã OTP mới đã được gửi lại đến email " + email);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        if ("FORGOT_PASSWORD".equalsIgnoreCase(type)) {
            return "redirect:/reset-password?email=" + email;
        }
        return "redirect:/verify-otp?email=" + email + "&type=" + type;
    }

    // ==============================================================
    // FORGOT PASSWORD
    // ==============================================================
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordDTO")) {
            model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        }
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(
            @Valid @ModelAttribute("forgotPasswordDTO") ForgotPasswordDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }

        try {
            userService.sendForgotPasswordOtp(dto.getEmail());
            redirectAttributes.addFlashAttribute("successMessage",
                    "Mã OTP đặt lại mật khẩu đã được gửi đến " + dto.getEmail() + ". Vui lòng nhập mã để tạo mật khẩu mới.");
            return "redirect:/reset-password?email=" + dto.getEmail();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/forgot-password";
        } catch (Exception e) {
            log.error("Forgot password error", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi gửi yêu cầu. Vui lòng thử lại!");
            return "auth/forgot-password";
        }
    }

    // ==============================================================
    // RESET PASSWORD
    // ==============================================================
    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(name = "email", required = false, defaultValue = "") String email,
            Model model
    ) {
        ResetPasswordDTO dto = ResetPasswordDTO.builder()
                .email(email)
                .build();
        model.addAttribute("resetPasswordDTO", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(
            @Valid @ModelAttribute("resetPasswordDTO") ResetPasswordDTO dto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }

        try {
            boolean success = userService.resetPassword(dto);
            if (success) {
                redirectAttributes.addFlashAttribute("successMessage", "Đặt lại mật khẩu thành công! Hãy đăng nhập bằng mật khẩu mới.");
                return "redirect:/login?reset=true";
            } else {
                model.addAttribute("errorMessage", "Mã OTP không chính xác hoặc đã hết hạn!");
                return "auth/reset-password";
            }
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/reset-password";
        } catch (Exception e) {
            log.error("Reset password error", e);
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi đặt lại mật khẩu. Vui lòng thử lại!");
            return "auth/reset-password";
        }
    }
}
