package praktikum;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private List<String> ingredients;

    public Order (List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public Order () {
        //this.ingredients = new ArrayList<>();
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        //this.ingredients.addAll(ingredients);
        this.ingredients = ingredients;
    }
}
