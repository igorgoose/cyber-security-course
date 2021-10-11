package by.bsu.kb.schepovpavlovets.client.converter;

import by.bsu.kb.schepovpavlovets.client.model.dto.UserServerShortDto;
import by.bsu.kb.schepovpavlovets.client.model.entity.UserServer;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserServerToShortDtoConverter implements Converter<UserServer, UserServerShortDto> {

    @Override
    public UserServerShortDto convert(@NonNull UserServer source) {
        return UserServerShortDto.builder()
                                 .id(source.getId().toString())
                                 .name(source.getName())
                                 .connected(!source.getServerConnections().isEmpty())
                                 .build();
    }
}
