package com.ticketrecipe.api.user;

import com.ticketrecipe.common.AccessToken;
import com.ticketrecipe.common.User;
import jakarta.persistence.Access;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AccessToken accessToken;

    /**
     * POST /users - Create or update a user based on Cognito ID token
     */
    @PostMapping
    public ResponseEntity<User> createUser() {
        try {
            String userId = accessToken.getUserId();
            String emailAddress = accessToken.getUserEmail();

            // Pass claims to the service to create or update the user
            User user = userService.createOrUpdateUser(userId, emailAddress);

            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
