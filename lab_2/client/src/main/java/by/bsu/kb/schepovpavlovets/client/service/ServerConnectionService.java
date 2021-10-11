package by.bsu.kb.schepovpavlovets.client.service;

import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;

import javax.transaction.Transactional;
import java.util.UUID;

public interface ServerConnectionService {
    void connectToServer(UUID userServerId);

    void disconnectFromServer(UUID userServerId);

    @Transactional
    ServerConnection getCurrentServerConnection();
}
