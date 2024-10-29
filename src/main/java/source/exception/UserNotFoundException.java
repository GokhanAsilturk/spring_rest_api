package source.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(Long id) {
        super("User not found: " + id);
    }

    public UserNotFoundException(String format) {
        super(format);
    }
}
