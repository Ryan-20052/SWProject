package anbd.he191271.dto;

import java.time.LocalDateTime;

public record AdminReportDTO(
        Long id,
        String reporterUsername,
        String reportedUsername,
        String reportReason,
        String description,
        String status,
        LocalDateTime createdAt
) {}