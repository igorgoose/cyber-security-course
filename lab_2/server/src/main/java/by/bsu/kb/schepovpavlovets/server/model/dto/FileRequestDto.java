package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class FileRequestDto {
    private String encodedNamespace;
    private String encodedFilename;
    private String encodedContent;
}
