package hello.springtx.apply;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class InternalCallV2Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void externalCallV2() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        public CallService callService() {
            return new CallService(InternalService());
        }

        @Bean
        public InternalService InternalService() {
            return new InternalService();
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class CallService {

        private final InternalService internalService;

        public void external() {
            log.info("call external");
            this.printTxInfo();
            internalService.internal(); // 외부호출
        }

        private void printTxInfo() {
            final boolean txActive =
                    TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active: {}", txActive);
        }
    }

    /**
     * 별도의 클래스로 분리
     * 합리적인 방법
     * 고급편에서 보기!
     */
    static class InternalService {
        @Transactional
        public void internal() {
            log.info("call internal");
            printTxInfo();
        }

        private void printTxInfo() {
            final boolean txActive =
                    TransactionSynchronizationManager.isActualTransactionActive();
            log.info("tx active: {}", txActive);
        }

    }
}
