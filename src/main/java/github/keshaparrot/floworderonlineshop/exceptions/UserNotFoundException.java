package github.keshaparrot.floworderonlineshop.exceptions;

public class UserNotFoundException extends EntityNotFoundException {
    public UserNotFoundException(Long userId) {
        super("User with id " + userId + " not found.");
    }
}


