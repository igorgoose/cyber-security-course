package by.bsu.kb.schepovpavlovets.client.service;

import lombok.SneakyThrows;

public interface IntegrationService {
    void signUpForServer();

    @SneakyThrows
    void getNewSession();
}
