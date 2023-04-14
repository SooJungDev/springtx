package hello.springtx.apply;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class InitTxTest {

    @Autowired
    Hello hello;

    @Test
    void go() {
        // 초기화 코드는 스프링 초기화 시점에 호출한다.
    }

    @TestConfiguration
    static class InitTxTestConfig {
        @Bean
        public Hello hello() {
            return new Hello();
        }
    }

    @Slf4j
    static class Hello {

        /**
         * 초기화 코드에 Transactional 을 함께 사용하면 적용되지 않는다
         * 왜냐면 초기화 코드가 먼저 호출되고 트랜잭션은 나중에 시작되기 때문이다
         */
        @PostConstruct
        @Transactional
        public void initV1() {
            final boolean isActive =
                    TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstruct tx active: {}", isActive);
        }

        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2(){
            final boolean isActive =
                    TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init ApplicationReadyEvent tx active: {}", isActive);
        }

    }
}
