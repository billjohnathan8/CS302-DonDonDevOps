package org.ddk.promotions;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("it")
@Tag("integration")
@SpringBootTest
class PromotionsApplicationTests {
	@Test
	void contextLoads() {
	}
}
