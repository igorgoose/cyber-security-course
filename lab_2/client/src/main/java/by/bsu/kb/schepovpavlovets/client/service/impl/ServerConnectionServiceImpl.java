package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.exception.ConnectivityException;
import by.bsu.kb.schepovpavlovets.client.exception.ServerError;
import by.bsu.kb.schepovpavlovets.client.model.dto.ServerConnectionResponseDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.ServerErrorDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;
import by.bsu.kb.schepovpavlovets.client.model.entity.UserServer;
import by.bsu.kb.schepovpavlovets.client.repository.ServerConnectionRepository;
import by.bsu.kb.schepovpavlovets.client.security.AppUserDetails;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import by.bsu.kb.schepovpavlovets.client.service.ServerConnectionService;
import by.bsu.kb.schepovpavlovets.client.service.UserServerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class ServerConnectionServiceImpl implements ServerConnectionService {

    private final ServerConnectionRepository serverConnectionRepository;
    private final UserServerService userServerService;
    private final IntegrationService integrationService;
    private final DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Transactional
    @Override
    public void connectToServer(UUID userServerId) {
        UserServer userServer = userServerService.getUserServer(userServerId);
        Optional<ServerConnection> currentServerConnectionOpt = serverConnectionRepository.findByUserServerId(userServer.getId());
        currentServerConnectionOpt.ifPresent(integrationService::disconnect);
        serverConnectionRepository.deleteByUserServerId(userServer.getId());
        ServerConnectionResponseDto responseDto = integrationService.connect(userServer.getServerData());
        ServerConnection serverConnection = new ServerConnection();
        serverConnection.setId(UUID.fromString(responseDto.getConnectionId()));
        serverConnection.setSession(responseDto.getSessionKey());
        serverConnection.setIv(responseDto.getIv());
        serverConnection.setExpiresAt(LocalDateTime.parse(responseDto.getExpiresAt(), dtf));
        serverConnection.setUserServer(userServer);
        serverConnectionRepository.save(serverConnection);
    }

    @Transactional(dontRollbackOn = ConnectivityException.class)
    @Override
    public void disconnectFromServer(UUID userServerId) {
        UserServer userServer = userServerService.getUserServer(userServerId);
        ServerConnection currentServerConnection = serverConnectionRepository.findByUserServerId(userServer.getId()).orElseThrow(() ->
                new ConnectivityException(String.format("You are not connected to server %s at the moment!", userServer.getName())));
        ServerErrorDto serverErrorDto = integrationService.disconnect(currentServerConnection);
        if (serverErrorDto != null && !serverErrorDto.getMessage().equals(ServerError.UNKNOWN_CONNECTION.name())) {
            throw new ConnectivityException(serverErrorDto.getMessage());
        }
        serverConnectionRepository.deleteById(currentServerConnection.getId());
    }

    @Transactional
    @Override
    public ServerConnection getCurrentServerConnection() {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return serverConnectionRepository.findByUserServerUserId(userDetails.getId()).orElseThrow(() ->
                new ConnectivityException("You are not connected to any server at the moment!"));
    }

    @Transactional
    @Override
    public String getServerConnectionStatus() {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        ServerConnection serverConnection = serverConnectionRepository.findByUserServerUserId(userDetails.getId()).orElse(null);
        return serverConnection == null ? "Disconnected" : "Connected to " + serverConnection.getUserServer().getName();
    }


}
