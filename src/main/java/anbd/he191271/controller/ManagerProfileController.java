package anbd.he191271.controller;

import anbd.he191271.entity.Manager;
import anbd.he191271.repository.ManagerRepository;
import anbd.he191271.service.ManagerProfileService;
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
@RequestMapping("/manager")
public class ManagerProfileController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerProfileController.class);

    private final ManagerRepository managerRepository;
    private final ManagerProfileService managerProfileService;

    public ManagerProfileController(ManagerRepository managerRepository,
                                    ManagerProfileService managerProfileService) {
        this.managerRepository = managerRepository;
        this.managerProfileService = managerProfileService;
    }

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

    @PostMapping("/profile/update")
    public String updateProfile(HttpSession session,
                                @RequestParam String name,
                                @RequestParam String phone,
                                Model model) {
        try {
            Manager manager = (Manager) session.getAttribute("manager");
            if (manager == null) {
                return "redirect:/login.html";
            }

            // Validate dữ liệu using service
            List<String> errors = managerProfileService.validateProfileData(name, phone);

            if (!errors.isEmpty()) {
                model.addAttribute("manager", manager);
                model.addAttribute("editMode", true);
                model.addAttribute("errors", errors);
                return "managerProfile";
            }

            // Cập nhật thông tin using service
            Manager updatedManager = managerProfileService.updateProfile(manager, name, phone);
            session.setAttribute("manager", updatedManager);

            return "redirect:/manager/profile?success=true";

        } catch (Exception ex) {
            logger.error("Unexpected error in POST /manager/profile/update", ex);
            model.addAttribute("errorMessage", "Có lỗi: " + ex.getMessage());
            return "error/simple-error";
        }
    }

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
                // Validate avatar using service
                List<String> errors = managerProfileService.validateAvatar(avatarFile);
                if (!errors.isEmpty()) {
                    model.addAttribute("manager", manager);
                    model.addAttribute("editMode", true);
                    model.addAttribute("errors", errors);
                    return "managerProfile";
                }

                // Update avatar using service
                Manager updatedManager = managerProfileService.updateAvatar(manager, avatarFile);
                session.setAttribute("manager", updatedManager);
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

    @GetMapping("/avatar/{id}")
    public ResponseEntity<byte[]> getAvatar(@PathVariable int id) {
        Optional<byte[]> avatarData = managerProfileService.getAvatarData(id);
        if (avatarData.isPresent()) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            return new ResponseEntity<>(avatarData.get(), headers, HttpStatus.OK);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}