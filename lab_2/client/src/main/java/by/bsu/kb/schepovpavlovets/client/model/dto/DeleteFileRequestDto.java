package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class DeleteFileRequestDto {
    private String encodedFileId;
    private String encodedNamespace;
}
