// service/CustomerProfileService.java
package anbd.he191271.service;

import anbd.he191271.entity.Customer;
import anbd.he191271.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerProfileService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerProfileService.class);

    private final CustomerRepository customerRepository;

    public CustomerProfileService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public List<String> validateProfileData(String name, String dob) {
        List<String> errors = new ArrayList<>();

        // Validate tên
        if (name == null || name.trim().isEmpty()) {
            errors.add("Tên không được để trống");
        } else {
            name = name.trim();
            if (name.length() < 2) {
                errors.add("Tên phải có ít nhất 2 ký tự");
            }
            if (name.length() > 100) {
                errors.add("Tên không được vượt quá 100 ký tự");
            }
        }

        // Validate ngày sinh
        if (dob != null && !dob.isEmpty()) {
            try {
                LocalDate dobDate = LocalDate.parse(dob);
                LocalDate today = LocalDate.now();
                int age = Period.between(dobDate, today).getYears();

                if (age < 12) {
                    errors.add("Bạn phải từ 12 tuổi trở lên");
                } else if (age > 100) {
                    errors.add("Ngày sinh không hợp lệ");
                }

                if (dobDate.isAfter(today)) {
                    errors.add("Ngày sinh không thể ở tương lai");
                }

            } catch (Exception e) {
                errors.add("Định dạng ngày sinh không hợp lệ");
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

    public Customer updateProfile(Customer customer, String name, String dob) {
        customer.setName(name.trim());

        if (dob != null && !dob.isEmpty()) {
            LocalDate dobDate = LocalDate.parse(dob);
            customer.setDob(dobDate);
        }

        return customerRepository.save(customer);
    }

    public Customer updateAvatar(Customer customer, MultipartFile avatarFile) throws IOException {
        customer.setAvatar(avatarFile.getBytes());
        return customerRepository.save(customer);
    }

    public Optional<byte[]> getAvatarData(Integer customerId) {
        Optional<Customer> optionalCustomer = customerRepository.findById(customerId);
        if (optionalCustomer.isPresent() && optionalCustomer.get().getAvatar() != null) {
            return Optional.of(optionalCustomer.get().getAvatar());
        }
        return Optional.empty();
    }
}