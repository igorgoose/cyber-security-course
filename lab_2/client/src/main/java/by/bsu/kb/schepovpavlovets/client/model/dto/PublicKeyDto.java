package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class PublicKeyDto {
    private String base64Key;
}
