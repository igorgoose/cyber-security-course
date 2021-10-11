package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class EditFileRequestDto {
    private String fileId;
    private String namespace;
    private String name;
    private String content;
}
