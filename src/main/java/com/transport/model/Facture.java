package com.transport.model;

import java.io.Serializable;
import java.util.Date;

public class Facture implements Serializable {

    private static final long serialVersionUID = 1L;

    private int id;
    private double montant;
    private int clientId;
    private Date date;

    public Facture(int id, double montant, int clientId, Date date) {
        this.id = id;
        this.montant = montant;
        this.clientId = clientId;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getMontant() {
        return montant;
    }

    public void setMontant(double montant) {
        this.montant = montant;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
