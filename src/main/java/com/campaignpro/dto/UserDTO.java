package com.campaignpro.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDTO {

    private String id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private boolean active;

    public UserDTO(String id, String username, String email, String fullName, String role, boolean active) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }
}
