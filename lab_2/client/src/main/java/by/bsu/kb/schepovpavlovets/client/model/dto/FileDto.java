package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FileDto {
    private String id;
    private String name;
    private String content;

}
