package com.karina.smeet.modules.user.repository;

import com.karina.smeet.entity.postgre.User;
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

    @Query("""
            SELECT u FROM User u WHERE u.id IN (
                SELECT f.addressee.id FROM Friendship f
                WHERE f.requester.id = :currentUserId AND f.status = 'ACCEPTED'
                UNION
                SELECT f.requester.id FROM Friendship f
                WHERE f.addressee.id = :currentUserId AND f.status = 'ACCEPTED'            
            )
            AND (
<<<<<<< HEAD
                LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) 
                OR LOWER(u.displayName) LIKE LOWER(CONCAT('%', :query, '%'))           
            )    
=======
                immutable_unaccent(LOWER(u.username)) LIKE immutable_unaccent(LOWER(CONCAT('%', :query, '%')))
                OR immutable_unaccent(LOWER(u.display_name)) LIKE immutable_unaccent(LOWER(CONCAT('%', :query, '%')))
            )
>>>>>>> 5c1e8fe (feat(friend): implement friend module with friendship management and room creation)
            """)
    List<User> searchFriends(
            @Param("query") String query,
            @Param("currentUserId") UUID currentUserId
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
<<<<<<< HEAD
=======

    @Query(nativeQuery = true, value = """
            SELECT * FROM users u
            WHERE u.id != :currentUserId
            AND (
                immutable_unaccent(LOWER(u.username)) LIKE immutable_unaccent(LOWER(CONCAT('%', :query, '%')))
                OR immutable_unaccent(LOWER(u.display_name)) LIKE immutable_unaccent(LOWER(CONCAT('%', :query, '%')))
            )
            """)
    List<User> searchGlobal(
            @Param("query") String query,
            @Param("currentUserId") UUID currentUserId,
            Limit limit
    );

    @Query("""
           SELECT u FROM User u WHERE u.id IN(
               SELECT f.addressee.id FROM Friendship f
               WHERE f.requester.id = :userId AND f.status = 'ACCEPTED'
               UNION
               SELECT f.requester.id FROM Friendship f
               WHERE f.addressee.id = :userId AND f.status = 'ACCEPTED'
               )
               ORDER BY u.displayName ASC
           """)
    List<User> findAllFriends(@Param("userId") UUID userId);
>>>>>>> 5c1e8fe (feat(friend): implement friend module with friendship management and room creation)
}
