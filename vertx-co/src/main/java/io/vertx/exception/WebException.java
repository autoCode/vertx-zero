package io.vertx.exception;

import io.vertx.core.http.HttpStatusCode;
import org.vie.cv.Strings;
import org.vie.util.Errors;

/**
 *
 */
public abstract class WebException extends ZeroRunException {

    private final String message;

    public WebException(final String message) {
        super(message);
        this.message = message;
    }

    public WebException(final Class<?> clazz, final Object... args) {
        super(Strings.EMPTY);
        this.message = Errors.normalize(clazz, getCode(), args);
    }

    public abstract int getCode();

    @Override
    public String getMessage() {
        return this.message;
    }

    public HttpStatusCode getStatus() {
        // Default exception for 400
        return HttpStatusCode.BAD_REQUEST;
    }
}