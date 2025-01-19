package com.ticketrecipe.api.user;

import com.ticketrecipe.api.sales.PrivateSaleService;
import com.ticketrecipe.common.Gender;
import com.ticketrecipe.common.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PrivateSaleService privateSaleService;

    public User getUserById(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));
    }

    public Optional<User> getUserByEmail(String emailAddress) {
        return userRepository.findByEmailAddress(emailAddress); // Requires a custom query method in UserRepository
    }

    public User createUpdate(String userId, String emailAddress, String fullName,
            Gender gender, LocalDate dateOfBirth)
    {
        User user = userRepository.findById(userId)
                .orElseGet(() -> User.builder()
                        .id(userId)
                        .emailAddress(emailAddress)
                        .createdAt(LocalDateTime.now())
                        .build());

        user.setFullName(fullName);
        user.setGender(gender);
        user.setDateOfBirth(dateOfBirth);
        user.setLastUpdatedDate(LocalDateTime.now());

        userRepository.save(user);

        // Delegate to PrivateSaleService to link any private listings to the new signed-up user
        privateSaleService.linkPrivateBuyer(user);

        return user;
    }
}
