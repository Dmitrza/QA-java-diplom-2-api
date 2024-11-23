package praktikum;

public class User {

    private String email;
    private String password;
    private String name;

    public User (String email, String password, String name) {
        this.email = email;
        this.password = password;
        this.name = name;
    }

    public User () {}

    public String getEmail() {
        return email;
    }

    public User setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public User setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getName() {
        return name;
    }

    public User setName(String name) {
        this.name = name;
        return this;
    }
}
