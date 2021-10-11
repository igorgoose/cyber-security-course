package by.bsu.kb.schepovpavlovets.server.repository;

import by.bsu.kb.schepovpavlovets.server.model.entity.ClientConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientConnectionRepository extends JpaRepository<ClientConnection, UUID> {
    boolean existsByIdAndClientId(UUID id, UUID clientId);
}
