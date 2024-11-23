package praktikum;

import com.github.javafaker.Faker;
import io.qameta.allure.Step;
import io.qameta.allure.junit4.DisplayName;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static praktikum.UserCreds.credsFromUser;

public class UserLoginTest {

    Faker faker = new Faker();
    User user;
    String token;

    @Before
    public void setUp() {
        RestAssured.baseURI = "https://stellarburgers.nomoreparties.site";
        user = new User(faker.internet().emailAddress(),
                faker.internet().password(6,10),
                faker.name().firstName());
    }

    @Test
    @DisplayName("Check is login working for existing user with correct email and password")
    public void userLogin() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        sendLoginUser(user).
                then().assertThat().body("success", equalTo(true)).
                and().statusCode(200);
    }

    @Test
    @DisplayName("Check login with wrong email")
    public void userLoginWithWrongEmail() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        user.setEmail("wrong@example.ru");
        sendLoginUser(user).
                then().assertThat().body("success", equalTo(false)).
                and().statusCode(401);
    }

    @Test
    @DisplayName("Check login with wrong password")
    public void userLoginWithWrongPassword() {
        Response response = sendCreateUser(user);
        token = response.body().as(UserCreatedResponse.class).
                getAccessToken().
                replace("Bearer ", "");
        user.setPassword("123");
        sendLoginUser(user).
                then().assertThat().body("success", equalTo(false)).
                and().statusCode(401);
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

    @Step("Send POST request to /api/auth/login")
    public Response sendLoginUser(User user) {
        return given().header("Content-type", "application/json").
                and().body(credsFromUser(user)).post("/api/auth/login");
    }

    @After
    public void tearDown() {
        if (token != null) {
            sendDeleteUser(token);
        }
    }

}
