package by.bsu.kb.schepovpavlovets.client.model.dto;

import by.bsu.kb.schepovpavlovets.client.model.entity.User;
import lombok.Data;

public enum UserDto {
    ;

    private interface Id {
        Long getId();
    }

    private interface Username {
        String getUsername();
    }

    private interface Password {
        String getPassword();
    }

    private interface FirstName {
        String getFirstName();
    }

    private interface LastName {
        String getLastName();
    }

    public enum Request {
        ;

        @Data
        public static class SignUp implements Username, Password {
            private String username;
            private String password;
            private String passwordControl;

            public User convert() {
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                return user;
            }
        }
    }
}
