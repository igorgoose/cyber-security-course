package by.bsu.kb.schepovpavlovets.server.model.entity;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "client_connections")
public class ClientConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String session;
    private String iv;
    private LocalDateTime expiresAt;

    @JoinColumn(name = "client_id")
    @ManyToOne
    private Client client;
}
