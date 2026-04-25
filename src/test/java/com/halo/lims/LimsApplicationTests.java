package com.halo.lims;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.cloud.gcp.sql.enabled=false",
	"spring.cloud.gcp.secretmanager.enabled=false",
	"spring.cloud.gcp.storage.enabled=false"
})
class LimsApplicationTests {

	@Test
	void contextLoads() {
	}

}
