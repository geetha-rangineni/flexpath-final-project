package org.example.models;

/**
 * Model for an order item.
 */
public class OrderItem {
    /**
     * The ID of the order item.
     */
    private int id;

    /**
     * The ID of the order.
     */
    private int orderId;

    /**
     * The ID of the product.
     */
    private int productId;

    /**
     * The quantity of the product.
     */
    private int quantity;

    /**
     * Creates a new order item.
     */
    public OrderItem() {
    }

    /**
     * Creates a new order item.
     *
     * @param id The ID of the order item.
     * @param orderId The ID of the order.
     * @param productId The ID of the product.
     * @param quantity The quantity of the product.
     */
    public OrderItem(int id, int orderId, int productId, int quantity) {
        this.id = id;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
    }

    /**
     * Gets the ID of the order item.
     *
     * @return int
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the ID of the order item.
     *
     * @param id The ID of the order item.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Gets the ID of the order.
     *
     * @return int
     */
    public int getOrderId() {
        return orderId;
    }

    /**
     * Sets the ID of the order.
     *
     * @param orderId The ID of the order.
     */
    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    /**
     * Gets the ID of the product.
     *
     * @return int
     */
    public int getProductId() {
        return productId;
    }

    /**
     * Sets the ID of the product.
     *
     * @param productId The ID of the product.
     */
    public void setProductId(int productId) {
        this.productId = productId;
    }

    /**
     * Gets the quantity of the product.
     *
     * @return int
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets the quantity of the product.
     *
     * @param quantity The quantity of the product.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
