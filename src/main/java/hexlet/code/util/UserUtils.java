package hexlet.code.util;

import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserUtils {
    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        var email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    public boolean isCurrentUserOrAdmin(Long id) {
        var emailCurrentUser = getCurrentUser().getEmail();
        var needUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("user not found"));
        var emailNeedUser = needUser.getEmail();
        var emailAdminUser = "hexlet@example.com";
        return emailCurrentUser.equals(emailNeedUser) || emailCurrentUser.equals(emailAdminUser);
    }
}
