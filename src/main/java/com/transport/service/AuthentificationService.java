package com.transport.service;

import com.transport.exception.AuthentificationException;

public class AuthentificationService {

    public void authentifier(String email, String motDePasse) throws AuthentificationException {
        if (email == null || motDePasse == null) {
            throw new AuthentificationException("Email ou mot de passe invalide");
        }
        // Simulation d'authentification
    }
}
