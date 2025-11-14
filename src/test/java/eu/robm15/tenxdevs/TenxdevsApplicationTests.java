package eu.robm15.tenxdevs;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.ai.openai.api-key=test-api-key",
	"spring.ai.openai.chat.options.model=gpt-4",
	"spring.ai.openai.chat.options.temperature=0.7"
})
class TenxdevsApplicationTests {

	@Test
	void contextLoads() {
	}

}
