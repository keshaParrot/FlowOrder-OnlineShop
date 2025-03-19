package github.keshaparrot.floworderonlineshop.exceptions;

public class UserAddressNotExistException extends EntityNotFoundException {
    public UserAddressNotExistException(Long userId) {
        super("User with id " + userId + " has not added address information.");
    }
}



