package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileDto {
    private String id;
    private String name;
    private String content;
    private String lastUpdate;

}
