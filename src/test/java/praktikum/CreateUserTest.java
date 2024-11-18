package praktikum;

import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

public class CreateUserTest {

    User user;
    String token;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User("test-zda5@yandex.ru", "password", "Username-ZDA5");
    }

    @Test
    @DisplayName("Check that user can be created with valid fields")
    public void createUser() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        response.then().assertThat().body("success", equalTo(true));
    }

    @Test
    @DisplayName("Check that existing user can't be created")
    public void createExistingUser() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        sendCreateUser(user).then().
                assertThat().body("success", equalTo(false)).
                and().statusCode(403);
    }

    @Test
    @DisplayName("Check that user can't be created without required field email")
    public void createUserWithoutEmailField() {
        user.setEmail(null);
        sendCreateUser(user).then().
                assertThat().body("success", equalTo(false)).
                and().statusCode(403);
    }

    @Test
    @DisplayName("Check that user can't be created without required field password")
    public void createUserWithoutPasswordField() {
        user.setPassword(null);
        sendCreateUser(user).then().
                assertThat().body("success", equalTo(false)).
                and().statusCode(403);
    }

    @Test
    @DisplayName("Check that user can't be created without required field name")
    public void createUserWithoutNameField() {
        user.setName(null);
        sendCreateUser(user).then().
                assertThat().body("success", equalTo(false)).
                and().statusCode(403);
    }

    @Step("Send POST request to /api/auth/register")
    public Response sendCreateUser(User user) {
        return given().header("Content-type", "application/json").
                and().body(user).post("/api/auth/register");
    }

    @Step("Send DELETE request to /api/auth/user")
    public void sendDeleteUser(String token) {
        given().header("Content-type", "application/json").
                auth().oauth2(token).delete("/api/auth/user");
    }

    @After
    public void tearDown() {
        if (token != null) {
            sendDeleteUser(token);
        }
    }
}
