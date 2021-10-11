package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.exception.BadRequestException;
import by.bsu.kb.schepovpavlovets.client.exception.ConnectivityException;
import by.bsu.kb.schepovpavlovets.client.exception.NotFoundException;
import by.bsu.kb.schepovpavlovets.client.exception.ServerError;
import by.bsu.kb.schepovpavlovets.client.model.dto.ServerErrorDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerShortDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerConnection;
import by.bsu.kb.schepovpavlovets.client.model.entity.ServerData;
import by.bsu.kb.schepovpavlovets.client.model.entity.User;
import by.bsu.kb.schepovpavlovets.client.model.entity.UserServer;
import by.bsu.kb.schepovpavlovets.client.repository.ServerConnectionRepository;
import by.bsu.kb.schepovpavlovets.client.repository.ServerDataRepository;
import by.bsu.kb.schepovpavlovets.client.repository.UserServerRepository;
import by.bsu.kb.schepovpavlovets.client.security.AppUserDetails;
import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import by.bsu.kb.schepovpavlovets.client.service.UserServerService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServerServiceImpl implements UserServerService {

    private static final String IP_REGEX = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
    private static final String PORT_REGEX = "\\d{4,5}";
    private final IntegrationService integrationService;
    private final UserServerRepository userServerRepository;
    private final ServerDataRepository serverDataRepository;
    private final ServerConnectionRepository serverConnectionRepository;
    private final Converter<UserServer, UserServerShortDto> userServerShortDtoConverter;
    private final Converter<UserServer, UserServerDto> userServerDtoConverter;

    @Override
    public List<UserServerDto> getUserServers() {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<UserServer> userServers = userServerRepository.findByUserId(userDetails.getId());
        return userServers.stream().map(userServerDtoConverter::convert).collect(Collectors.toList());
    }

    @Override
    public UserServer getUserServer(UUID id) {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userServerRepository.findByIdAndUserId(id, userDetails.getId()).orElseThrow(() -> new NotFoundException("Server config not found!"));
    }

    @Override
    public UserServerDto getUserServerDto(UUID id) {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserServer userServer = userServerRepository.findByIdAndUserId(id, userDetails.getId()).orElseThrow(() -> new NotFoundException("Server config not found!"));
        return userServerDtoConverter.convert(userServer);
    }

    @Transactional
    @Override
    public UserServerShortDto saveUserServer(String name, String ip, String port) {
        AppUserDetails userDetails = (AppUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (ip == null || port == null || ip.isEmpty() || port.isEmpty()) {
            throw new BadRequestException("IP and Port must not be empty!");
        }
        if (!ip.matches(IP_REGEX)) {
            throw new BadRequestException("Invalid IP format!");
        }
        if (!port.matches(PORT_REGEX)) {
            throw new BadRequestException("Invalid Port format!");
        }
        if (name == null || name.isEmpty()) {
            name = ip;
        }
        ServerData serverData = serverDataRepository.findByIpAndPort(ip, port).orElse(null);
        if (serverData == null) {
           serverData = integrationService.signUpForServer(ip, port);
        }
        UserServer userServer = new UserServer();
        userServer.setServerData(serverData);
        User user = new User();
        user.setId(userDetails.getId());
        userServer.setUser(user);
        userServer.setName(name);
        userServer.setServerData(serverData);
        userServer.setNamespaceCreated(false);
        userServerRepository.save(userServer);
        return userServerShortDtoConverter.convert(userServerRepository.findById(userServer.getId()).orElseThrow());
    }

    @Transactional
    @Override
    public void deleteUserServer(String userServerId) {
        UserServer userServer = getUserServer(UUID.fromString(userServerId));
        ServerConnection currentServerConnection = serverConnectionRepository.findByUserServerId(userServer.getId()).orElse(null);
        if (currentServerConnection != null) {
            ServerErrorDto serverErrorDto = integrationService.disconnect(currentServerConnection);
            if (serverErrorDto != null && !serverErrorDto.getMessage().equals(ServerError.UNKNOWN_CONNECTION.name())) {
                throw new ConnectivityException(serverErrorDto.getMessage());
            }
            serverConnectionRepository.deleteById(currentServerConnection.getId());
        }
        userServerRepository.deleteById(userServer.getId());
    }
}
