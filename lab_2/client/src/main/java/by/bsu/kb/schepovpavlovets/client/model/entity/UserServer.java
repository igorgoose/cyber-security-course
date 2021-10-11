package by.bsu.kb.schepovpavlovets.client.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_servers")
public class UserServer {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    private String name;
    private Boolean namespaceCreated;

    @JoinColumn(name = "user_id")
    @ManyToOne
    private User user;

    @JoinColumn(name = "server_data_id")
    @ManyToOne
    private ServerData serverData;

    @OneToMany(mappedBy = "userServer")
    private List<ServerConnection> serverConnections = new ArrayList<>();
}
