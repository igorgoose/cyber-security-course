package by.bsu.kb.schepovpavlovets.client.model.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ServerErrorDto {
    private String message;
}
