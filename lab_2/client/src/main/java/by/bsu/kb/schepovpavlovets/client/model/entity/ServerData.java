package by.bsu.kb.schepovpavlovets.client.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "server_data", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ip", "port"})
})
public class ServerData {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String clientId;
    private String ip;
    private String port;
    private LocalDateTime updatedOn = LocalDateTime.now();
}
