import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.CreateUserPayload;
import org.example.LoginUserPayload;
import org.example.UserClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ChangeUserDataTest {

    private String nameValue = "Kit-kat";
    private String emailValue = "kit-kat@yandex.ru";
    private String passwordValue = "pass12345";
    private String newNameValue = "Pom-Pom";
    private String newEmailValue = "kapiko@yandex.ru";
    private String newPasswordValue = "newPass123";
    LoginUserPayload loginUserPayload = new LoginUserPayload(emailValue, passwordValue);

    @Before
    public void setUp() {
        CreateUserPayload createUserPayload = new CreateUserPayload(emailValue, passwordValue,
            nameValue);
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        UserClient createUser = new UserClient();
        createUser.createUser(createUserPayload);
    }

    @After
    public void deleteUser() {
        UserClient userClient = new UserClient();
        userClient.deleteUser(loginUserPayload);
    }

    @Step("Отправка запроса на авторизацию и получение токена")
    public String sendPostRequestLoginUserAndGetToken(LoginUserPayload json) {
        UserClient userClient = new UserClient();
        return userClient.loginAndGetToken(json);
    }

    @Step("Отправка запроса на изменение поля для авторизованного пользователя")
    public Response sendPatchRequest(String token, String newData) {
        return given().header("Content-type", "application/json").header("Authorization", token)
            .and()
            .body(newData)
            .when()
            .patch("/api/auth/user/");
    }

    @Step("Отправка запроса на изменение поля для неавторизованного пользователя")
    public Response sendPatchRequestWithoutAuthorization(String newData) {
        return given().header("Content-type", "application/json")
            .and()
            .body(newData)
            .when()
            .patch("/api/auth/user/");
    }

    @Step("Проверка статус-кода и тела ответа при корректном изменении поля")
    public void checkCorrectChange(Response response, int statusCode, boolean message) {
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
    @DisplayName("Check change Email with authorization")
    @Description("Проверка смены электронной почты с авторизацией")
    public void checkChangeEmailWithAuthorization() {
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);
        Response response = sendPatchRequest(token, "{\"email\": \"" + newEmailValue + "\"}");

        checkCorrectChange(response, 200, true);
        loginUserPayload = new LoginUserPayload(newEmailValue, passwordValue);
    }

    @Test
    @DisplayName("Check change Password with authorization")
    @Description("Проверка смены пароля с авторизацией")
    public void checkChangePasswordWithAuthorization() {
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);
        Response response = sendPatchRequest(token, "{\"password\": \"" + newPasswordValue + "\"}");

        checkCorrectChange(response, 200, true);
        loginUserPayload = new LoginUserPayload(emailValue, newPasswordValue);
    }

    @Test
    @DisplayName("Check change Name with authorization")
    @Description("Проверка смены имени пользователя с авторизацией")
    public void checkChangeNameWithAuthorization() {
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);

        Response response = sendPatchRequest(token, "{\"name\": \"" + newNameValue + "\"}");
        checkCorrectChange(response, 200, true);
    }

    @Test
    @DisplayName("Check change Email without authorization")
    @Description("Проверка попытки смены электронной почты без авторизаци")
    public void checkChangeEmailWithoutAuthorization() {
        Response response = sendPatchRequestWithoutAuthorization(
            "{\"email\": \"" + newEmailValue + "\"}");

        checkStatusCodeAndBody(response, 401, "You should be authorised");
    }

    @Test
    @DisplayName("Check change Password without authorization")
    @Description("Проверка попытки смены пароля без авторизаци")
    public void checkChangePasswordWithoutAuthorization() {
        Response response = sendPatchRequestWithoutAuthorization(
            "{\"password\": \"" + newPasswordValue + "\"}");

        checkStatusCodeAndBody(response, 401, "You should be authorised");
    }

    @Test
    @DisplayName("Check change Name without authorization")
    @Description("Проверка попытки смены имени без авторизаци")
    public void checkChangeNameWithoutAuthorization() {
        Response response = sendPatchRequestWithoutAuthorization(
            "{\"name\": \"" + newNameValue + "\"}");

        checkStatusCodeAndBody(response, 401, "You should be authorised");
    }
}
