import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.CreateUserPayload;
import org.example.DeleteUser;
import org.example.LoginUserPayload;
import org.example.UserClient;
import org.junit.Before;
import org.junit.Test;

public class LoginUserTest extends DeleteUser {
    private String nameValue = "Kit-kat";
    private String emailValue = "kit-kat@yandex.ru";
    private String passwordValue = "pass12345";
    private String wrongEmailValue = "kit-katTtTt@yandex.ru";

    @Before
    public void setUp() {
        CreateUserPayload createUserPayload = new CreateUserPayload(emailValue, passwordValue, nameValue);
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        UserClient createUser = new UserClient();
        createUser.createUser(createUserPayload);
    }

    @Step("Отправка запроса на авторизацию")
    public Response sendPostRequestLoginUser(LoginUserPayload json) {
        UserClient logInUser = new UserClient();
        return logInUser.logInUser(json);
    }

    @Step("Проверка статус-кода и тела ответа при корректном логине")
    public void checkCorrectLogin(Response response, int statusCode, boolean message) {
        response.then().assertThat().statusCode(statusCode)
            .and()
            .assertThat().body("success", equalTo(message));
    }

    @Step("Проверка кода ошибки и текстового сообщения")
    public void checkStatusCodeAndBody(Response response, int statusCode, String message) {
        response.then().assertThat().statusCode(statusCode)
            .and()
            .body("message", equalTo(message));
    }

    @Test
    @DisplayName("Check login existing user")
    @Description("Проверка логина существующего пользователя")
    public void checkLoginExistingUser() {
        LoginUserPayload loginUserPayload = new LoginUserPayload(emailValue, passwordValue);
        Response response = sendPostRequestLoginUser(loginUserPayload);

        checkCorrectLogin(response, 200, true);
    }

    @Test
    @DisplayName("Check login non-existent user")
    @Description("Проверка логина несуществующего пользователя")
    public void checkLoginNonExistentUser() {
        LoginUserPayload loginUserPayload = new LoginUserPayload(wrongEmailValue, passwordValue);
        Response response = sendPostRequestLoginUser(loginUserPayload);

        checkStatusCodeAndBody(response, 401, "email or password are incorrect");
    }
}
