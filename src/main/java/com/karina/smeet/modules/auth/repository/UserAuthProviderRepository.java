package com.karina.smeet.modules.auth.repository;

import com.karina.smeet.entity.postgre.User;
import com.karina.smeet.entity.postgre.UserAuthProvider;
import com.karina.smeet.enums.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserAuthProviderRepository extends JpaRepository<UserAuthProvider, UUID> {
    Optional<UserAuthProvider> findByUserAndProvider(User user, Provider provider);
}
