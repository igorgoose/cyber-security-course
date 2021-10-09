package by.bsu.kb.schepovpavlovets.client.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.UUID;


public class AppUserDetails extends User {
    @Getter
    private final UUID id;

    public AppUserDetails(UUID id, String username, String password, Collection<? extends GrantedAuthority> authorities) {
        super(username, password, authorities);
        this.id = id;
    }

}
