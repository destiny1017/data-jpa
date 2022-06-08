package study.datajpa.dto;

import lombok.Data;

@Data
public class UsernameOnlyDto {

    private final String username;
    public UsernameOnlyDto(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }
}
