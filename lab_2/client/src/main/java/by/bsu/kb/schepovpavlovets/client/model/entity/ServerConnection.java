package by.bsu.kb.schepovpavlovets.client.model.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "server_connections", uniqueConstraints = @UniqueConstraint(columnNames = {"user_server_id"}))
public class ServerConnection {

    @Id
    private UUID id;
    private String session;
    private String iv;
    private LocalDateTime expiresAt;

    @ToString.Exclude
    @JoinColumn(name = "user_server_id")
    @ManyToOne
    private UserServer userServer;
}
