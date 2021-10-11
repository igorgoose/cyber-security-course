package by.bsu.kb.schepovpavlovets.server.model.dto;

import lombok.Data;

@Data
public class DeleteFileRequestDto {
    private String encodedFileId;
    private String encodedNamespace;
}
