import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserClient;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class CreateOrderTest {

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        String json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\", \"name\": \"Kaktus\"}";
        UserClient createUser = new UserClient();
        createUser.createUser(json);
    }

    @AfterClass
    public static void deleteUser() {
        String json = "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}";
        UserClient userClient = new UserClient();
        userClient.deleteUser(json);
    }

    @Step("Отправка запроса на авторизацию и получение токена")
    public String sendPostRequestLoginUserAndGetToken(String json) {
        UserClient userClient = new UserClient();
        return userClient.loginAndGetToken(json);
    }

    @Step("Отправка запроса на создание заказа")
    public Response sendCreateOrderRequest(String token, String orderJson) {
        return given().header("Content-type", "application/json").header("Authorization", token)
            .and()
            .body(orderJson)
            .when()
            .post("/api/orders");
    }

    @Step("Проверка статус-кода и тела ответа при корректном создании заказа")
    public void checkCorrectCreatingOrder(Response response, int statusCode, boolean message) {
        response.then().assertThat().statusCode(statusCode)
            .and()
            .assertThat().body("success", equalTo(message));
    }

    @Step("Проверка кода ошибки и текстового сообщения")
    public void checkStatusCodeAndBody(Response response, int statusCode, String message) {
        response.then().assertThat().statusCode(statusCode)
            .and().body(containsString(message));
    }

    @Test
    @DisplayName("Check create correct order for authorized user")
    @Description("Проверка создания корректного заказа для авторизованного пользователя")
    public void checkCreateCorrectOrderForAuthorizedUser() {
        String orderJson = "{\n"
            + "\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\",\"61c0c5a71d1f82001bdaaa6f\"]\n"
            + "}";
        String token = sendPostRequestLoginUserAndGetToken(
            "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
        Response response = sendCreateOrderRequest(token, orderJson);
        checkCorrectCreatingOrder(response, 200, true);
    }

    @Test
    @DisplayName("Check create order with wrong hash")
    @Description("Проверка создания заказа с неверным хэшем ингредиентов")
    public void checkCreateCOrderWithWrongHash() {
        String orderJson = "{\n"
            + "\"ingredients\": [\"122345\",\"616161\"]\n"
            + "}";
        String token = sendPostRequestLoginUserAndGetToken(
            "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
        Response response = sendCreateOrderRequest(token, orderJson);
        checkStatusCodeAndBody(response, 500, "Internal Server Error");
    }

    @Test
    @DisplayName("Check create order without ingredients")
    @Description("Проверка создания заказа без ингредиентов")
    public void checkCreateCOrderWithoutIngredients() {
        String token = sendPostRequestLoginUserAndGetToken(
            "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
        Response response = sendCreateOrderRequest(token, "");
        checkStatusCodeAndBody(response, 400, "Ingredient ids must be provided");
    }

    /**
     * Следующий тест падает, так как для неавторизованного пользователя должен возвращаться код 401
     * Unauthorized, но возвращается 200 ОК. Это баг системы, найденный автотестом.
     **/
    @Test
    @DisplayName("Check create order for non-authorized user")
    @Description("Проверка создания заказа для неавторизованного пользователя")
    public void checkCreateOrderForNonAuthorizedUser() {
        String orderJson = "{\n"
            + "\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\",\"61c0c5a71d1f82001bdaaa6f\"]\n"
            + "}";
        Response response = sendCreateOrderRequest("", orderJson);
        checkStatusCodeAndBody(response, 401, "You should be authorised");
    }

}
