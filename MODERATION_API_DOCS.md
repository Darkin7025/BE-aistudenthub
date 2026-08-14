# Moderation & Report APIs Documentation

## Overview
The moderation system includes two main flows:
1. **Report API** - Users report inappropriate documents
2. **Moderator Dashboard API** - Moderators review and handle reports + document approvals

---

## 1. REPORT API

### 1.1 Create Report (User Action)
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /api/v1/reports` |
| **Authentication** | Required (User/Moderator) |
| **Authorization** | Role: `USER` |
| **Purpose** | Student tạo report cho document đang public |
| **Request Body** | `CreateReportRequest` |
| **Response** | `ApiResponse<ReportResponse>` with HTTP 201 |

#### Request Body (CreateReportRequest)
```json
{
  "documentId": "UUID",
  "reason": "ENUM: INAPPROPRIATE, PLAGIARISM, COPYRIGHT, SPAM, OTHER",
  "description": "string (optional details)"
}
```

#### Response (ReportResponse)
```json
{
  "reportId": "long",
  "documentId": "UUID",
  "documentTitle": "string",
  "reporterId": "UUID",
  "reporterName": "string",
  "reason": "enum",
  "description": "string",
  "status": "ENUM: PENDING, DISMISSED, RESOLVED",
  "reviewedBy": "UUID (nullable)",
  "moderatorName": "string (nullable)",
  "reviewedAt": "timestamp (nullable)",
  "moderatorNote": "string (nullable)",
  "createdAt": "timestamp"
}
```

#### Status Codes
- `201 Created` - Report created successfully
- `400 Bad Request` - Invalid input
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - User role check failed

---

### 1.2 Get Pending Reports (Moderator View)
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /api/v1/reports/pending` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Moderator xem danh sách report đang PENDING |
| **Response** | `ApiResponse<List<ReportResponse>>` |

#### Response
```json
{
  "success": true,
  "data": [
    {
      "reportId": 1,
      "documentId": "UUID",
      "documentTitle": "Java Servlet.pdf",
      "reporterId": "UUID",
      "reporterName": "Nguyễn A",
      "reason": "PLAGIARISM",
      "description": "Nội dung giống hệt bài của tôi",
      "status": "PENDING",
      "reviewedBy": null,
      "moderatorName": null,
      "reviewedAt": null,
      "moderatorNote": null,
      "createdAt": "2025-08-14T10:30:00Z"
    }
  ],
  "message": null
}
```

#### Status Codes
- `200 OK` - Success
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator

---

### 1.3 Review/Process Report (Moderator Action)
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /api/v1/reports/{reportId}/review` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Moderator xử lý report (DISMISSED hoặc RESOLVED) |
| **Path Parameter** | `reportId` (long) |
| **Request Body** | `ReviewReportRequest` |
| **Response** | `ApiResponse<ReportResponse>` with HTTP 200 |

#### Request Body (ReviewReportRequest)
```json
{
  "decision": "ENUM: DISMISSED, RESOLVED",
  "moderatorNote": "string (reason for decision)"
}
```

#### Decision Logic
- **DISMISSED** - Report is invalid, document remains as-is
- **RESOLVED** - Document is inappropriate, visibility set to PRIVATE

#### Response (ReportResponse)
```json
{
  "success": true,
  "data": {
    "reportId": 1,
    "documentId": "UUID",
    "documentTitle": "Java Servlet.pdf",
    "reporterId": "UUID",
    "reporterName": "Nguyễn A",
    "reason": "PLAGIARISM",
    "description": "...",
    "status": "RESOLVED",
    "reviewedBy": "UUID",
    "moderatorName": "Trần Moderator",
    "reviewedAt": "2025-08-14T11:00:00Z",
    "moderatorNote": "Xác nhận nội dung sao chép",
    "createdAt": "2025-08-14T10:30:00Z"
  },
  "message": "Report đã được xử lý"
}
```

#### Status Codes
- `200 OK` - Successfully processed
- `400 Bad Request` - Invalid decision/request
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator
- `404 Not Found` - Report not found

---

## 2. MODERATOR DASHBOARD API

### 2.1 Get Dashboard Statistics
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /api/v1/moderator/dashboard/stats` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Tổng quan thống kê duyệt tài liệu |
| **Response** | `ApiResponse<DashboardStatsDto>` |

#### Response (DashboardStatsDto)
```json
{
  "success": true,
  "data": {
    "pendingDocumentsCount": 15,
    "approvedByCurrentModerator": 42,
    "rejectedByCurrentModerator": 8,
    "totalApprovedByAllModerators": 250,
    "totalRejectedByAllModerators": 30
  },
  "message": null
}
```

#### Status Codes
- `200 OK` - Success
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator

---

### 2.2 Get Pending Documents List
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /api/v1/moderator/dashboard/pending-documents` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Danh sách tài liệu chờ duyệt (visibility = PENDING) |
| **Response** | `ApiResponse<List<PendingDocumentResponseDto>>` |

#### Response (PendingDocumentResponseDto List)
```json
{
  "success": true,
  "data": [
    {
      "documentId": "UUID",
      "title": "Java Servlet.pdf",
      "description": "Tài liệu về Servlet",
      "fileName": "Java Servlet.pdf",
      "fileSize": 2048576,
      "fileType": "application/pdf",
      "uploadedBy": "UUID",
      "uploaderName": "Nguyễn B",
      "subject": "Lập trình Java",
      "major": "CNTT",
      "documentType": "PDF",
      "createdAt": "2025-08-14T09:00:00Z",
      "updatedAt": "2025-08-14T09:00:00Z"
    },
    {
      "documentId": "UUID-2",
      "title": "Spring Boot Guide.pdf",
      "description": "Hướng dẫn Spring Boot",
      "fileName": "Spring Boot Guide.pdf",
      "fileSize": 3145728,
      "fileType": "application/pdf",
      "uploadedBy": "UUID-2",
      "uploaderName": "Trần C",
      "subject": "Framework Java",
      "major": "CNTT",
      "documentType": "PDF",
      "createdAt": "2025-08-14T08:30:00Z",
      "updatedAt": "2025-08-14T08:30:00Z"
    }
  ],
  "message": null
}
```

#### Status Codes
- `200 OK` - Success
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator

---

### 2.3 Approve Document
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /api/v1/moderator/dashboard/approve` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Phê duyệt tài liệu (PENDING → PUBLIC) |
| **Request Body** | `ApproveDocumentRequest` |
| **Response** | `ApiResponse<ModerationResponseDto>` with HTTP 201 |

#### Request Body (ApproveDocumentRequest)
```json
{
  "documentId": "UUID"
}
```

#### Response (ModerationResponseDto)
```json
{
  "success": true,
  "data": {
    "moderationId": "UUID",
    "documentId": "UUID",
    "documentTitle": "Java Servlet.pdf",
    "moderatorId": "UUID",
    "moderatorName": "Trần Moderator",
    "action": "APPROVE",
    "reason": null,
    "createdAt": "2025-08-14T11:30:00Z"
  },
  "message": null
}
```

#### Side Effects
- Document visibility: `PENDING` → `PUBLIC`
- Moderation record created
- Moderator ID recorded

#### Status Codes
- `201 Created` - Document approved successfully
- `400 Bad Request` - Invalid document ID
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator
- `404 Not Found` - Document not found

---

### 2.4 Reject Document
| Field | Value |
|-------|-------|
| **Endpoint** | `POST /api/v1/moderator/dashboard/reject` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Từ chối tài liệu (PENDING → PRIVATE + reason) |
| **Request Body** | `RejectDocumentRequest` |
| **Response** | `ApiResponse<ModerationResponseDto>` with HTTP 201 |

#### Request Body (RejectDocumentRequest)
```json
{
  "documentId": "UUID",
  "reason": "string (reason for rejection)"
}
```

#### Response (ModerationResponseDto)
```json
{
  "success": true,
  "data": {
    "moderationId": "UUID",
    "documentId": "UUID",
    "documentTitle": "Java Servlet.pdf",
    "moderatorId": "UUID",
    "moderatorName": "Trần Moderator",
    "action": "REJECT",
    "reason": "Nội dung không phù hợp chính sách",
    "createdAt": "2025-08-14T11:35:00Z"
  },
  "message": null
}
```

#### Side Effects
- Document visibility: `PENDING` → `PRIVATE`
- Moderation record created with reason
- Moderator ID recorded
- Original uploader cannot make it public again (stays private)

#### Status Codes
- `201 Created` - Document rejected successfully
- `400 Bad Request` - Invalid request/reason missing
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator
- `404 Not Found` - Document not found

---

### 2.5 Get Moderation History
| Field | Value |
|-------|-------|
| **Endpoint** | `GET /api/v1/moderator/dashboard/moderation-history` |
| **Authentication** | Required |
| **Authorization** | Role: `MODERATOR` |
| **Purpose** | Lịch sử duyệt tài liệu của moderator hiện tại |
| **Response** | `ApiResponse<List<ModerationResponseDto>>` |

#### Response (ModerationResponseDto List)
```json
{
  "success": true,
  "data": [
    {
      "moderationId": "UUID-1",
      "documentId": "UUID-101",
      "documentTitle": "Java Servlet.pdf",
      "moderatorId": "UUID",
      "moderatorName": "Trần Moderator",
      "action": "APPROVE",
      "reason": null,
      "createdAt": "2025-08-14T11:30:00Z"
    },
    {
      "moderationId": "UUID-2",
      "documentId": "UUID-102",
      "documentTitle": "Spam Document.pdf",
      "moderatorId": "UUID",
      "moderatorName": "Trần Moderator",
      "action": "REJECT",
      "reason": "Nội dung spam",
      "createdAt": "2025-08-14T11:25:00Z"
    }
  ],
  "message": null
}
```

#### Status Codes
- `200 OK` - Success
- `401 Unauthorized` - Not authenticated
- `403 Forbidden` - Not a moderator

---

## 3. Enums Reference

### ReportReason
```
INAPPROPRIATE    - Nội dung không phù hợp
PLAGIARISM       - Nội dung sao chép
COPYRIGHT        - Vi phạm bản quyền
SPAM             - Nội dung spam
OTHER            - Lý do khác
```

### ReportStatus
```
PENDING   - Chờ xử lý
DISMISSED - Bị từ chối (report không hợp lệ)
RESOLVED  - Đã xử lý (document bị ẩn)
```

### DocumentVisibility
```
PUBLIC   - Công khai (tất cả người dùng có thể xem)
PRIVATE  - Riêng tư (chỉ chủ sở hữu xem)
PENDING  - Chờ duyệt (chưa phê duyệt)
```

### ModerationAction
```
APPROVE - Phê duyệt tài liệu
REJECT  - Từ chối tài liệu
```

---

## 4. Workflow Diagram

### Report Workflow
```
User                          Moderator
  |                                |
  | 1. POST /api/v1/reports       |
  |----Create Report (document)---|
  |                          Report Status = PENDING
  |                                |
  |                 2. GET /api/v1/reports/pending
  |                 3. View pending reports
  |                                |
  |        4. POST /api/v1/reports/{id}/review
  |        5. DISMISSED or RESOLVED
  |                                |
  |<--Report Status Updated--------|
  | Document visibility updated
  |
```

### Document Moderation Workflow
```
User                          Moderator
  |                                |
  | 1. Upload Document             |
  |    visibility = PENDING         |
  |                                |
  |            2. GET /api/v1/moderator/dashboard/pending-documents
  |            3. View all pending documents
  |                                |
  |     4a. POST .../approve       | or | 4b. POST .../reject
  |     Document → PUBLIC          |    | Document → PRIVATE
  |                                |
  |<--Document Status Updated------|
  |                                |
```

---

## 5. Security & Access Control

### Authentication
All endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt-token>
```

### Authorization
- **USER Role**: Can create reports on public documents
- **MODERATOR Role**: Can view/process reports and approve/reject documents
- **ADMIN Role**: Full access (inherits all permissions)

### Audit Trail
- All moderation actions are recorded with:
  - Moderator ID
  - Timestamp
  - Action (APPROVE/REJECT)
  - Reason (for rejection)

---

## 6. Error Response Format

```json
{
  "success": false,
  "data": null,
  "message": "Error description"
}
```

### Common HTTP Status Codes
| Code | Meaning |
|------|---------|
| 200 | OK |
| 201 | Created |
| 400 | Bad Request (validation error) |
| 401 | Unauthorized (missing/invalid token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found (resource doesn't exist) |
| 500 | Internal Server Error |

---

## 7. Database Entities

### Report Entity
- `id` (Long): Primary key
- `document` (Document): Foreign key to document
- `reporter` (User): Foreign key to reporter user
- `reason` (ReportReason Enum): Reason for report
- `description` (String): Additional details
- `status` (ReportStatus Enum): PENDING, DISMISSED, RESOLVED
- `reviewedBy` (User): Moderator who reviewed (nullable)
- `reviewedAt` (OffsetDateTime): Review timestamp (nullable)
- `moderatorNote` (String): Moderator's decision note (nullable)
- `createdAt` (OffsetDateTime): Creation timestamp

### Moderation Entity
- `moderationId` (UUID): Primary key
- `document` (Document): Foreign key to document
- `moderator` (User): Foreign key to moderator
- `action` (ModerationAction Enum): APPROVE or REJECT
- `reason` (String): Reason for action (nullable for approval)
- `createdAt` (OffsetDateTime): Action timestamp

---

## 8. Example cURL Commands

### Create Report
```bash
curl -X POST http://localhost:8080/api/v1/reports \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "reason": "PLAGIARISM",
    "description": "This content is copied from another source"
  }'
```

### Get Pending Reports
```bash
curl -X GET http://localhost:8080/api/v1/reports/pending \
  -H "Authorization: Bearer <token>"
```

### Review Report
```bash
curl -X POST http://localhost:8080/api/v1/reports/1/review \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "decision": "RESOLVED",
    "moderatorNote": "Content verified as plagiarized, document hidden"
  }'
```

### Get Pending Documents
```bash
curl -X GET http://localhost:8080/api/v1/moderator/dashboard/pending-documents \
  -H "Authorization: Bearer <token>"
```

### Approve Document
```bash
curl -X POST http://localhost:8080/api/v1/moderator/dashboard/approve \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "550e8400-e29b-41d4-a716-446655440000"
  }'
```

### Reject Document
```bash
curl -X POST http://localhost:8080/api/v1/moderator/dashboard/reject \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "documentId": "550e8400-e29b-41d4-a716-446655440000",
    "reason": "Document violates content policy"
  }'
```

---

## 9. Testing Notes

### Test Credentials
- User Email: `user@example.com` / Role: USER
- Moderator Email: `moderator@example.com` / Role: MODERATOR

### Test Flow
1. Login as User → Create Report on public document
2. Verify report status = PENDING
3. Login as Moderator → Get pending reports
4. Process report (DISMISSED or RESOLVED)
5. Verify document visibility changed (if RESOLVED)

---

**Last Updated**: 2025-08-14  
**API Version**: v1  
**Status**: Active
