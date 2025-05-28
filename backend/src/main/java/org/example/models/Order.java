package org.example.models;

/**
 * Model for an order.
 */
public class Order {
    /**
     * The ID of the order.
     */
    private int id;

    /**
     * The username of the order.
     */
    private String username;

    /**
     * Creates a new order.
     */
    public Order() {
    }

    /**
     * Creates a new order.
     *
     * @param id The ID of the order.
     * @param username The username of the order.
     */
    public Order(int id, String username) {
        this.id = id;
        this.username = username;
    }

    /**
     * Gets the ID of the order.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the order.
     *
     * @param id The ID of the order.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the username of the order.
     *
     * @return String
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the order.
     *
     * @param username The username of the order.
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
