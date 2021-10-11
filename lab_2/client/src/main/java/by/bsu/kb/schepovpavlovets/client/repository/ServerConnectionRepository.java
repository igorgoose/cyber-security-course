package by.bsu.kb.schepovpavlovets.client.repository;

import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ServerConnectionRepository extends JpaRepository<ServerConnection, UUID> {

    Optional<ServerConnection> findByUserServerId(UUID userServerId);

    Optional<ServerConnection> findByUserServerUserId(UUID userId);

    void deleteByUserServerId(UUID userServerId);
}
