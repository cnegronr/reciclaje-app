package cl.reciclajelitoral.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionInvalidationService {

    public record EmailChangeInfo(Long userId, String oldEmail, String newEmail, long timestamp) {}
    public record PasswordChangeInfo(Long userId, String email, long timestamp) {}

    private final Map<Long, EmailChangeInfo> changesByUserId = new ConcurrentHashMap<>();
    private final Map<String, EmailChangeInfo> changesByOldEmail = new ConcurrentHashMap<>();

    private final Map<Long, PasswordChangeInfo> passwordChangesByUserId = new ConcurrentHashMap<>();
    private final Map<String, PasswordChangeInfo> passwordChangesByEmail = new ConcurrentHashMap<>();

    public void registerEmailChange(Long userId, String oldEmail, String newEmail) {
        if (userId == null || oldEmail == null || newEmail == null) return;
        EmailChangeInfo info = new EmailChangeInfo(userId, oldEmail.toLowerCase().trim(), newEmail.toLowerCase().trim(), System.currentTimeMillis());
        changesByUserId.put(userId, info);
        changesByOldEmail.put(oldEmail.toLowerCase().trim(), info);
    }

    public void registerPasswordChange(Long userId, String email) {
        if (userId == null && email == null) return;
        long now = System.currentTimeMillis();
        PasswordChangeInfo info = new PasswordChangeInfo(userId, email != null ? email.toLowerCase().trim() : null, now);
        if (userId != null) {
            passwordChangesByUserId.put(userId, info);
        }
        if (email != null) {
            passwordChangesByEmail.put(email.toLowerCase().trim(), info);
        }
    }

    public boolean isEmailChangedForToken(String tokenEmail) {
        if (tokenEmail == null) return false;
        return changesByOldEmail.containsKey(tokenEmail.toLowerCase().trim());
    }

    public boolean isEmailChangedForUser(Long userId, String tokenEmail) {
        if (tokenEmail != null && changesByOldEmail.containsKey(tokenEmail.toLowerCase().trim())) {
            return true;
        }
        if (userId != null) {
            EmailChangeInfo info = changesByUserId.get(userId);
            if (info != null && tokenEmail != null && !info.newEmail().equalsIgnoreCase(tokenEmail.toLowerCase().trim())) {
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordChangedAfter(Long userId, String tokenEmail, long tokenIssuedAt) {
        if (tokenEmail != null) {
            PasswordChangeInfo info = passwordChangesByEmail.get(tokenEmail.toLowerCase().trim());
            if (info != null && tokenIssuedAt < info.timestamp()) {
                return true;
            }
        }
        if (userId != null) {
            PasswordChangeInfo info = passwordChangesByUserId.get(userId);
            if (info != null && tokenIssuedAt < info.timestamp()) {
                return true;
            }
        }
        return false;
    }

    public void clearEmailChange(Long userId, String email) {
        if (userId != null) {
            changesByUserId.remove(userId);
        }
        if (email != null) {
            changesByOldEmail.remove(email.toLowerCase().trim());
        }
    }

    public void clearPasswordChange(Long userId, String email) {
        if (userId != null) {
            passwordChangesByUserId.remove(userId);
        }
        if (email != null) {
            passwordChangesByEmail.remove(email.toLowerCase().trim());
        }
    }
}
