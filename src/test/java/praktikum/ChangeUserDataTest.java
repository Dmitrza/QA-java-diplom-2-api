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
import static org.junit.Assert.assertEquals;

public class ChangeUserDataTest {

    User user;
    User secondUser;
    String token;
    String tokenForSecondUser;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User("dmitrz20@example.ru", "password", "Dmitrii");
        secondUser = new User("aleksandr20@example.ru", "56789", "Aleksandr");
    }

    @Test
    @DisplayName("Check user email can be changed with authorization")
    public void changeUserEmailWithAuthorization() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        user.setEmail("aleksandr20@example.ru");
        user.setPassword("null");
        sendChangeUserDataWithAuthorization(user);
        User userData = sendGetUserData().as(UserData.class).getUser();
        assertEquals("aleksandr20@example.ru", userData.getEmail());
    }

    @Test
    @DisplayName("Check user name can be changed with authorization")
    public void changeUserNameWithAuthorization() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        user.setPassword("null");
        user.setName("Aleksandr");
        sendChangeUserDataWithAuthorization(user);
        User userData = sendGetUserData().as(UserData.class).getUser();
        assertEquals("Aleksandr", userData.getName());
    }

    @Test
    @DisplayName("Check user data can't be changed without authorization")
    public void changeUserDataWithoutAuthorization() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        user.setEmail("aleksandr20@example.ru");
        user.setPassword("null");
        user.setName("Aleksandr");
        sendChangeUserDataWithoutAuthorization(user).
                then().assertThat().body("message", equalTo("You should be authorised")).
                and().statusCode(401);
    }

    @Test
    @DisplayName("Check user data can't be changed with existing email")
    public void changeUserDataWithExistingEmail() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        response = sendCreateUser(secondUser);
        tokenForSecondUser = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        user.setEmail(secondUser.getEmail());
        user.setPassword("null");
        user.setName("Aleksandr");
        sendChangeUserDataWithAuthorization(user).
                then().assertThat().
                body("message", equalTo("User with such email already exists")).
                and().statusCode(403);
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

    @Step("Send PATCH request to /api/auth/user with authorization")
    public Response sendChangeUserDataWithAuthorization(User user) {
        return given().header("Content-type", "application/json").
                auth().oauth2(token).and().body(user).
                patch("/api/auth/user");
    }

    @Step("Send PATCH request to /api/auth/user without authorization")
    public Response sendChangeUserDataWithoutAuthorization(User user) {
        return given().header("Content-type", "application/json").
                and().body(user).
                patch("/api/auth/user");
    }

    @Step("Send GET request to /api/auth/user")
    public Response sendGetUserData() {
        return given().header("Content-type", "application/json").
                auth().oauth2(token).
                get("/api/auth/user");
    }

    @After
    public void tearDown() {
        if (token != null) {
            sendDeleteUser(token);
        }
        if (tokenForSecondUser != null) {
            sendDeleteUser(tokenForSecondUser);
        }
    }

}
