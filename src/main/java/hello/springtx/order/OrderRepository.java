package hello.springtx.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User : Soo Jung Choi (crystal2840@neowiz.com)
 * Date : 2023.04.14
 * Time : 12:35 PM
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
