package by.bsu.kb.schepovpavlovets.client.repository;

import by.bsu.kb.schepovpavlovets.client.model.entity.UserServer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserServerRepository extends JpaRepository<UserServer, UUID> {
    List<UserServer> findByUserId(UUID userId);

    Optional<UserServer> findByIdAndUserId(UUID id, UUID userId);
}
