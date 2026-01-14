package com.transport.ui.dashboard;

import com.transport.model.Utilisateur;
import com.transport.service.DashboardService;
import com.transport.ui.admin.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.Map;

public class AdminDashboard {

    private Stage stage;
    private Utilisateur admin;
    private DashboardService dashboardService;

    public AdminDashboard(Stage stage, Utilisateur admin) {
        this.stage = stage;
        this.admin = admin;
        this.dashboardService = new DashboardService();
    }

    public void show() {
        // Layout principal
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        // ===== HEADER =====
        HBox header = createHeader();
        root.setTop(header);

        // ===== SIDEBAR =====
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // ===== CONTENT =====
        StackPane content = new StackPane();
        content.setStyle("-fx-background-color: #f8f9fa;");
        content.getChildren().add(createWelcomePanel());
        root.setCenter(content);

        // ===== SCENE =====
        Scene scene = new Scene(root, 1200, 700);
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Tableau de Bord Admin - Système de Gestion de Transport");
        stage.centerOnScreen();
        stage.show();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé, continuation avec styles inline");
        }
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 30;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(20);

        // Logo/Titre
        Label title = new Label("🚚 Transport Management System");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        // Info utilisateur
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(userInfo, Priority.ALWAYS);

        // CORRECTION: Vérifier si admin n'est pas null
        String nom = (admin != null) ? admin.getNom() : "Administrateur";
        Label welcome = new Label("Bienvenue, " + nom);
        welcome.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 14px;");

        Label role = new Label("Administrateur");
        role.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 3 10;");

        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        logoutBtn.setOnAction(e -> logout());

        userInfo.getChildren().addAll(welcome, role, logoutBtn);
        header.getChildren().addAll(title, userInfo);

        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox();
        sidebar.setStyle("-fx-background-color: #34495e; -fx-min-width: 250;");
        sidebar.setSpacing(5);
        sidebar.setPadding(new Insets(20, 0, 20, 0));

        // Titre sidebar
        Label menuTitle = new Label("MENU ADMIN");
        menuTitle.setStyle("-fx-text-fill: #bdc3c7; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 15 20 10 20;");
        sidebar.getChildren().add(menuTitle);

        // Boutons du menu - AJOUT DE GESTION DES LIVRAISONS
        Map<String, Runnable> menuActions = new LinkedHashMap<>();
        menuActions.put("📊 Tableau de bord", () -> showWelcome());
        menuActions.put("📦 Gestion des Livraisons", () -> new GestionLivraisonView(stage, admin).show()); // AJOUTÉ
        menuActions.put("👥 Gestion Utilisateurs", () -> new GestionUsersView(stage, admin).show());
        menuActions.put("🚚 Gestion Camions", () -> new GestionCamionsView(stage, admin).show());
        menuActions.put("💰 Gestion Tarifs", () -> new GestionTarifsView(stage, admin).show());
        menuActions.put("📈 Statistiques", () -> showStatistics());

        for (Map.Entry<String, Runnable> entry : menuActions.entrySet()) {
            Button menuBtn = createMenuButton(entry.getKey(), entry.getValue());
            sidebar.getChildren().add(menuBtn);
        }

        // Espace flexible
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        sidebar.getChildren().add(spacer);

        // Version
        Label version = new Label("v1.0.0");
        version.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px; -fx-padding: 20 0 0 20;");
        sidebar.getChildren().add(version);

        return sidebar;
    }

    private Button createMenuButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12 20; -fx-border-width: 0;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 20;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12 20;"));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    private VBox createWelcomePanel() {
        VBox welcomePanel = new VBox(20);
        welcomePanel.setAlignment(Pos.CENTER);
        welcomePanel.setPadding(new Insets(40));
        welcomePanel.setMaxWidth(600);

        Label welcomeTitle = new Label("🎯 Tableau de Bord Administrateur");
        welcomeTitle.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitle = new Label("Gérez efficacement votre flotte de transport");
        subtitle.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");

        // Stats cards
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);
        statsGrid.setAlignment(Pos.CENTER);

        try {
            Map<String, Object> stats = dashboardService.getDashboardStatistics();
            
            // Utilisation des statistiques sans revenus
            if (stats != null) {
                String[] statKeys = {"totalUsers", "totalCamions", "totalLivraisons"};
                String[] statTitles = {"Utilisateurs", "Camions", "Livraisons"};
                String[] colors = {"#3498db", "#2ecc71", "#9b59b6"};
                
                for (int i = 0; i < statKeys.length; i++) {
                    Object value = stats.get(statKeys[i]);
                    String valueStr = (value != null) ? value.toString() : "0";
                    VBox statCard = createStatCard(statTitles[i], valueStr, colors[i]);
                    statsGrid.add(statCard, i, 0);
                }
            }
        } catch (Exception e) {
            // En cas d'erreur, afficher des données par défaut
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            
            String[] stats = {"Utilisateurs", "Camions", "Livraisons"};
            String[] values = {"42", "18", "156"};
            String[] colors = {"#3498db", "#2ecc71", "#9b59b6"};
            
            for (int i = 0; i < stats.length; i++) {
                VBox statCard = createStatCard(stats[i], values[i], colors[i]);
                statsGrid.add(statCard, i, 0);
            }
        }

        // Actions rapides - AJOUT DE GESTION DES LIVRAISONS
        Label quickActionsTitle = new Label("🚀 Actions Rapides");
        quickActionsTitle.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");

        HBox quickActions = new HBox(15);
        quickActions.setAlignment(Pos.CENTER);

        Map<String, Runnable> quickActionsMap = new LinkedHashMap<>();
        quickActionsMap.put("📦 Gérer Livraisons", () -> new GestionLivraisonView(stage, admin).show()); // AJOUTÉ
        quickActionsMap.put("👥 Ajouter Utilisateur", () -> new GestionUsersView(stage, admin).show());
        quickActionsMap.put("🚚 Nouveau Camion", () -> new GestionCamionsView(stage, admin).show());
        quickActionsMap.put("💰 Modifier Tarif", () -> new GestionTarifsView(stage, admin).show());

        for (Map.Entry<String, Runnable> entry : quickActionsMap.entrySet()) {
            Button actionBtn = new Button(entry.getKey());
            actionBtn.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
            actionBtn.setOnAction(e -> entry.getValue().run());
            actionBtn.setOnMouseEntered(e -> actionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;"));
            actionBtn.setOnMouseExited(e -> actionBtn.setStyle("-fx-background-color: #ecf0f1; -fx-text-fill: #2c3e50; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;"));
            quickActions.getChildren().add(actionBtn);
        }

        welcomePanel.getChildren().addAll(welcomeTitle, subtitle, statsGrid, quickActionsTitle, quickActions);
        return welcomePanel;
    }

    private VBox createStatCard(String title, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setStyle(String.format("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0); -fx-border-color: %s; -fx-border-width: 0 0 4 0;", color));
        card.setMinWidth(180);
        card.setMinHeight(120);

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 32px; -fx-font-weight: bold;", color));

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 14px;");

        card.getChildren().addAll(valueLabel, titleLabel);
        return card;
    }

    private void showWelcome() {
        // Recharge le dashboard
        show();
    }

    private void showStatistics() {
        try {
            Map<String, Object> stats = dashboardService.getDashboardStatistics();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("📈 Statistiques Détaillées");
            alert.setHeaderText("Statistiques du système");
            
            // Corrigé: Suppression de la référence aux revenus
            String content = String.format(
                "📊 **Statistiques Globales**\n\n" +
                "👥 Utilisateurs totaux: %s\n" +
                "🚚 Camions enregistrés: %s\n" +
                "📦 Livraisons effectuées: %s\n\n" +
                "🔄 Dernière mise à jour: %s",
                stats.getOrDefault("totalUsers", "0"),
                stats.getOrDefault("totalCamions", "0"),
                stats.getOrDefault("totalLivraisons", "0"),
                new java.util.Date()
            );
            
            alert.setContentText(content);
            alert.showAndWait();
            
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de charger les statistiques: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void logout() {
        try {
            com.transport.ui.LoginView login = new com.transport.ui.LoginView(stage);
            login.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}