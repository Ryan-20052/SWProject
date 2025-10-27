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
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
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

            // Danh sách lỗi
            List<String> errors = new ArrayList<>();

            // Validate tên
            if (name == null || name.trim().isEmpty()) {
                errors.add("Tên không được để trống");
            } else {
                name = name.trim();
                // Có thể thêm các validate khác cho tên nếu cần
                if (name.length() < 2) {
                    errors.add("Tên phải có ít nhất 2 ký tự");
                }
                if (name.length() > 100) {
                    errors.add("Tên không được vượt quá 100 ký tự");
                }
            }

            // Validate ngày sinh
            LocalDate dobDate = null;
            if (dob != null && !dob.isEmpty()) {
                try {
                    dobDate = LocalDate.parse(dob);

                    // Tính tuổi
                    LocalDate today = LocalDate.now();
                    int age = Period.between(dobDate, today).getYears();

                    // Kiểm tra độ tuổi từ 12 đến 100
                    if (age < 12) {
                        errors.add("Bạn phải từ 12 tuổi trở lên");
                    } else if (age > 100) {
                        errors.add("Ngày sinh không hợp lệ");
                    }

                    // Kiểm tra ngày sinh không ở tương lai
                    if (dobDate.isAfter(today)) {
                        errors.add("Ngày sinh không thể ở tương lai");
                    }

                } catch (DateTimeParseException dtpe) {
                    errors.add("Định dạng ngày sinh không hợp lệ");
                }
            }

            // Nếu có lỗi, trả về trang profile với thông báo lỗi
            if (!errors.isEmpty()) {
                model.addAttribute("customer", customer);
                model.addAttribute("editMode", true);
                model.addAttribute("errors", errors);
                return "profile";
            }

            // Cập nhật thông tin nếu không có lỗi
            customer.setName(name);
            if (dobDate != null) {
                customer.setDob(dobDate);
            }

            customerRepository.save(customer);
            session.setAttribute("customer", customer);

            return "redirect:/customer/profile?success=true";

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
                // Validate file type
                String contentType = avatarFile.getContentType();
                if (!contentType.startsWith("image/")) {
                    model.addAttribute("customer", customer);
                    model.addAttribute("editMode", true);
                    model.addAttribute("errors", List.of("Chỉ được upload file ảnh"));
                    return "profile";
                }

                // Validate file size (ví dụ: max 5MB)
                if (avatarFile.getSize() > 5 * 1024 * 1024) {
                    model.addAttribute("customer", customer);
                    model.addAttribute("editMode", true);
                    model.addAttribute("errors", List.of("Kích thước ảnh không được vượt quá 5MB"));
                    return "profile";
                }

                customer.setAvatar(avatarFile.getBytes());
                customerRepository.save(customer);
                session.setAttribute("customer", customer);
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