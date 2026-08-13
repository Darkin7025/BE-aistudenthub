# Phân tích Yêu cầu Backend (BE) - AI Student Hub

Tài liệu này tổng hợp, làm rõ và phân loại các yêu cầu từ phía người dùng cho hệ thống **AI Student Hub**, tập trung chi tiết vào các nhiệm vụ của **Backend (BE)**, bao gồm cấu trúc database, logic xử lý, thiết kế API và giải pháp tối ưu hiệu năng.

---

## 1. Bảng phân loại Yêu cầu (BE vs FE)

| STT | Yêu cầu người dùng | Phân loại | Mô tả kỹ thuật phía Backend (BE) |
|---|---|---|---|
| **1** | Thêm 1 role mới để duyệt tài liệu (Teacher/Moderator) | **BE** | Cập nhật `Role` enum, cấu trúc phân quyền bảo mật cấp API (Method Security). |
| **2** | Duyệt tài liệu tuân thủ DMCA trước khi đăng | **BE** | Thêm trạng thái duyệt (`approvalStatus`), chặn hiển thị tài liệu `PUBLIC` nếu chưa được duyệt. |
| **3** | Thêm quy trình/chứng chỉ duyệt kiểu DMCA | **BE** | Thêm các trường xác thực bản quyền (`dmcaVerified`, `dmcaVerifiedAt`, `dmcaVerifiedBy`). |
| **4** | Thêm chức năng Report tài liệu Community gửi Admin | **BE** | Thiết kế bảng `document_reports`, tạo API report cho user và API quản lý report cho Admin. |
| **5** | Trả lời: AI có chunking lại khi upload/update không? | **BE** | Trả lời dựa trên mã nguồn: **Có**, hệ thống tự động chia nhỏ khi upload và tự động xây dựng lại chunk khi sửa đổi văn bản để tránh dữ liệu stale. |
| **6** | Trả lời: AI chunking có cập nhật version không? | **BE** | Trả lời: **Hiện tại chưa có versioning**. Cần đề xuất giải pháp thêm trường `version` vào tài liệu. |
| **7** | Cắt tài liệu theo headline/đầu mục | **BE** | Phát triển chiến lược chunking mới (`HEADLINE_ROMAN`) phân tách dựa trên đầu mục chính. |
| **8** | Đoạn sau phải overlap một phần đoạn trước | **BE** | Đảm bảo logic trượt (sliding window) giữ cấu trúc overlap trong thuật toán cắt chunk mới. |
| **9** | Cắt theo số La Mã (I, II, III...) và mục (a, b, c...) | **BE** | Áp dụng Regex để định vị các ký tự La Mã và ký tự chữ cái làm ranh giới cắt chunk. |
| **10** | Đọc/cắt đúng thứ tự trong tài liệu | **BE** | Quản lý index (`chunkIndex`) tăng dần từ đầu đến cuối văn bản một cách tuần tự. |
| **11** | Hỏi AI đúng 1 tài liệu đã chọn - không trộn file khác | **BE** | Đảm bảo context query chỉ giới hạn trong ID tài liệu được truyền lên, cấu hình prompt để AI từ chối suy diễn ngoài tài liệu. |
| **12** | Thêm thông báo email lúc chia sẻ thành công | **BE** | Tích hợp Brevo Mail API để gửi email bất đồng bộ khi thực hiện chia sẻ quyền truy cập. |
| **13** | Phải có đầy đủ thông tin khi chia sẻ | **BE** | Cung cấp thông tin chi tiết người gửi, thông tin tài liệu, link xem trực tiếp trong nội dung Email. |
| **14** | Điều tra và xử lý upload file chậm | **BE** | Đề xuất giải pháp **Direct Upload từ Client** qua Signed Signature, tránh double-hop upload tại server. |
| **15** | Điều tra và xử lý load data chậm | **BE** | Phân tích vấn đề quét bảng (`LIKE '%extractedText%'`), đề xuất Indexing và database Full-Text Search. |
| **16** | Community: Lọc file đã tải | **BE** | Thiết kế lưu lịch sử download và API lọc tài liệu hiện tại user đã tải. |
| **17** | Community: Lọc theo tên | **BE** & FE | Hỗ trợ tìm kiếm không dấu, khớp tên tài liệu chính xác qua câu lệnh query. |
| **18** | Community: Lọc theo số lượt tải | **BE** | Thêm cột `downloadCount` vào bảng `documents` phục vụ sắp xếp tốc độ cao. |
| **19** | Community: Xem tài liệu dạng popup | FE | FE xử lý giao diện popup. BE hỗ trợ API lấy chi tiết tài liệu nhanh gọn. |
| **20** | Admin: Quản lý gói thanh toán | **BE** | Thiết kế bảng `pricing_plans`, thay thế việc hardcode số tiền cước trong code Java bằng dữ liệu động. |
| **21** | Admin: Mọi màn hình đều có filter và sort | **BE** | Nâng cấp các API Admin hiện tại để nhận các tham số filter/sort và phân trang động. |
| **22** | Admin: Lọc/tìm ngay trên bảng (query filter) | **BE** | Viết câu lệnh SQL/JPA Specification linh hoạt cho bảng dữ liệu Admin. |
| **23** | Admin: Popup chi tiết tài liệu trong bảng | FE | FE hiển thị popup. BE cung cấp API chi tiết tương ứng. |
| **24** | Admin detail: Mô tả, người đăng, người tải liên quan | **BE** | Cập nhật API chi tiết tài liệu trả về thêm danh sách lịch sử những người dùng đã tải. |
| **25** | Admin Dashboard: Doanh thu | **BE** | API tính tổng doanh thu và xu hướng doanh thu theo mốc thời gian. |
| **26** | Admin Dashboard: Số giao dịch | **BE** | API đếm tổng số giao dịch thanh toán thành công/thất bại. |
| **27** | Admin Dashboard: Số lượng gói Student | **BE** | API đếm số gói cước cụ thể đang active của nhóm người dùng Student. |
| **28** | Admin Dashboard: Báo cáo tổng hợp, uploader | **BE** | API thống kê bảng xếp hạng người dùng đóng góp tài liệu (Uploader) có tương tác cao nhất. |
| **29** | User: Thống kê toàn bộ tài liệu của mình | **BE** | API thống kê tổng quan cá nhân: số file upload, dung lượng sử dụng, số lượt tải nhận được. |
| **30** | User: Xem thông tin người tải / chỉ số liên quan | **BE** | API trả về danh sách lịch sử tải của những người dùng khác trên tài liệu của mình. |

---

## 2. Thiết kế chi tiết các yêu cầu Backend (BE)

### 2.1. Phân quyền & Quy trình duyệt tài liệu tuân thủ DMCA

#### Cập nhật Database
1. **Thêm Role mới:** Thêm giá trị `TEACHER` và `MODERATOR` vào `Role` enum trong ứng dụng.
2. **Thêm trạng thái Duyệt tài liệu:**
   - Tạo enum `ApprovalStatus` gồm các trạng thái:
     - `PENDING`: Tài liệu mới được upload (chế độ Public) đang đợi duyệt.
     - `APPROVED`: Đã duyệt đăng thành công, hiển thị lên thư viện cộng đồng.
     - `REJECTED`: Từ chối duyệt do vi phạm quy tắc nội dung.
     - `DMCA_TAKEN_DOWN`: Bị gỡ bỏ sau khi đăng do vi phạm bản quyền (DMCA).
   - Thêm cột `approval_status` (`VARCHAR`) vào bảng `documents` (mặc định là `APPROVED` nếu tài liệu để chế độ `PRIVATE`, mặc định là `PENDING` nếu tài liệu để chế độ `PUBLIC`).
3. **Chứng chỉ kiểm duyệt DMCA:**
   - Thêm các trường sau vào bảng `documents`:
     - `dmca_verified` (`BIT`): Đánh dấu tài liệu đã được kiểm duyệt bản quyền.
     - `dmca_verified_at` (`DATETIMEOFFSET`): Thời điểm duyệt bản quyền.
     - `dmca_verified_by` (`UNIQUEIDENTIFIER`): ID của Moderator/Admin thực hiện kiểm duyệt.

#### Thiết kế APIs Kiểm Duyệt
- **`GET /api/v1/moderator/documents/pending`**
  - *Quyền:* `ADMIN`, `MODERATOR`, `TEACHER`
  - *Chức năng:* Lấy danh sách phân trang các tài liệu công khai đang chờ duyệt.
- **`POST /api/v1/moderator/documents/{id}/approve`**
  - *Quyền:* `ADMIN`, `MODERATOR`, `TEACHER`
  - *Chức năng:* Phê duyệt tài liệu công khai. Chuyển `approvalStatus = APPROVED` và thiết lập `dmca_verified = true`.
- **`POST /api/v1/moderator/documents/{id}/reject`**
  - *Quyền:* `ADMIN`, `MODERATOR`, `TEACHER`
  - *Chức năng:* Từ chối tài liệu kèm lý do (lưu vào trường lý do từ chối). Chuyển `approvalStatus = REJECTED`.
- **`POST /api/v1/moderator/documents/{id}/dmca-takedown`**
  - *Quyền:* `ADMIN`, `MODERATOR`
  - *Chức năng:* Buộc gỡ bỏ tài liệu công cộng do vi phạm bản quyền. Chuyển trạng thái tài liệu sang `DMCA_TAKEN_DOWN` và xóa các chunk dữ liệu AI để không cho hỏi đáp nữa.

> [!IMPORTANT]
> **Ràng buộc hiển thị:** Cập nhật câu truy vấn lấy danh sách tài liệu công khai ngoài trang Community: Chỉ hiển thị tài liệu có `visibility = PUBLIC` và `approvalStatus = APPROVED`.

---

### 2.2. Tính năng Report tài liệu gửi Admin

#### Cập nhật Database
Tạo bảng mới `document_reports`:
- `id` (`UNIQUEIDENTIFIER` PRIMARY KEY)
- `document_id` (`UNIQUEIDENTIFIER` FOREIGN KEY references `documents`)
- `reporter_user_id` (`UNIQUEIDENTIFIER` FOREIGN KEY references `users`)
- `reason` (`VARCHAR(100)`): Phân loại lý do (vd: `COPYRIGHT_VIOLATION` - Vi phạm DMCA, `INAPPROPRIATE_CONTENT` - Nội dung không phù hợp, `SPAM`, `OTHER`).
- `description` (`NVARCHAR(MAX)`): Mô tả chi tiết hành vi vi phạm từ người báo cáo.
- `status` (`VARCHAR(50)`): Trạng thái xử lý báo cáo (`PENDING`, `RESOLVED`, `DISMISSED`).
- `created_at` (`DATETIMEOFFSET`)
- `updated_at` (`DATETIMEOFFSET`)
- `resolved_by` (`UNIQUEIDENTIFIER`)

#### Thiết kế APIs Report
- **`POST /api/v1/documents/{id}/report`**
  - *Quyền:* Mọi user đã đăng nhập.
  - *Chức năng:* Gửi báo cáo vi phạm cho một tài liệu công khai.
- **`GET /api/v1/admin/reports`**
  - *Quyền:* `ADMIN`, `MODERATOR`
  - *Chức năng:* Xem danh sách phân trang các báo cáo tài liệu. Hỗ trợ lọc theo trạng thái báo cáo (`status`) hoặc lý do (`reason`).
- **`PUT /api/v1/admin/reports/{reportId}/resolve`**
  - *Quyền:* `ADMIN`, `MODERATOR`
  - *Chức năng:* Đóng báo cáo. Cho phép chọn hành động: Bác bỏ báo cáo (`DISMISSED`) hoặc chấp nhận và gỡ tài liệu vi phạm bản quyền (`DMCA_TAKEN_DOWN`).

---

### 2.3. Cơ chế Chunking / Cắt nhỏ tài liệu theo quy tắc đặc thù

#### Phản hồi câu hỏi về RAG Pipeline hiện tại
1. **Upload tài liệu mới:** AI **có** tự động cắt nhỏ tài liệu thành các chunk (kích thước ~800 từ, overlap 150 từ) ngay sau khi file upload thành công và được xử lý trong nền (`DocumentProcessor.processDocumentText`). Các chunk này được ghi nhận trực tiếp vào bảng `document_chunks`.
2. **Cập nhật tài liệu:** Khi chỉnh sửa tài liệu hoặc cập nhật nội dung văn bản qua API, hệ thống **có** tự động xóa toàn bộ chunk cũ và sinh lại chunk mới tương ứng (`documentProcessor.reindexChunks`), đảm bảo AI không trả lời dựa trên nội dung cũ.
3. **Cập nhật Version khi Chunking:** Hiện tại hệ thống **chưa** lưu trữ lịch sử hoặc phiên bản (version) của tài liệu và chunk. 
   - *Đề xuất:* Thêm trường `version` (`INT` mặc định là 1) vào bảng `documents`. Mỗi khi cập nhật nội dung văn bản tài liệu thành công, tăng giá trị `version` lên 1 để theo dõi và hỗ trợ khôi phục lịch sử nếu cần.

#### Triển khai Thuật toán Chunking Mới (`HEADLINE_ROMAN`)
Yêu cầu thuật toán cắt nhỏ tài liệu theo đề mục:
1. **Cắt tài liệu theo headline/đầu mục:** Phân tích nội dung văn bản từ trên xuống dưới, sử dụng ký tự phân cách đầu mục lớn.
2. **Cắt theo số La Mã (I, II, III...) và danh mục (a, b, c...):** 
   - Sử dụng Regex để phát hiện các dòng bắt đầu bằng ký tự phân cấp số La Mã như `^[I|V|X|L|C|D|M]+\.` (vd: `I.`, `II.`) hoặc các danh mục con phụ `^[a-z]\.` hoặc `^[a-z]\)` (vd: `a.`, `b)`).
   - Khi phát hiện ranh giới này, dừng chunk hiện tại và bắt đầu chunk mới.
3. **Đoạn sau phải overlap một phần đoạn trước:** Lấy khoảng 100 - 150 từ cuối của chunk trước đó ghép vào phần đầu của chunk tiếp theo để tránh đứt gãy ngữ cảnh thông tin ở vị trí phân tách đề mục.
4. **Đọc/cắt đúng thứ tự:** Sắp xếp lưu trữ tuần tự với `chunk_index` tăng dần để lúc RAG tái cấu trúc nội dung ngữ cảnh chính xác.
5. **Hỏi AI đúng 1 tài liệu:** Khi chat theo tài liệu (`POST /api/v1/chat/document/{id}`), ngữ cảnh gửi vào prompt của Gemini chỉ lấy từ các chunk thuộc đúng `documentId` đó (trong phương thức `retrieveRelevantContext`). Thắt chặt system prompt gửi cho Gemini: *"Chỉ sử dụng thông tin từ tài liệu được cung cấp. Nếu tài liệu không chứa câu trả lời, hãy trả lời chính xác: 'Tài liệu không chứa thông tin này', tuyệt đối không được tự suy diễn."*

---

### 2.4. Gửi email thông báo chia sẻ tài liệu thành công

#### Quy trình Xử lý
Khi một người dùng chia sẻ tài liệu thành công (`POST /api/v1/documents/{id}/share`):
1. Backend ghi nhận dữ liệu chia sẻ vào bảng `document_shares`.
2. Backend kích hoạt tiến trình gửi thư bất đồng bộ (`@Async`) qua `EmailService`.
3. Email được gửi đến `targetEmail` chứa đầy đủ thông tin:
   - **Tên người chia sẻ:** Lấy từ thông tin người dùng đang đăng nhập (`currentUser.fullName` / `currentUser.email`).
   - **Chi tiết tài liệu:** Tên tài liệu, loại tài liệu, mô tả sơ lược.
   - **Quyền hạn chia sẻ:** Quyền xem (`VIEW`) hoặc quyền chỉnh sửa/duyệt.
   - **Link truy cập:** Đường dẫn URL trực tiếp dẫn tới màn hình xem tài liệu (Frontend URL, ví dụ: `https://aistudenthub.com/documents/view/{documentId}`).

---

### 2.5. Thống kê & Quản lý lượt tải (Download Tracking)

#### Cập nhật Database
Tạo bảng mới `document_downloads` để ghi nhận lịch sử tải:
- `id` (`UNIQUEIDENTIFIER` PRIMARY KEY)
- `document_id` (`UNIQUEIDENTIFIER` FOREIGN KEY references `documents`)
- `user_id` (`UNIQUEIDENTIFIER` FOREIGN KEY references `users`) - Người thực hiện tải tài liệu.
- `downloaded_at` (`DATETIMEOFFSET`)

Thêm cột tối ưu vào bảng `documents`:
- `download_count` (`INT` mặc định là 0) để phục vụ việc lọc và sắp xếp nhanh ngoài trang Community.

#### Cập nhật Logic & APIs
1. Khi người dùng gọi API tải tài liệu (`GET /api/v1/documents/{id}/download`) hoặc stream tài liệu thành công:
   - Thêm bản ghi mới vào bảng `document_downloads`.
   - Tăng giá trị cột `download_count` của tài liệu lên 1 đơn vị.
2. **Community Filters:**
   - Lọc các tài liệu đã tải: `GET /api/v1/documents/public?downloadedOnly=true` -> Query tìm các documents có liên kết trong bảng `document_downloads` khớp với `userId` hiện tại.
   - Lọc theo số lượt tải: Thêm tùy chọn sort `sort=downloadCount,desc`.
3. **User Statistics:**
   - API thống kê tài liệu cá nhân: Đếm tổng số tài liệu của user, tổng số lượt tải nhận được (sum `download_count` của các tài liệu thuộc sở hữu của user đó).
   - API chi tiết người tải: Trả về danh sách thông tin người tải tài liệu của mình (FullName, Email, thời gian tải).
4. **Admin Document Detail:**
   - Trả về danh sách chi tiết những người dùng đã từng tải tài liệu này.

---

### 2.6. Admin: Quản lý gói thanh toán (Pricing Plans)

#### Hiện trạng
Hệ thống đang kiểm tra gói cước cứng (hardcode) theo mệnh giá tiền thanh toán tại `ChatServiceImpl` (ví dụ: cước >= 39k là gói Nâng cao, >= 79k là gói Chuyên gia). Điều này gây khó khăn khi cần thay đổi giá gói hoặc quyền lợi.

#### Giải pháp
Thiết kế bảng cấu hình động `pricing_plans`:
- `id` (`UNIQUEIDENTIFIER` PRIMARY KEY)
- `name` (`VARCHAR(100)`): Tên gói (vd: `STUDENT`, `TEACHER`, `PRO`).
- `price` (`INT`): Giá gói cước (VND).
- `duration_months` (`INT`): Thời hạn sử dụng (ví dụ: 1 tháng, 6 tháng).
- `ai_daily_limit` (`INT`): Giới hạn câu hỏi AI mỗi ngày.
- `document_limit` (`INT`): Giới hạn số lượng tài liệu được lưu trữ.
- `description` (`NVARCHAR(500)`): Mô tả chi tiết quyền lợi gói.
- `active` (`BIT`): Trạng thái kích hoạt gói cước.

#### Cập nhật Logic
- Trong bảng `payment_orders`, thêm cột `plan_id` liên kết tới `pricing_plans`.
- Thay đổi logic kiểm tra giới hạn trong `ChatServiceImpl` và `DocumentService`: Truy vấn gói cước đang hoạt động của người dùng để lấy `ai_daily_limit` và `document_limit` thực tế từ database thay vì so sánh cứng số tiền `amount`.
- Viết API CRUD dành cho Admin để quản lý các gói cước này (`/api/v1/admin/pricing-plans`).

---

### 2.7. Admin Dashboard: Mở rộng các chỉ số kinh doanh

Cập nhật API `/api/v1/admin/dashboard/business-stats` và thêm các endpoints thống kê khác để trả về:
1. **Doanh thu:** Chi tiết doanh thu theo mốc thời gian lọc (hôm nay, tháng này, năm nay, khoảng ngày tùy chọn).
2. **Số lượng giao dịch:** Tổng số đơn đặt hàng thanh toán và phân chia trạng thái (`PAID`, `PENDING`, `CANCELLED`).
3. **Số lượng gói Student:** Số lượng gói cước Student/Pro đang hoạt động thực tế.
4. **Báo cáo tổng hợp Uploader (Người bán):** Thống kê danh sách những người đóng góp (upload tài liệu public) nhiều nhất, số lượt tài liệu được tải, và doanh thu ước tính đem lại cho hệ thống từ tài liệu của họ.

---

### 2.8. Điều tra & Tối ưu hóa hiệu năng (Chậm upload, Chậm load data)

#### A. Khắc phục Upload file chậm
- **Nguyên nhân:** File được truyền qua trung gian Backend (Browser -> BE Server -> Cloudinary). BE phải lưu file tạm vào ổ đĩa cứng của server rồi mới tải lên Cloudinary, điều này chiếm dụng dung lượng đĩa, băng thông mạng gấp đôi và chặn luồng xử lý HTTP trong thời gian dài.
- **Giải pháp xử lý (Client-Side Direct Upload):**
  1. Backend cung cấp API: `GET /api/v1/documents/upload-signature`
     - Sinh và trả về signed signature từ Cloudinary API Key & Secret Key kèm theo các thông số cấu hình (`folder`, `timestamp`, `public_id`).
  2. Frontend nhận chữ ký và thực hiện upload trực tiếp file từ trình duyệt lên Cloudinary.
  3. Sau khi upload thành công lên Cloudinary, Frontend nhận URL của file và gửi API tạo tài liệu bình thường về Backend (`POST /api/v1/documents`) kèm theo URL và metadata có sẵn. Backend chỉ lưu DB và kích hoạt tiến trình tách chunk AI ngầm, không còn phải download hay upload file trực tiếp nữa.

#### B. Khắc phục Load data chậm
- **Nguyên nhân:** Câu lệnh truy vấn lọc tài liệu hiện tại sử dụng điều kiện `LIKE '%keyword%'` trên cột dữ liệu lớn `extracted_text` (dung lượng văn bản lưu có thể lên tới 500KB mỗi tài liệu). Điều này ngăn cản việc sử dụng các chỉ mục thông thường và ép hệ quản trị cơ sở dữ liệu thực hiện quét toàn bộ bảng (Full Table Scan) cực kỳ tốn tài nguyên đĩa và CPU khi số lượng bản ghi tăng lên.
- **Giải pháp xử lý:**
  1. **Indexing:** Thêm chỉ mục (Index) trên các cột bộ lọc phổ biến trong bảng `documents`: `user_id`, `visibility`, `deleted_at`, `subject`, `major`, `document_type`, `upload_status`.
  2. **Tách biệt tìm kiếm nội dung văn bản:**
     - Loại bỏ mệnh đề `LIKE %extractedText%` trong các câu lệnh query lấy danh sách tài liệu tổng quát hoặc các bảng dữ liệu Admin.
     - Với tính năng tìm kiếm sâu vào nội dung file: Chuyển sang sử dụng công cụ **Full-Text Search (FTS)** tích hợp của database (ví dụ: Full-Text Index trên SQL Server) thông qua các từ khóa `CONTAINS` hoặc `FREETEXT`, tránh sử dụng mệnh đề `LIKE` trên các trường văn bản lớn.
