package by.bsu.kb.schepovpavlovets.client.service;

import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import lombok.SneakyThrows;

public interface IntegrationService {
    ServerData signUpForServer();

    @SneakyThrows
    ServerData getNewSession();

    @SneakyThrows
    ServerData invalidateSession();

    String getConnectionStatus();
}
