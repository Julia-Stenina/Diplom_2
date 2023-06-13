import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserClient;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class LoginUserTest {

    @BeforeClass
    public static void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        String json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\", \"name\": \"Kit-kat\"}";
        UserClient createUser = new UserClient();
        createUser.createUser(json);
    }

    @AfterClass
    public static void deleteUser() {
        String json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}";
        UserClient userClient = new UserClient();
        userClient.deleteUser(json);
    }

    @Step("Отправка запроса на авторизацию")
    public Response sendPostRequestLoginUser(String json) {
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
        Response response = sendPostRequestLoginUser(
            "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
        checkCorrectLogin(response, 200, true);
    }

    @Test
    @DisplayName("Check login non-existent user")
    @Description("Проверка логина несуществующего пользователя")
    public void checkLoginNonExistentUser() {
        Response response = sendPostRequestLoginUser(
            "{\"email\": \"kaktus_xoxo@yandex.ru\", \"password\": \"pass98765\"}");
        checkStatusCodeAndBody(response, 401, "email or password are incorrect");
    }
}
