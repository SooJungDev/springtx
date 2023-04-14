package hello.springtx.order;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * User : Soo Jung Choi (crystal2840@neowiz.com)
 * Date : 2023.04.14
 * Time : 12:33 PM
 */
@Entity
@Table(name = "orders")
@Getter
@Setter
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    private String username; //정상. 예외 잔고부족
    private String payStatus; // 대기 완료
}
