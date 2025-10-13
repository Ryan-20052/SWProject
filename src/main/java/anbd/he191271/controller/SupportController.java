package anbd.he191271.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping ("/support")
@Controller
public class SupportController {
    @GetMapping("/view")
    public String viewSupportPage() {
        return "support.html";
    }
}

