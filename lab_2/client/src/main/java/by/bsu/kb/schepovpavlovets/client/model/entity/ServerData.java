package by.bsu.kb.schepovpavlovets.client.model.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "server_data")
public class ServerData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String clientId;
    private String session;
    private String iv;
    private String status;
    private LocalDateTime updatedOn = LocalDateTime.now();

    public enum ConnectionStatus {
        NO_SERVER, DISCONNECTED, CONNECTED
    }
}
