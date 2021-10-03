package by.bsu.kb.schepovpavlovets.client.service.impl;

import by.bsu.kb.schepovpavlovets.client.service.IntegrationService;
import by.bsu.kb.schepovpavlovets.client.util.CryptUtility;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IntegrationServiceImpl implements IntegrationService {

    private final CryptUtility cryptUtility;
    @Value("${kb2.server.base-url")
    private String serverBaseUrl;
    @Value("${kb2.server.session")
    private String sessionEndpoint;

    @Override
    public void updateSessionKey() {

    }

}
