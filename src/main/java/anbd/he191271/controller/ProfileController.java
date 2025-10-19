package anbd.he191271.controller;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
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
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

@Controller
@RequestMapping("/customer")
public class ProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ProfileController.class);

    private final CustomerRepository customerRepository;

    public ProfileController(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
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

            if (name != null && !name.isEmpty()) customer.setName(name);

            if (dob != null && !dob.isEmpty()) {
                try {
                    customer.setDob(LocalDate.parse(dob));
                } catch (DateTimeParseException dtpe) {
                    logger.warn("Invalid dob format: {}", dob);
                }
            }

            customerRepository.save(customer);
            session.setAttribute("customer", customer);

            return "redirect:/customer/profile";
        } catch (Exception ex) {
            logger.error("Unexpected error in POST /customer/profile/update", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

    // Cập nhật avatar -> lưu byte[] vào DB
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
                customer.setAvatar(avatarFile.getBytes());
                customerRepository.save(customer);
                session.setAttribute("customer", customer);
            }

            return "redirect:/customer/profile";
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

    // Trả ảnh avatar từ DB
    @GetMapping("/avatar/{id}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable int id) {
        Optional<Customer> optionalCustomer = customerRepository.findById(id);
        if (optionalCustomer.isPresent() && optionalCustomer.get().getAvatar() != null) {
            byte[] imageBytes = optionalCustomer.get().getAvatar();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // hoặc IMAGE_PNG nếu bạn lưu PNG
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
