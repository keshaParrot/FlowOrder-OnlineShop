package github.keshaparrot.floworderonlineshop.services.interfaces;

public interface INotificationService {

    void sendNotificationToUser(String to, String subject, String body);
    void sendVerificationCodeToUser(String email);
}
