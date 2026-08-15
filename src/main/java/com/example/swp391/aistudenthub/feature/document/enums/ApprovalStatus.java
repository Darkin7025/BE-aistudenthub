package com.example.swp391.aistudenthub.feature.document.enums;

public enum ApprovalStatus {
    PENDING,          // Tài liệu công khai mới tải lên, đang chờ duyệt
    APPROVED,         // Tài liệu đã duyệt, được phép hiển thị ngoài thư viện cộng đồng
    REJECTED,         // Từ chối phê duyệt do vi phạm quy tắc
    DMCA_TAKEN_DOWN   // Bị gỡ bỏ sau khi đăng do vi phạm bản quyền (DMCA)
}
