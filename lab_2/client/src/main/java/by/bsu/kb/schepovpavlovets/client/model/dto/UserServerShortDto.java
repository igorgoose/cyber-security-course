package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserServerShortDto {
    private String id;
    private String name;
    private Boolean connected;
}
