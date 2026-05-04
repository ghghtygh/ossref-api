package com.ossref.batch;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
class OssrefBatchApplicationTest {

    @Test
    @DisplayName("Spring 컨텍스트가 정상적으로 로드된다")
    void contextLoads() {
    }
}
