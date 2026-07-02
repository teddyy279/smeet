package com.karina.smeet.modules.user.repository;

import com.karina.smeet.entity.postgre.User;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query(nativeQuery = true, value = """
            SELECT u.* FROM users u WHERE u.id IN (
                SELECT f.addressee_id FROM friendships f
                WHERE f.requester_id = :currentUserId AND f.status = 'ACCEPTED'
                UNION
                SELECT f.requester_id FROM friendships f
                WHERE f.addressee_id = :currentUserId AND f.status = 'ACCEPTED'
            )
            AND (
                unaccent(LOWER(u.username)) LIKE unaccent(LOWER(CONCAT('%', :query, '%')))
                OR unaccent(LOWER(u.display_name)) LIKE unaccent(LOWER(CONCAT('%', :query, '%')))
            )
            """)
    List<User> searchFriends(
            @Param("query") String query,
            @Param("currentUserId") UUID currentUserId,
            Limit limit
    );/* search friend -> khi là bạn bè -> (ACCEPTED) -> với 1 user xét cả trường hợp mình là người request và nguời
     được request kết bạn*/

    @Query("""
        SELECT u FROM User u
        WHERE LOWER(u.username) = LOWER(:username)
        AND u.id != :currentUserId
        """)
    Optional<User> findByUsernameExact(
            @Param("username") String username,
            @Param("currentUserId") UUID currentUserId
    );

    @Query(nativeQuery = true, value = """
            SELECT * FROM users u
            WHERE u.id != :currentUserId
            AND (
                unaccent(LOWER(u.username)) LIKE unaccent(LOWER(CONCAT('%', :query, '%')))
                OR unaccent(LOWER(u.display_name)) LIKE unaccent(LOWER(CONCAT('%', :query, '%')))
            )
            """)
    List<User> searchGlobal(
            @Param("query") String query,
            @Param("currentUserId") UUID currentUserId,
            Limit limit
    );
}
