package com.example.swp391.aistudenthub;

import com.example.swp391.aistudenthub.feature.auth.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AistudenthubApplicationTests {

	@org.springframework.test.context.DynamicPropertySource
	static void registerEnvProperties(org.springframework.test.context.DynamicPropertyRegistry registry) {
		java.util.Map<String, Object> envProperties = AistudenthubApplication.loadEnvMap();
		envProperties.forEach((key, value) -> registry.add(key, () -> value));
	}

	@Autowired
	private EmailService emailService;

	@Test
	void contextLoads() {
	}

	@Test
	@Disabled("Requires explicit SMTP credentials and an intentional external email recipient")
	void testSendEmail() throws InterruptedException {
		// Thay đổi email nhận bên dưới để test thực tế
		String testRecipient = "cuongntse172349@fpt.edu.vn";
		System.out.println("Đang gửi mail thử nghiệm tới: " + testRecipient);

		emailService.sendPasswordResetEmail(testRecipient, "test-token-abcdef123456");

		// Đợi 5 giây vì EmailService gửi bất đồng bộ (@Async)
		System.out.println("Đang đợi mail gửi đi...");
		Thread.sleep(5000);
		System.out.println("Hoàn thành test gửi mail!");
	}
}
