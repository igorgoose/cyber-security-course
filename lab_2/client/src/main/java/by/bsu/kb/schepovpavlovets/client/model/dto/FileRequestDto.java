package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FileRequestDto {
    private String encodedFilename;
    private String encodedContent;
}
