package anbd.he191271.controller;

import anbd.he191271.dto.LoginRequest;
import anbd.he191271.entity.Customer;
import anbd.he191271.entity.Manager;
import anbd.he191271.service.CustomerService;
import anbd.he191271.service.ManagerService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/manager")
public class ManagerLoginController {

    private final ManagerService managerService;

    public ManagerLoginController(ManagerService managerService) {
        this.managerService = managerService;
    }

    @PostMapping("/login")
    public ResponseEntity<Manager> login(@RequestBody LoginRequest request, HttpSession session) {
        return managerService.login(request.getUsername(), request.getPassword())
                .map(manager -> {
                    session.setAttribute("manager", manager);
                    return ResponseEntity.ok(manager);
                })
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login.html";
    }
}
