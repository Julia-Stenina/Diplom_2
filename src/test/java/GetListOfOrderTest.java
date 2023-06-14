import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.example.UserClient;
import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

public class GetListOfOrderTest {

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
    public void sendCreateOrderRequest(String token, String orderJson) {
        given().header("Content-type", "application/json").header("Authorization", token)
            .and()
            .body(orderJson)
            .when()
            .post("/api/orders");
    }

    @Step("Отправка запроса на получение списка заказов")
    public Response sendGetListOfOrderRequest(String token) {
        return given().header("Authorization", token)
            .get("/api/orders");
    }

    @Step("Проверка, что вернулся не пустой список заказов")
    public void checkListOfOrder(Response response) {
        response.then().assertThat().body("$", Matchers.hasKey("orders")).body("orders",
            Matchers.hasSize(Matchers.greaterThan(0)));
    }

    @Step("Проверка кода ошибки и текстового сообщения")
    public void checkStatusCodeAndBody(Response response, int statusCode, String message) {
        response.then().assertThat().statusCode(statusCode)
            .and()
            .body("message", equalTo(message));
    }

    @Test
    @DisplayName("Check getting list of order for authorized user")
    @Description("Проверка получения заказов авторизованного пользователя")
    public void checkGettingListOfOrderForAuthorizedUser() {
        String orderJson = "{\n"
            + "\"ingredients\": [\"61c0c5a71d1f82001bdaaa6d\",\"61c0c5a71d1f82001bdaaa6f\"]\n"
            + "}";
        String token = sendPostRequestLoginUserAndGetToken(
            "{\"email\": \"kit-kat@yandex.ru\", \"password\": \"pass12345\"}");
        sendCreateOrderRequest(token, orderJson);
        Response response = sendGetListOfOrderRequest(token);
        checkListOfOrder(response);
    }

    @Test
    @DisplayName("Check getting list of order for non-authorized user")
    @Description("Проверка получения заказов неавторизованного пользователя")
    public void checkGettingListOfOrderForNonAuthorizedUser() {
        Response response = sendGetListOfOrderRequest("");
        checkStatusCodeAndBody(response, 401, "You should be authorised");
    }

}
