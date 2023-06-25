package org.example;

public class LoginUserPayload {

    private String email;
    private String password;

    public LoginUserPayload(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public LoginUserPayload() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
