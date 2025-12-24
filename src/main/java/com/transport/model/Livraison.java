package com.transport.model;

import java.io.Serializable;

public class Livraison implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private String destination;
    private String status;

    public Livraison(int id, String destination, String status) {
        this.id = id;
        this.destination = destination;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
