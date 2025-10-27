package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.repository.ManagerRepository;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/manager")
public class ManagerProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerProfileController.class);

    private final ManagerRepository managerRepository;

    public ManagerProfileController(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    // Hiển thị trang profile
    @GetMapping("/profile")
    public String profilePage(HttpSession session,
                              @RequestParam(value = "editMode", required = false, defaultValue = "false") boolean editMode,
                              Model model) {
        Manager manager = (Manager) session.getAttribute("manager");
        if (manager == null) {
            return "redirect:/login.html";
        }
        model.addAttribute("manager", manager);
        model.addAttribute("editMode", editMode);
        return "managerProfile";
    }

    // Update thông tin với validate (chỉ dùng các trường có sẵn)
    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                Model model) {
        try {
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager == null) {
                return "redirect:/login.html";
            }

            // Danh sách lỗi
            List<String> errors = new ArrayList<>();

            // Validate tên
            if (name == null || name.trim().isEmpty()) {
                errors.add("Họ tên không được để trống");
            } else {
                name = name.trim();
                if (name.length() < 2) {
                    errors.add("Họ tên phải có ít nhất 2 ký tự");
                }
                if (name.length() > 100) {
                    errors.add("Họ tên không được vượt quá 100 ký tự");
                }
            }

            // Validate số điện thoại
            if(phone == null || phone.trim().isEmpty()){
                errors.add("Số điện thoại không được để trống");
            }
            else {
                phone = phone.trim();
                if (!phone.matches("^(\\+84|0)[3|5|7|8|9][0-9]{8}$")) {
                    errors.add("Số điện thoại không hợp lệ");
                }
            }

            // Nếu có lỗi, trả về trang với thông báo lỗi
            if (!errors.isEmpty()) {
                model.addAttribute("manager", manager);
                model.addAttribute("editMode", true);
                model.addAttribute("errors", errors);
                return "managerProfile";
            }

            // Cập nhật thông tin nếu không có lỗi (chỉ các trường có sẵn)
            manager.setName(name);
            if (phone != null) manager.setPhone(phone);

            managerRepository.save(manager);
            session.setAttribute("manager", manager);

            return "redirect:/manager/profile?success=true";

        } catch (Exception ex) {
            logger.error("Unexpected error in POST /manager/profile/update", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

    // Cập nhật avatar với validate
    @PostMapping("/profile/update-avatar")
    public String updateAvatar(HttpSession session,
                               @RequestParam("avatar") MultipartFile avatarFile,
                               Model model) {
        try {
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager == null) {
                return "redirect:/login.html";
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                // Validate file type
                String contentType = avatarFile.getContentType();
                if (!contentType.startsWith("image/")) {
                    model.addAttribute("manager", manager);
                    model.addAttribute("editMode", true);
                    model.addAttribute("errors", List.of("Chỉ được upload file ảnh"));
                    return "managerProfile";
                }

                // Validate file size (max 5MB)
                if (avatarFile.getSize() > 5 * 1024 * 1024) {
                    model.addAttribute("manager", manager);
                    model.addAttribute("editMode", true);
                    model.addAttribute("errors", List.of("Kích thước ảnh không được vượt quá 5MB"));
                    return "managerProfile";
                }

                manager.setAvatar(avatarFile.getBytes());
                managerRepository.save(manager);
                session.setAttribute("manager", manager);
            }

            return "redirect:/manager/profile?success=true";

        } catch (IOException ioe) {
            logger.error("IO error when saving avatar", ioe);
            model.addAttribute("errorMessage", "Lỗi khi lưu avatar: " + ioe.getMessage());
            return "error/simple-error";
        } catch (Exception ex) {
            logger.error("Unexpected error in POST /manager/profile/update-avatar", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

    // Trả ảnh avatar từ DB
    @GetMapping("/avatar/{id}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable int id) {
        Optional<Manager> optionalManager = managerRepository.findById(id);
        if (optionalManager.isPresent() && optionalManager.get().getAvatar() != null) {
            byte[] imageBytes = optionalManager.get().getAvatar();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}