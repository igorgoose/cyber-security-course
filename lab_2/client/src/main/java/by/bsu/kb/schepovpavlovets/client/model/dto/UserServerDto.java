package by.bsu.kb.schepovpavlovets.client.model.dto;

import lombok.Data;

@Data
public class UserServerDto {
    private String id;
    private String name;
    private String ip;
    private String port;
    private boolean connected;
}
