package com.company.cms.domain.content;

import com.company.cms.domain.common.CmsEnums.AttachmentValidationStatus;
import com.company.cms.domain.common.CmsEnums.UploadStatus;
import java.time.Instant;
import java.util.UUID;

public class Attachment {
    private final String id;
    private final String contentItemId;
    private final String fileName;
    private final String mimeType;
    private final long sizeBytes;
    private final String checksum;
    private final String storageReference;
    private final UploadStatus uploadStatus;
    private final AttachmentValidationStatus validationStatus;
    private final String validationMessage;
    private final String uploadedBy;
    private final Instant uploadedAt;

    public Attachment(
        String contentItemId,
        String fileName,
        String mimeType,
        long sizeBytes,
        String checksum,
        String uploadedBy,
        AttachmentValidationStatus validationStatus,
        String validationMessage
    ) {
        this.id = UUID.randomUUID().toString();
        this.contentItemId = contentItemId;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.sizeBytes = sizeBytes;
        this.checksum = checksum;
        this.storageReference = "cms://" + contentItemId + "/" + id;
        this.uploadStatus = UploadStatus.UPLOADED;
        this.validationStatus = validationStatus;
        this.validationMessage = validationMessage;
        this.uploadedBy = uploadedBy;
        this.uploadedAt = Instant.now();
    }

    public boolean isDownloadable() {
        return uploadStatus == UploadStatus.UPLOADED && validationStatus == AttachmentValidationStatus.ACCEPTED;
    }

    public String getId() {
        return id;
    }

    public String getContentItemId() {
        return contentItemId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getChecksum() {
        return checksum;
    }

    public String getStorageReference() {
        return storageReference;
    }

    public UploadStatus getUploadStatus() {
        return uploadStatus;
    }

    public AttachmentValidationStatus getValidationStatus() {
        return validationStatus;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }
}
