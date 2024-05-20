package ru.vilas.sewing.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.vilas.sewing.model.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    @NotNull
    @Query("SELECT u FROM User u  ORDER BY u.name")
    List<User> findAll();

    Boolean existsByEmail(String email);

    Optional<User> findByUsernameOrEmail(String username, String email);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    //Optional<String> findPasswordByUsername(String username);
    @Query("SELECT u.password FROM User u WHERE u.username = :username")
    Optional<String> findPasswordByUsername(@Param("username") String username);

    // Метод для поиска пользователя по идентификатору (id)
    @Query("SELECT u.name FROM User u WHERE u.id = :userId")
    String findNameById(Long userId);

}
