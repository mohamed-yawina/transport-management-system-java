package com.transport.controller;

import com.transport.model.Utilisateur;
import com.transport.service.AuthentificationService;
import com.transport.ui.dashboard.AdminDashboard;
import com.transport.ui.dashboard.ClientDashboard;
import com.transport.ui.dashboard.ChauffeurDashboard;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheckBox;
    @FXML private Button loginButton;
    @FXML private Label messageLabel;
    @FXML private VBox loginForm;
    @FXML private StackPane loadingPane;
    @FXML private Label message; // Garder l'ancien pour compatibilité

    private AuthentificationService authService = new AuthentificationService();

    @FXML
    public void initialize() {
        // Initialiser les champs si vous avez gardé l'ancien FXML
        if (messageLabel == null && message != null) {
            messageLabel = message;
        }
        
        // Focus sur le champ email au démarrage
        Platform.runLater(() -> {
            if (emailField != null) {
                emailField.requestFocus();
            }
        });
        
        // Ajouter validation en temps réel
        setupValidation();
    }
    
    private void setupValidation() {
        if (emailField != null) {
            emailField.textProperty().addListener((obs, oldVal, newVal) -> {
                validateEmail();
            });
        }
        
        if (passwordField != null) {
            passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
                validatePassword();
            });
        }
    }
    
    private void validateEmail() {
        String email = emailField.getText().trim();
        if (!email.isEmpty() && !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showFieldError(emailField, "Format email invalide");
        } else {
            clearFieldError(emailField);
        }
    }
    
    private void validatePassword() {
        String password = passwordField.getText();
        if (!password.isEmpty() && password.length() < 6) {
            showFieldError(passwordField, "Min. 6 caractères");
        } else {
            clearFieldError(passwordField);
        }
    }
    
    private void showFieldError(Control field, String tooltipMsg) {
        field.setStyle("-fx-border-color: #e74c3c;");
        if (field.getTooltip() == null) {
            Tooltip tooltip = new Tooltip(tooltipMsg);
            tooltip.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
            field.setTooltip(tooltip);
        }
    }
    
    private void clearFieldError(Control field) {
        field.setStyle("-fx-border-color: #bdc3c7;");
        field.setTooltip(null);
    }
    
    private void showLoading(boolean show) {
        if (loadingPane != null) {
            loadingPane.setVisible(show);
        }
        if (loginForm != null) {
            loginForm.setDisable(show);
        }
        if (loginButton != null) {
            loginButton.setDisable(show);
        }
    }

    @FXML
    private void login() {
        // Validation de base
        String email = emailField.getText().trim();
        String password = passwordField.getText();
        
        if (email.isEmpty() || password.isEmpty()) {
            showMessage("❌ Tous les champs sont obligatoires", "#e74c3c");
            return;
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showMessage("❌ Format d'email invalide", "#e74c3c");
            return;
        }
        
        // Afficher le chargement si disponible
        showLoading(true);
        
        try {
            Utilisateur user = authService.authentifier(email, password);
            
            Stage stage = (Stage) emailField.getScene().getWindow();
            
            // Petite pause pour l'effet visuel
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Simule un temps de chargement
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                Platform.runLater(() -> {
                    showLoading(false);
                    switch (user.getRole()) {
                        case "ADMIN" -> new AdminDashboard(stage, user).show();
                        case "CLIENT" -> new ClientDashboard(stage, user).show();
                        case "CHAUFFEUR" -> new ChauffeurDashboard(stage, user).show();
                        default -> showMessage("❌ Rôle non reconnu", "#e74c3c");
                    }
                });
            }).start();
            
        } catch (Exception e) {
            showLoading(false);
            showMessage("❌ Identifiants invalides", "#e74c3c");
            // Effet de vibration sur les champs
            shakeField(emailField);
            shakeField(passwordField);
        }
    }
    
    @FXML
    private void forgotPassword() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Mot de passe oublié");
        alert.setHeaderText("Réinitialisation du mot de passe");
        alert.setContentText("Contactez l'administrateur pour réinitialiser votre mot de passe.");
        alert.showAndWait();
    }
    
    private void showMessage(String text, String color) {
        if (messageLabel != null) {
            messageLabel.setText(text);
            messageLabel.setStyle("-fx-text-fill: " + color + ";");
        } else if (message != null) {
            message.setText(text);
            message.setStyle("-fx-text-fill: " + color + ";");
        }
    }
    
    private void shakeField(Control field) {
        // Animation simple de secousse
        javafx.animation.TranslateTransition tt = 
                new javafx.animation.TranslateTransition(javafx.util.Duration.millis(50), field);
        tt.setFromX(0);
        tt.setByX(10);
        tt.setCycleCount(4);
        tt.setAutoReverse(true);
        tt.play();
    }
}