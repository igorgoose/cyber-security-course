package by.bsu.kb.schepovpavlovets.client.service;

import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerDto;
import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerShortDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.UserServer;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

public interface UserServerService {
    List<UserServerDto> getUserServers();

    UserServer getUserServer(UUID id);

    UserServerDto getUserServerDto(UUID id);

    @Transactional
    UserServerShortDto saveUserServer(String name, String ip, String port);

    @Transactional
    void deleteUserServer(String userServerId);
}
