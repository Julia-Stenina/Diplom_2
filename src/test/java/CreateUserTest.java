import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.CreateUserPayload;
import org.example.DeleteUser;
import org.example.UserClient;
import org.junit.Before;
import org.junit.Test;

public class CreateUserTest extends DeleteUser {

    private String nameValue = "Kit-kat";
    private String emailValue = "kit-kat@yandex.ru";
    private String passwordValue = "pass12345";
    CreateUserPayload createUserPayload = new CreateUserPayload(emailValue, passwordValue, nameValue);

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
    }

    @Step("Отправка запроса")
    public Response sendRequestCreateUser(CreateUserPayload json) {
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
        Response response = sendRequestCreateUser(createUserPayload);
        response.then().assertThat().statusCode(200)
            .and()
            .body("success", equalTo(true));
    }

    @Test
    @DisplayName("Check create two identical users")
    @Description("Проверка статус-кода и тела ответа при создании пользователя, который уже зарегистрирован")
    public void checkCreateTwoIdenticalUsers() {
        sendRequestCreateUser(createUserPayload);
        Response response = sendRequestCreateUser(createUserPayload);

        checkStatusCodeAndMessage(response, 403, "User already exists");
    }

    @Test
    @DisplayName("Check create user without email")
    @Description("Проверка создания пользователя без электронной почты")
    public void checkCreateUserWithoutEmail() {
        CreateUserPayload createUser = new CreateUserPayload(null, passwordValue, nameValue);
        Response response = sendRequestCreateUser(createUser);

        checkStatusCodeAndMessage(response, 403, "Email, password and name are required fields");
    }

    @Test
    @DisplayName("Check create user without password")
    @Description("Проверка создания пользователя без пароля")
    public void checkCreateUserWithoutPassword() {
        CreateUserPayload createUser = new CreateUserPayload(emailValue, null, nameValue);
        Response response = sendRequestCreateUser(createUser);

        checkStatusCodeAndMessage(response, 403, "Email, password and name are required fields");
    }

    @Test
    @DisplayName("Check create user without name")
    @Description("Проверка создания пользователя без имени")
    public void checkCreateUserWithoutName() {
        CreateUserPayload createUser = new CreateUserPayload(emailValue, passwordValue, null);
        Response response = sendRequestCreateUser(createUser);

        checkStatusCodeAndMessage(response, 403, "Email, password and name are required fields");
    }
}
