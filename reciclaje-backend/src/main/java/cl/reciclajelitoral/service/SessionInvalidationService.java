package cl.reciclajelitoral.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionInvalidationService {

    public record EmailChangeInfo(Long userId, String oldEmail, String newEmail, long timestamp) {}

    private final Map<Long, EmailChangeInfo> changesByUserId = new ConcurrentHashMap<>();
    private final Map<String, EmailChangeInfo> changesByOldEmail = new ConcurrentHashMap<>();

    public void registerEmailChange(Long userId, String oldEmail, String newEmail) {
        if (userId == null || oldEmail == null || newEmail == null) return;
        EmailChangeInfo info = new EmailChangeInfo(userId, oldEmail.toLowerCase().trim(), newEmail.toLowerCase().trim(), System.currentTimeMillis());
        changesByUserId.put(userId, info);
        changesByOldEmail.put(oldEmail.toLowerCase().trim(), info);
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

    public void clearEmailChange(Long userId, String email) {
        if (userId != null) {
            changesByUserId.remove(userId);
        }
        if (email != null) {
            changesByOldEmail.remove(email.toLowerCase().trim());
        }
    }
}
