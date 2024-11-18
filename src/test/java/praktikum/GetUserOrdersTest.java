package praktikum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

public class GetUserOrdersTest {
    User user;
    String token;
    Order order;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User("dmitrz20@example.ru", "password", "Dmitrii");
        order = new Order();
        List<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71d1f82001bdaaa6d");
        ingredients.add("61c0c5a71d1f82001bdaaa6f");
        order.setIngredients(ingredients);
    }

    @Test
    @DisplayName("Check user orders with authorization")
    public void getUserOrdersWithAuthorization() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        sendCreateOrderWithAuthorization(order, token);
        sendGetUserOrders(token).then().
                assertThat().
                body("orders", is(notNullValue())).and().
                statusCode(200);
    }

    @Test
    @DisplayName("Check user orders without authorization")
    public void getUserOrdersWithoutAuthorization() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        sendCreateOrderWithAuthorization(order, token);
        sendGetUserOrdersWithoutAuthorization().then().
                assertThat().
                body("message", equalTo("You should be authorised")).and().
                statusCode(401);
    }

    @Step("Send POST request to /api/auth/register")
    public Response sendCreateUser(User user) {
        return given().header("Content-type", "application/json").
                and().body(user).
                post("/api/auth/register");
    }

    @Step("Send DELETE request to /api/auth/user")
    public void sendDeleteUser(String token) {
        given().header("Content-type", "application/json").
                auth().oauth2(token).
                delete("/api/auth/user");
    }

    @Step("Send POST request to /api/orders with authorization")
    public Response sendCreateOrderWithAuthorization(Order order, String token) {
        return given().header("Content-type", "application/json").
                auth().oauth2(token).
                and().body(order).
                post("/api/orders");
    }

    @Step("Send POST request to /api/orders without authorization")
    public Response sendGetUserOrders(String token) {
        return given().header("Content-type", "application/json").
                auth().oauth2(token).and().
                get("/api/orders");
    }

    @Step("Send POST request to /api/orders without authorization")
    public Response sendGetUserOrdersWithoutAuthorization() {
        return given().header("Content-type", "application/json").
                and().get("/api/orders");
    }

    @After
    public void tearDown() {
        if (token != null) {
            sendDeleteUser(token);
        }
    }

}
