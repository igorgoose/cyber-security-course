package by.bsu.kb.schepovpavlovets.server.repository;

import by.bsu.kb.schepovpavlovets.server.model.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ClientRepository extends JpaRepository<Client, UUID> {
}
