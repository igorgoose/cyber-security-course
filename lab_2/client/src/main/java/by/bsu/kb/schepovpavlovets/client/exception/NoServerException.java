package by.bsu.kb.schepovpavlovets.client.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class NoServerException extends RuntimeException {
    public NoServerException() {
    }

    public NoServerException(String message) {
        super(message);
    }

    public NoServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoServerException(Throwable cause) {
        super(cause);
    }
}
