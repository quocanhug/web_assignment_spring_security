package vn.iotstar.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/error")
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

    @RequestMapping
    public String error(Model model) {
        model.addAttribute("message", "Đã xảy ra lỗi.");
        return "error";
    }
}
