package hello.springtx.apply;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
public class InternalCallV1Test {

    @Autowired
    CallService callService;

    @Test
    void printProxy() {
        log.info("callService class={}", callService.getClass());
    }

    @Test
    void internalCall() {
        callService.internal();
    }

    @Test
    void externalCall() {
        callService.external();
    }

    @TestConfiguration
    static class InternalCallV1TestConfig {
        @Bean
        public CallService callService() {
            return new CallService();
        }
    }

    @Slf4j
    static class CallService {

        /**
         *   @Transactional 어노테이션이 없다
         *   트랜잭션 프록시는 트랜잭션을 적용하지 않는다
         *   트랜잭션을 적용하지 않고 실제 CallService 객체 인스턴스에 external() 메서드를 호출한다
         *   this 실제 대상 객체의 인스턴스를 뜻한다
         *   자기 내부의 this.internal()  내부호출은 프록시를 거치지 않는다.
         *   따라서 트랜잭션을 적용할 수 없다.
         *   결과적으로 target 에 있는 internal() 을 직접 호출하게 된것이다
         *
         *  프록시 방식의 AOP 한계
         * @Transaction 을 사용하는 트랜잭션 AOP는 프록시를 사용한다. 프록시를 사용하면 메서드 내부 호출에 프록시를 적용할수 없다.
         * internal 메소드를 별도의 클래스로 분리
         */
        public void external() {
            log.info("call external");
            this.printTxInfo();
            this.internal(); // 나 자신을 뜻함
        }

        /**
         * 트랜잭션 프록시는 트랜잭션을 적용한다.
         */
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
