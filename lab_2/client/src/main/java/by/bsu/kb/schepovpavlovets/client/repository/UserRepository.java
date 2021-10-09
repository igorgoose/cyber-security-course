package by.bsu.kb.schepovpavlovets.client.repository;

import by.bsu.kb.schepovpavlovets.client.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
