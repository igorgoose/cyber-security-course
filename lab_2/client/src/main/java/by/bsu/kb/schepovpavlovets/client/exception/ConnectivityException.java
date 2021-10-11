package by.bsu.kb.schepovpavlovets.client.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ConnectivityException extends RuntimeException {

    public ConnectivityException(String message) {
        super(message);
    }
}
