import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ChangeUserDataTest {

  @Before
  public void setUp() {
    RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
    String json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\", \"name\": \"Kaktus\"}";
    UserClient createUser = new UserClient();
    createUser.createUser(json);
  }

  @After
  public void deleteUser() {
    UserClient userClient = new UserClient();
    userClient.deleteUser(json);
  }

  @Step("Отправка запроса на авторизацию и получение токена")
  public String sendPostRequestLoginUserAndGetToken(String json) {
    UserClient userClient = new UserClient();
    return userClient.loginAndGetToken(json);
  }

  @Step("Отправка запроса на изменение поля для авторизованного пользователя")
  public Response sendPatchRequest(String token, String newData){
    return given().header("Content-type", "application/json").header("Authorization", token)
        .and()
        .body(newData)
        .when()
        .patch("/api/auth/user/");
  }

  @Step("Отправка запроса на изменение поля для неавторизованного пользователя")
  public Response sendPatchRequestWithoutAuthorization(String newData){
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

  private String json;

  @Test
  @DisplayName("Check change Email with authorization")
  @Description("Проверка смены электронной почты с авторизацией")
  public void checkChangeEmailWithAuthorization() {

    String token = sendPostRequestLoginUserAndGetToken("{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
    Response response = sendPatchRequest(token, "{\"email\": \"kapiko@yandex.ru\"}");
    checkCorrectChange(response, 200, true);
    json = "{\"email\": \"kapiko@yandex.ru\", \"password\": \"pass12345\"}";
  }

  @Test
  @DisplayName("Check change Password with authorization")
  @Description("Проверка смены пароля с авторизацией")
  public void checkChangePasswordWithAuthorization() {

    String token = sendPostRequestLoginUserAndGetToken("{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
    Response response = sendPatchRequest(token, "{\"password\": \"newPass123\"}");
    checkCorrectChange(response, 200, true);
    json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"newPass123\"}";
  }

  @Test
  @DisplayName("Check change Name with authorization")
  @Description("Проверка смены имени пользователя с авторизацией")
  public void checkChangeNameWithAuthorization() {

    json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}";
    String token = sendPostRequestLoginUserAndGetToken(json);
    Response response = sendPatchRequest(token, "{\"name\": \"Pom-Pom\"}");
    checkCorrectChange(response, 200, true);
  }

  @Test
  @DisplayName("Check change Email without authorization")
  @Description("Проверка попытки смены электронной почты без авторизаци")
  public void checkChangeEmailWithoutAuthorization() {

    Response response = sendPatchRequestWithoutAuthorization("{\"email\": \"kapiko@yandex.ru\"}");
    checkStatusCodeAndBody(response, 401, "You should be authorised");
    json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}";
  }

  @Test
  @DisplayName("Check change Password without authorization")
  @Description("Проверка попытки смены пароля без авторизаци")
  public void checkChangePasswordWithoutAuthorization() {

    Response response = sendPatchRequestWithoutAuthorization("{\"password\": \"newPass123\"}");
    checkStatusCodeAndBody(response, 401, "You should be authorised");
    json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}";
  }

  @Test
  @DisplayName("Check change Name without authorization")
  @Description("Проверка попытки смены имени без авторизаци")
  public void checkChangeNameWithoutAuthorization() {

    Response response = sendPatchRequestWithoutAuthorization("{\"name\": \"Pom-Pom\"}");
    checkStatusCodeAndBody(response, 401, "You should be authorised");
    json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}";
  }
}
