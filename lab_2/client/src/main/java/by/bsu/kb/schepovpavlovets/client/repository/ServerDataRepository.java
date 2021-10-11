package by.bsu.kb.schepovpavlovets.client.repository;

import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ServerDataRepository extends JpaRepository<ServerData, Integer> {
    @Query("from ServerData sd order by sd.updatedOn desc")
    List<ServerData> findCurrentServerData(Pageable pageable);

    Optional<ServerData> findByIpAndPort(String ip, String port);

    void deleteByClientId(String clientId);
}
