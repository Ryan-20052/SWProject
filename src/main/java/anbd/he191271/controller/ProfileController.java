package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import anbd.he191271.service.CustomerProfileService;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final CustomerRepository customerRepository;
    private final CustomerProfileService customerProfileService;

    public ProfileController(CustomerRepository customerRepository,
                             CustomerProfileService customerProfileService) {
        this.customerRepository = customerRepository;
        this.customerProfileService = customerProfileService;
    }

    @GetMapping("/profile")
    public String profilePage(HttpSession session,
                              @RequestParam(value = "editMode", required = false, defaultValue = "false") boolean editMode,
                              Model model) {
        Customer customer = (Customer) session.getAttribute("customer");
        if (customer == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("customer", customer);
        model.addAttribute("editMode", editMode);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam(required = false) String dob,
                                Model model) {
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                return "redirect:/login.html";
            }

            // Validate dữ liệu using service
            List<String> errors = customerProfileService.validateProfileData(name, dob);

            // Nếu có lỗi, trả về trang profile với thông báo lỗi
            if (!errors.isEmpty()) {
                model.addAttribute("customer", customer);
                model.addAttribute("editMode", true);
                model.addAttribute("errors", errors);
                return "profile";
            }

            // Cập nhật thông tin using service
            Customer updatedCustomer = customerProfileService.updateProfile(customer, name, dob);
            session.setAttribute("customer", updatedCustomer);

            return "redirect:/customer/profile?success=true";

        } catch (Exception ex) {
            logger.error("Unexpected error in POST /customer/profile/update", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

    @PostMapping("/profile/update-avatar")
    public String updateAvatar(HttpSession session,
                               @RequestParam("avatar") MultipartFile avatarFile,
                               Model model) {
        try {
            Customer customer = (Customer) session.getAttribute("customer");
            if (customer == null) {
                return "redirect:/login.html";
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Validate avatar using service
                List<String> errors = customerProfileService.validateAvatar(avatarFile);
                if (!errors.isEmpty()) {
                    model.addAttribute("customer", customer);
                    model.addAttribute("editMode", false);
                    model.addAttribute("errors", errors);
                    return "profile";
                }

                // Update avatar using service
                Customer updatedCustomer = customerProfileService.updateAvatar(customer, avatarFile);
                session.setAttribute("customer", updatedCustomer);
            }

            return "redirect:/customer/profile?success=true";

        } catch (IOException ioe) {
            logger.error("IO error when saving avatar", ioe);
            model.addAttribute("errorMessage", "Lỗi khi lưu avatar: " + ioe.getMessage());
            return "error/simple-error";
        } catch (Exception ex) {
            logger.error("Unexpected error in POST /customer/profile/update-avatar", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

    @GetMapping("/avatar/{id}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable int id) {
        Optional<byte[]> avatarData = customerProfileService.getAvatarData(id);
        if (avatarData.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(avatarData.get(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}