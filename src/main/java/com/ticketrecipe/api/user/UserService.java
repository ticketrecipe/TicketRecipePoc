package com.ticketrecipe.api.user;

import com.ticketrecipe.common.Gender;
import com.ticketrecipe.common.User;
import com.ticketrecipe.common.auth.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private UserRepository userRepository;

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

    public User createUpdateUser(String userId, String emailAddress, String fullName,
            Gender gender, LocalDate dateOfBirth)
    {
        User user = userRepository.findById(userId)
                .orElseGet(() -> User.builder()
                        .id(userId)
                        .emailAddress(emailAddress)
                        .createdDate(LocalDateTime.now())
                        .build());

        // Update user fields
        user.setFullName(fullName);
        user.setGender(gender);
        user.setDateOfBirth(dateOfBirth);
        user.setLastUpdatedDate(LocalDateTime.now());

        // Save the user and return
        return userRepository.save(user);
    }
}
