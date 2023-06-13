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

//  @After
//  public void deleteUser() {
//    String json = "{\"email\": \"kaktus@yandex.ru\", \"password\": \"pass12345\"}";
//    UserClient userClient = new UserClient();
//    userClient.deleteUser(json);
//  }

  @Step("Отправка запроса на авторизацию и получение токена")
  public String sendPostRequestLoginUser(String json) {
    Response auth_response = given().header("Content-type", "application/json").and().body(json)
            .when().post("/api/auth/login");
    return auth_response.jsonPath().getString("accessToken");
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

    String token = sendPostRequestLoginUser("{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
    System.out.println("token = " + token);
    String json = "{\"email\": \"kapiko@yandex.ru\"}";
    Response response = given().header("Authorization", token).header("Content-type", "application/json")
            .and()
            .body(json)
            .when()
            .patch("/api/auth/user/");
    checkCorrectChange(response, 200, true);
    //Удаление пользователя
    String json_2 = "{\"email\": \"kapiko@yandex.ru\", \"password\": \"pass12345\"}";
    UserClient userClient = new UserClient();
    userClient.deleteUser(json_2);
  }
}
