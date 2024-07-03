package com.example.prueba;

public class RecipeItem {
    private String id;
    private String name;

    public RecipeItem(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name; // Para que el Spinner muestre el nombre de la receta
    }
}
