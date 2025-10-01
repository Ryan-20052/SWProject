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
            return "redirect:/auth/manager/login";
        }
        model.addAttribute("manager", manager);
        model.addAttribute("editMode", editMode);
        return "managerProfile"; // file managerProfile.html trong templates/
    }

    // Update thông tin
    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam(required = false) String phone,
                                Model model) {
        try {
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager == null) {
                return "redirect:/auth/manager/login";
            }

            if (name != null) manager.setName(name);
            if (phone != null) manager.setPhone(phone);

            managerRepository.save(manager);
            session.setAttribute("manager", manager);

            return "redirect:/manager/profile";
        } catch (Exception ex) {
            logger.error("Unexpected error in POST /manager/profile/update", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

    // Cập nhật avatar
    @PostMapping("/profile/update-avatar")
    public String updateAvatar(HttpSession session,
                               @RequestParam("avatar") MultipartFile avatarFile,
                               Model model) {
        try {
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager == null) {
                return "redirect:/auth/manager/login";
            }

            if (avatarFile != null && !avatarFile.isEmpty()) {
                manager.setAvatar(avatarFile.getBytes());
                managerRepository.save(manager);
                session.setAttribute("manager", manager);
            }

            return "redirect:/manager/profile";
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
            byte[] imageBytes = optionalManager.get().getAvatar(); // đã là byte[] rồi
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG); // nếu bạn lưu PNG thì đổi thành IMAGE_PNG
            return new ResponseEntity<>(imageBytes, headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
