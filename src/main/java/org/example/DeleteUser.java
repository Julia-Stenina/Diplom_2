package org.example;

import org.junit.After;

public class DeleteUser {
    @After
    public void deleteUser() {
        String emailValue = "kit-kat@yandex.ru";
        String passwordValue = "pass12345";
        LoginUserPayload loginUserPayload = new LoginUserPayload(emailValue, passwordValue);
        UserClient userClient = new UserClient();
        userClient.deleteUser(loginUserPayload);
    }

}
