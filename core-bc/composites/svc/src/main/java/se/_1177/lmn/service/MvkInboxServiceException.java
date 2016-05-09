package se._1177.lmn.service;

/**
 * @author Patrik Björk
 */
public class MvkInboxServiceException extends Exception {

    public MvkInboxServiceException(String message) {
        super(message);
    }

    public MvkInboxServiceException(String message, Exception cause) {
        super(message, cause);
    }
}
