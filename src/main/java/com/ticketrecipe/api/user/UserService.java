package com.ticketrecipe.api.user;

import com.ticketrecipe.common.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    public Optional<User> getUserByEmail(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress); // Requires a custom query method in UserRepository
    }

    public User createOrUpdateUser(String userId, String emailAddress) {
        User user = User.builder().id(userId).emailAddress(emailAddress).build();
        return userRepository.save(user);
    }
}
