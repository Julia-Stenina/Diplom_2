import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
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
import org.junit.Before;
import org.junit.Test;

public class CreateOrderTest extends DeleteUser {

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
    public Response sendCreateOrderRequest(String token, OrderPayload order) {
        return given().header("Content-type", "application/json").header("Authorization", token)
            .and()
            .body(order)
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

        String firstIngredient = sendRequestForListOfIngredients().jsonPath().getString("data[0]._id");
        String secondIngredient = sendRequestForListOfIngredients().jsonPath().getString("data[3]._id");
        List<String> ingredients = new ArrayList<>();
        ingredients.add(firstIngredient);
        ingredients.add(secondIngredient);

        OrderPayload orderPayload = new OrderPayload(ingredients);
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);

        Response response = sendCreateOrderRequest(token, orderPayload);
        checkCorrectCreatingOrder(response, 200, true);
    }

    @Test
    @DisplayName("Check create order with wrong hash")
    @Description("Проверка создания заказа с неверным хэшем ингредиентов")
    public void checkCreateCOrderWithWrongHash() {
        List<String> ingredients = new ArrayList<>();
        ingredients.add("1234555");
        ingredients.add("95743");
        OrderPayload orderPayload = new OrderPayload(ingredients);
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);

        Response response = sendCreateOrderRequest(token, orderPayload);
        checkStatusCodeAndBody(response, 500, "Internal Server Error");
    }

    @Test
    @DisplayName("Check create order without ingredients")
    @Description("Проверка создания заказа без ингредиентов")
    public void checkCreateCOrderWithoutIngredients() {
        List<String> ingredients = new ArrayList<>();
        OrderPayload orderPayload = new OrderPayload(ingredients);
        String token = sendPostRequestLoginUserAndGetToken(loginUserPayload);

        Response response = sendCreateOrderRequest(token, orderPayload);
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
        String firstIngredient = sendRequestForListOfIngredients().jsonPath().getString("data[0]._id");
        String secondIngredient = sendRequestForListOfIngredients().jsonPath().getString("data[3]._id");
        List<String> ingredients = new ArrayList<>();
        ingredients.add(firstIngredient);
        ingredients.add(secondIngredient);

        OrderPayload orderPayload = new OrderPayload(ingredients);

        Response response = sendCreateOrderRequest("", orderPayload);
        checkStatusCodeAndBody(response, 401, "You should be authorised");
    }

}
