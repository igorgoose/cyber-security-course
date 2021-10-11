package by.bsu.kb.schepovpavlovets.server.repository;

import by.bsu.kb.schepovpavlovets.server.model.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    List<FileEntity> findByClientIdAndNamespaceOrderByUpdatedOnDesc(UUID clientId, String namespace);

    Optional<FileEntity> findByIdAndNamespace(UUID fileId, String namespace);
}
