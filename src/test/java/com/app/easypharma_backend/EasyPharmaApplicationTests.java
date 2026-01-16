package com.app.easypharma_backend;

import com.app.easypharma_backend.config.TestMailConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestMailConfiguration.class)
class EasyPharmaApplicationTests {

    @Test
    void contextLoads() {
    }

}
