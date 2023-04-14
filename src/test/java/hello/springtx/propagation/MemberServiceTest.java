package hello.springtx.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * User : Soo Jung Choi (crystal2840@neowiz.com)
 * Date : 2023.04.14
 * Time : 5:57 PM
 */
@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;


    /**
     * memberService @Transaction:OFF
     * memberRepository @Transaction:ON
     * logRepository @Transaction:ON
     */
    @Test
    void outerTxOff_success() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then 모든 데이터가 정상 저장된다
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }


    /**
     * memberService @Transaction:OFF
     * memberRepository @Transaction:ON
     * logRepository @Transaction:ON Exception 발생
     */
    @Test
    void outerTxOff_fail() {
        // given
        String username = "로그예외_outerTxOff_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);


        // then 모든 데이터가 정상 저장된다
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }



    /**
     * memberService @Transaction:ON
     * memberRepository @Transaction:OFF
     * logRepository @Transaction:OFF
     */
    @Test
    void singleTx() {
        // given
        String username = "outerTxOff_success";

        // when
        memberService.joinV1(username);

        // then 모든 데이터가 정상 저장된다
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transaction:ON
     * memberRepository @Transaction:ON
     * logRepository @Transaction:ON
     */
    @Test
    void outerTxOn_success() {
        // given
        String username = "outerTxOn_success";

        // when
        memberService.joinV1(username);

        // then 모든 데이터가 정상 저장된다
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService @Transaction:ON
     * memberRepository @Transaction:ON
     * logRepository @Transaction:ON Exception 발생
     *
     * 트랜잭션 매니저에 rollback-only 로 마크를 함
     * 서비스에 있는데까지 와서 예외를 발생
     * 런타임 익셉션 서비스까지옴 신규트랜잭션이기 때문에 물리 롤백 호출하게됨
     */
    @Test
    void outerTxOn_fail() {
        // given
        String username = "로그예외_outerTxOn_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);


        // then 모든 데이터가 롤백된다
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * rollbackOnly = true 마크된 상태임
     * try-catch 로 잡아서 정상 흐름으로 바꾼후 커밋을 호출함
     * rollbackOnly 를 체크한다 실제 물리트랜잭션을 롤백처리를 한다.
     * 트랜잭션 매니저가 UnexpectedRollbackException 을 발생시킴
     *
     * 논리 트랜잭션중 하나라도 롤백되면 전체 트랜잭션을 롤백된다.
     * 내부 트랜잭션이 롤백 되었는데 외부 트랜잭션이 커밋되면 UnexpectedRollbackException 예외가 발생
     * rollbackOnly 상황에서 커밋이 발생하면 UnexpectedRollbackException 예외가 발생한다.
     *
     * memberService @Transaction:ON
     * memberRepository @Transaction:ON
     * logRepository @Transaction:ON Exception 발생
     */
    @Test
    void recoverException_fail() {
        // given
        String username = "로그예외_recoverException_fail";

        // when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);


        // then 모든 데이터가 롤백된다
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }


    /**
     * 항상 새로운 트랜잭션을 만든다 따라서 해당 트랜잭션 안에서 DB 커넥션도 별도로 사용하게된다.
     * 신규 트랜잭션으로 rollbackOnly 가 표시되지 않는다. 해당 트랜잭션이 물리 롤백되고 끝난다.
     *
     * 커넥션 1번은 잠시 대기 커낵션 2번 예외 발생하고 롤백하고 끝남
     * 예외는 전달된다 try-catch 로 예외를 잡고 정상 호출로 전환함
     * 커넥션 1번은 커밋을 정상적으로한다. (rollbackOnly 가 표시되지 않았기 때문에)
     *
     * 논리 트랜잭션은 하나라도 롤백되면 관련 물리 트랜잭션은 롤백되어 버린다
     * 이문제를 해결하려면 REQUIRES_NEW 를 사용해서 트랜잭션을 분리해야한다.
     * REQUIRES_NEW 쓰면 커넥션을 2개 사용하게된다. 성능이 중요한곳에서는 이런 부분 주의해서 사용해야한다
     * REQUIRES_NEW 를 사용하지 않고 문제를 해결할수 있는 단순한 방법이 있다면 그 방법을 선택하는 것이 더좋다.
     *
     * memberService @Transaction:ON
     * memberRepository @Transaction:ON
     * logRepository @Transaction:ON(REQUIRES_NEW) Exception 발생
     */
    @Test
    void recoverException_success() {
        // given
        String username = "로그예외_recoverException_success";

        // when
        memberService.joinV2(username);


        // then member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }


}
