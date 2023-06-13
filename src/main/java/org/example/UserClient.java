package org.example;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;

public class UserClient {

   //метод для создания пользователя
    public Response createUser(String json) {
        return given().header("Content-type", "application/json")
            .and()
            .body(json)
            .when()
            .post("/api/auth/register");
    }

    //метод для удаления пользователя
    public void deleteUser(String json) {
        Response response = given().header("Content-type", "application/json").and().body(json)
            .when().post("/api/auth/login");
        int code = response.statusCode();
        if (code == 200) {
            String userToken = response.jsonPath().getString("accessToken");
            //TODO удалить вывод токена
            System.out.println("Token = " + userToken);

            given().header("Authorization", userToken).and()
                .delete("/api/auth/user/");
        }
    }

    //метод для авторизации пользователя
    public Response logInUser(String json) {
        return given().header("Content-type", "application/json").and().body(json).when()
            .post("/api/auth/login");
    }

    //метод для авторизации пользователя и получения токена
    public String loginAndGetToken(String json) {
        Response response = given().header("Content-type", "application/json").and().body(json)
            .when().post("/api/auth/login");
            return response.jsonPath().getString("accessToken");
    }

}
