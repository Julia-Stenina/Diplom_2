import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CreateUserTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
    }

    @After
    public void deleteUser() {
        String json = "{\"email\": \"kaktus@yandex.ru\", \"password\": \"pass12345\"}";
        UserClient userClient = new UserClient();
        userClient.deleteUser(json);
    }

    @Step("Отправка запроса")
    public Response sendRequestCreateUser(String json) {
        UserClient createCourier = new UserClient();
        return createCourier.createUser(json);
    }

    @Step("Проверка кода ошибки и сообщения")
    public void checkStatusCodeAndMessage(Response response, int statusCode, String message) {
        response.then().assertThat().statusCode(statusCode).and()
            .body("message", equalTo(message));
    }

    @Test
    @DisplayName("Check create unique user")
    @Description("Проверка статус-кода и тела ответа при создании уникального пользователя")
    public void checkCreateUniqueUser() {
        Response response = sendRequestCreateUser(
            "{\"email\": \"kaktus@yandex.ru\", \"password\": \"pass12345\", \"name\": \"Kaktus\"}");
        response.then().assertThat().statusCode(200)
            .and()
            .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Check create two identical users")
    @Description("Проверка статус-кода и тела ответа при создании пользователя, который уже зарегистрирован")
    public void checkCreateTwoIdenticalUsers() {
        sendRequestCreateUser(
            "{\"email\": \"kaktus@yandex.ru\", \"password\": \"pass12345\", \"name\": \"Kaktus\"}");
        Response response = sendRequestCreateUser(
            "{\"email\": \"kaktus@yandex.ru\", \"password\": \"pass12345\", \"name\": \"Kaktus\"}");
        checkStatusCodeAndMessage(response, 403, "User already exists");
    }

    @Test
    @DisplayName("Check create user without email")
    @Description("Проверка создания пользователя без электронной почты")
    public void checkCreateUserWithoutEmail() {
        Response response = sendRequestCreateUser(
            "{\"email\": \"\", \"password\": \"pass12345\", \"name\": \"Kaktus\"}");
        checkStatusCodeAndMessage(response, 403, "Email, password and name are required fields");
    }

    @Test
    @DisplayName("Check create user without password")
    @Description("Проверка создания пользователя без пароля")
    public void checkCreateUserWithoutPassword() {
        Response response = sendRequestCreateUser(
            "{\"email\": \"kaktus@yandex.ru\", \"password\": \"\", \"name\": \"Kaktus\"}");
        checkStatusCodeAndMessage(response, 403, "Email, password and name are required fields");
    }

    @Test
    @DisplayName("Check create user without name")
    @Description("Проверка создания пользователя без имени")
    public void checkCreateUserWithoutName() {
        Response response = sendRequestCreateUser(
            "{\"email\": \"kaktus@yandex.ru\", \"password\": \"pass12345\", \"name\": \"\"}");
        checkStatusCodeAndMessage(response, 403, "Email, password and name are required fields");
    }
}
