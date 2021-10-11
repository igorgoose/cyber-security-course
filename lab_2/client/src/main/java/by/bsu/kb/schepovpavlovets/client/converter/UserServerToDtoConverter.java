package by.bsu.kb.schepovpavlovets.client.converter;

import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.UserServer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class UserServerToDtoConverter implements Converter<UserServer, UserServerDto> {

    @Override
    public UserServerDto convert(UserServer source) {
        UserServerDto userServerDto = new UserServerDto();
        userServerDto.setId(source.getId().toString());
        userServerDto.setName(source.getName());
        userServerDto.setIp(source.getServerData().getIp());
        userServerDto.setPort(source.getServerData().getPort());
        userServerDto.setConnected(!source.getServerConnections().isEmpty());
        return userServerDto;
    }
}
