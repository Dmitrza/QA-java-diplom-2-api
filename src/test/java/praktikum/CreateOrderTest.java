package praktikum;

import com.github.javafaker.Faker;
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

public class CreateOrderTest {
    Faker faker = new Faker();
    User user;
    String token;
    Order order;

    @Before
    public void setUp(){
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User(faker.internet().emailAddress(),
                faker.internet().password(6,10),
                faker.name().firstName());
        order = new Order();
        List<String> ingredients = new ArrayList<>();
        ingredients.add("61c0c5a71d1f82001bdaaa6d");
        ingredients.add("61c0c5a71d1f82001bdaaa6f");
        order.setIngredients(ingredients);
    }

    @Test
    @DisplayName("Check order can be created with authorization and ingredients")
    public void createOrder() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        sendCreateOrderWithAuthorization(order, token).
                then().assertThat().
                body("order", is(notNullValue())).
                and().statusCode(200);
    }

    @Test
    @DisplayName("Check order can be created without authorization")
    public void createOrderWithoutAuthorization() {
        sendCreateOrderWithoutAuthorization(order).
                then().assertThat().
                body("order", is(notNullValue())).
                and().statusCode(200);
    }

    @Test
    @DisplayName("Check order can't be created without ingredients")
    public void createOrderWithoutIngredients() {
        order.setIngredients(null);
        sendCreateOrderWithoutAuthorization(order).
                then().assertThat().
                body("message", equalTo("Ingredient ids must be provided")).
                and().statusCode(400);
    }

    @Test
    @DisplayName("Check order can't be created with wrong ingredients hash")
    public void createOrderWithWrongIngredientsHash() {
        List<String> ingredients = order.getIngredients();
        ingredients.set(0, "61c0c5a71d1f82001bdaaa");
        order.setIngredients(ingredients);
        sendCreateOrderWithoutAuthorization(order).
                then().assertThat().statusCode(500);
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
    public Response sendCreateOrderWithoutAuthorization(Order order) {
        return given().header("Content-type", "application/json").
                body(order).
                post("/api/orders");
    }

    @After
    public void tearDown() {
        if (token != null) {
            sendDeleteUser(token);
        }
    }

}
