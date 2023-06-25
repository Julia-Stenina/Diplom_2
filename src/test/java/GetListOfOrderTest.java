import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import java.util.ArrayList;
import java.util.List;
import org.example.CreateUserPayload;
import org.example.DeleteUser;
import org.example.LoginUserPayload;
import org.example.OrderPayload;
import org.example.UserClient;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

public class GetListOfOrderTest extends DeleteUser {
    private String nameValue = "Kit-kat";
    private String emailValue = "kit-kat@yandex.ru";
    private String passwordValue = "pass12345";
    LoginUserPayload loginUserPayload = new LoginUserPayload(emailValue, passwordValue);

    @Before
    public void setUp() {
        CreateUserPayload createUserPayload = new CreateUserPayload(emailValue, passwordValue, nameValue);
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site/";
        UserClient createUser = new UserClient();
        createUser.createUser(createUserPayload);
    }

    @Step("Отправка запроса на авторизацию и получение токена")
    public String sendPostRequestLoginUserAndGetToken(LoginUserPayload json) {
        UserClient userClient = new UserClient();
        return userClient.loginAndGetToken(json);
    }

    @Step("Отправка запроса на получение списка ингредиентов")
    public Response sendRequestForListOfIngredients() {
        return given().header("Content-type", "application/json")
            .get("/api/ingredients");
    }

    @Step("Отправка запроса на создание заказа")
    public void sendCreateOrderRequest(String token) {
        String firstIngredient = sendRequestForListOfIngredients().jsonPath().getString("data[0]._id");
        String secondIngredient = sendRequestForListOfIngredients().jsonPath().getString("data[3]._id");
        List<String> ingredients = new ArrayList<>();
        ingredients.add(firstIngredient);
        ingredients.add(secondIngredient);

        OrderPayload orderPayload = new OrderPayload(ingredients);
        given().header("Content-type", "application/json").header("Authorization", token)
            .and()
            .body(orderPayload)
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
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);
        sendCreateOrderRequest(token);
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
