package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class EditFileRequestDto {
    private String fileId;
    private String namespace;
    private String name;
    private String content;
}
