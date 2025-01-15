package com.ticketrecipe.api.user;

import com.ticketrecipe.common.Gender;
import com.ticketrecipe.common.User;
import com.ticketrecipe.common.auth.CustomUserDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/v1/users")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserCreateRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userService.createUpdateUser(userDetails.getUsername(), userDetails.getEmail(),
                    request.getFullName(), request.getGender(), request.getDateOfBirth());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @Data
    public static class UserCreateRequest {
        @NotBlank(message = "First name cannot be blank")
        @Size(max = 300, message = "full name must not exceed 300 characters")
        private String fullName;

        private Gender gender;

        @Past(message = "Date of birth must be a past date")
        private LocalDate dateOfBirth;
    }
}
