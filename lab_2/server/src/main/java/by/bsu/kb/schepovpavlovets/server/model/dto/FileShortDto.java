package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileShortDto {
    private String id;
    private String filename;
    private String lastUpdate;
}
