// service/ManagerProfileService.java
package anbd.he191271.service;

import anbd.he191271.entity.Manager;
import anbd.he191271.repository.ManagerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ManagerProfileService {

    private static final Logger logger = LoggerFactory.getLogger(ManagerProfileService.class);

    private final ManagerRepository managerRepository;

    public ManagerProfileService(ManagerRepository managerRepository) {
        this.managerRepository = managerRepository;
    }

    public List<String> validateProfileData(String name, String phone) {
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
        if (phone == null || phone.trim().isEmpty()) {
            errors.add("Số điện thoại không được để trống");
        } else {
            phone = phone.trim();
            if (!phone.matches("^(\\+84|0)[3|5|7|8|9][0-9]{8}$")) {
                errors.add("Số điện thoại không hợp lệ");
            }
        }

        return errors;
    }

    public List<String> validateAvatar(MultipartFile avatarFile) {
        List<String> errors = new ArrayList<>();

        if (avatarFile == null || avatarFile.isEmpty()) {
            errors.add("File avatar không được để trống");
            return errors;
        }

        // Validate file type
        String contentType = avatarFile.getContentType();
        if (!contentType.startsWith("image/")) {
            errors.add("Chỉ được upload file ảnh");
        }

        // Validate file size (max 5MB)
        if (avatarFile.getSize() > 5 * 1024 * 1024) {
            errors.add("Kích thước ảnh không được vượt quá 5MB");
        }

        return errors;
    }

    public Manager updateProfile(Manager manager, String name, String phone) {
        manager.setName(name.trim());
        manager.setPhone(phone.trim());
        return managerRepository.save(manager);
    }

    public Manager updateAvatar(Manager manager, MultipartFile avatarFile) throws IOException {
        manager.setAvatar(avatarFile.getBytes());
        return managerRepository.save(manager);
    }

    public Optional<byte[]> getAvatarData(Integer managerId) {
        Optional<Manager> optionalManager = managerRepository.findById(managerId);
        if (optionalManager.isPresent() && optionalManager.get().getAvatar() != null) {
            return Optional.of(optionalManager.get().getAvatar());
        }
        return Optional.empty();
    }
}