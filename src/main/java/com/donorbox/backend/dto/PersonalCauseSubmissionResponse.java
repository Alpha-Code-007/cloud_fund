package com.donorbox.backend.dto;

import com.donorbox.backend.entity.PersonalCauseSubmission;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonalCauseSubmissionResponse {

    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private BigDecimal targetAmount;
    private String imageUrl;
    private String videoUrl;
    private String proofDocumentUrl;
    private String proofDocumentName;
    private String proofDocumentType;
    private String category;
    private String location;
    private LocalDateTime endDate;

    // Submitter information
    private String submitterName;
    private String submitterEmail;
    private String submitterPhone;
    private String submitterMessage;

    // Approval workflow
    private PersonalCauseSubmission.SubmissionStatus status;
    private String adminNotes;
    private String approvedBy;
    private LocalDateTime approvedAt;
    private LocalDateTime rejectedAt;
    private Long causeId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PersonalCauseSubmissionResponse fromEntity(PersonalCauseSubmission submission) {
        return PersonalCauseSubmissionResponse.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .description(submission.getDescription())
                .shortDescription(submission.getShortDescription())
                .targetAmount(submission.getTargetAmount())
                .imageUrl(submission.getImageUrl())
                .videoUrl(submission.getVideoUrl())
                .proofDocumentUrl(submission.getProofDocumentUrl())
                .proofDocumentName(submission.getProofDocumentName())
                .proofDocumentType(submission.getProofDocumentType())
                .category(submission.getCategory())
                .location(submission.getLocation())
                .endDate(submission.getEndDate())
                .submitterName(submission.getSubmitterName())
                .submitterEmail(submission.getSubmitterEmail())
                .submitterPhone(submission.getSubmitterPhone())
                .submitterMessage(submission.getSubmitterMessage())
                .status(submission.getStatus())
                .adminNotes(submission.getAdminNotes())
                .approvedBy(submission.getApprovedBy())
                .approvedAt(submission.getApprovedAt())
                .rejectedAt(submission.getRejectedAt())
                .causeId(submission.getCauseId())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }

    public static PersonalCauseSubmissionResponse summaryFromEntity(PersonalCauseSubmission submission) {
        return PersonalCauseSubmissionResponse.builder()
                .id(submission.getId())
                .title(submission.getTitle())
                .description(submission.getDescription())
                .shortDescription(submission.getShortDescription())
                .targetAmount(submission.getTargetAmount())
                .imageUrl(submission.getImageUrl())
                .videoUrl(submission.getVideoUrl())
                .proofDocumentUrl(submission.getProofDocumentUrl())
                .proofDocumentName(submission.getProofDocumentName())
                .proofDocumentType(submission.getProofDocumentType())
                .category(submission.getCategory())
                .location(submission.getLocation())
                .endDate(submission.getEndDate())
                .submitterName(submission.getSubmitterName())
                .submitterEmail(submission.getSubmitterEmail())
                .submitterPhone(submission.getSubmitterPhone())
                .submitterMessage(submission.getSubmitterMessage())
                .status(submission.getStatus())
                .adminNotes(submission.getAdminNotes())
                .approvedBy(submission.getApprovedBy())
                .approvedAt(submission.getApprovedAt())
                .rejectedAt(submission.getRejectedAt())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .causeId(submission.getCauseId())
                .build();
    }
}
