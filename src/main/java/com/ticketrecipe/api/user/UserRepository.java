package com.ticketrecipe.api.user;

import com.ticketrecipe.common.User;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmailAddress(String emailAddress);
}
