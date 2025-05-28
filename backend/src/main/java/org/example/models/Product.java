package org.example.models;

import java.math.BigDecimal;

/**
 * Model for a product.
 */
public class Product {
    /**
     * The ID of the product.
     */
    private int id;

    /**
     * The name of the product.
     */
    private String name;

    /**
     * The price of the product.
     */
    private BigDecimal price;

    /**
     * Creates a new product.
     */
    public Product() {
    }

    /**
     * Creates a new product.
     *
     * @param id The ID of the product.
     * @param name The name of the product.
     * @param price The price of the product.
     */
    public Product(int id, String name, BigDecimal price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    /**
     * Gets the ID of the product.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the product.
     *
     * @param id The ID of the product.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the name of the product.
     *
     * @return String
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the product.
     *
     * @param name The name of the product.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the price of the product.
     *
     * @return BigDecimal
     */
    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Sets the price of the product.
     *
     * @param price The price of the product.
     */
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
