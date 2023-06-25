package org.example;

import java.util.List;

public class OrderPayload {
    private List<String> ingredients;

    public OrderPayload(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public OrderPayload() {
    }

    public List<String> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<String> ingredients) {
        this.ingredients = ingredients;
    }
}
