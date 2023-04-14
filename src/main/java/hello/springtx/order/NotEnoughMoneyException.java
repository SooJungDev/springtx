package hello.springtx.order;

/**
 * User : Soo Jung Choi (crystal2840@neowiz.com)
 * Date : 2023.04.14
 * Time : 12:32 PM
 */
public class NotEnoughMoneyException extends Exception{

    public NotEnoughMoneyException(String message) {
        super(message);
    }
}
