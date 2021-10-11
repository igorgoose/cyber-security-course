package by.bsu.kb.schepovpavlovets.client.service;

import by.bsu.kb.schepovpavlovets.client.model.dto.ServerConnectionResponseDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.ServerErrorDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import lombok.SneakyThrows;
import org.springframework.transaction.annotation.Transactional;

public interface IntegrationService {
    ServerData signUpForServer(String ip, String port);

    @Transactional
    @SneakyThrows
    ServerConnectionResponseDto connect(ServerData serverData);

    ServerErrorDto disconnect(ServerConnection serverConnection);

    String getConnectionStatus();
}
