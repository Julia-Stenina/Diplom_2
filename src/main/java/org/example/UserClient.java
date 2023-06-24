package org.example;

import static io.restassured.RestAssured.given;

import io.qameta.allure.Step;
import io.restassured.response.Response;

public class UserClient {

    @Step("Создаём пользователя")
    public Response createUser(String json) {
        return given().header("Content-type", "application/json")
            .and()
            .body(json)
            .when()
            .post("/api/auth/register");
    }

    @Step("Удаляем пользователя")
    public void deleteUser(String json) {
        Response response = given().header("Content-type", "application/json").and().body(json)
            .when().post("/api/auth/login");
        int code = response.statusCode();
        if (code == 200) {
            String userToken = response.jsonPath().getString("accessToken");

            given().header("Authorization", userToken).and()
                .delete("/api/auth/user/");
        }
    }

    @Step("Метод авторизации")
    public Response logInUser(String json) {
        return given().header("Content-type", "application/json").and().body(json).when()
            .post("/api/auth/login");
    }

    @Step("Метод для авторизации пользователя и получения токена")
    public String loginAndGetToken(String json) {
        Response response = given().header("Content-type", "application/json").and().body(json)
            .when().post("/api/auth/login");
        return response.jsonPath().getString("accessToken");
    }

}
