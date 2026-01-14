package com.transport.ui.dashboard;

import com.transport.model.Utilisateur;
import com.transport.model.Livraison;
import com.transport.service.DemandeService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class ChauffeurDashboard {
    private Stage stage;
    private Utilisateur currentChauffeur;
    private DemandeService demandeService;
    private BorderPane root;

    public ChauffeurDashboard(Stage stage, Utilisateur chauffeur) {
        this.stage = stage;
        this.currentChauffeur = chauffeur;
        this.demandeService = new DemandeService();
    }

    public void show() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Main Content - Afficher le dashboard par défaut
        showDashboard();

        Scene scene = new Scene(root, 1200, 700);
        loadCSS(scene);

        stage.setScene(scene);
        stage.setTitle("Dashboard - Chauffeur");
        stage.centerOnScreen();
        stage.show();
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 25;");

        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);

        // Logo/Title
        Label title = new Label("🚛 Transport Express");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Info chauffeur
        VBox userInfo = new VBox(2);
        Label userName = new Label("Chauffeur: " + currentChauffeur.getNom());
        userName.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        // Charger les statistiques pour afficher le nombre de livraisons
        int nbLivraisons = getNombreLivraisonsChauffeur();
        Label userStats = new Label(nbLivraisons + " livraison(s) assignée(s)");
        userStats.setStyle("-fx-text-fill: #ecf0f1; -fx-font-size: 12px;");

        userInfo.getChildren().addAll(userName, userStats);

        // Bouton déconnexion
        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; " +
                         "-fx-background-radius: 5; -fx-padding: 8 15;");
        logoutBtn.setOnAction(e -> logout());

        topBar.getChildren().addAll(title, spacer, userInfo, logoutBtn);

        header.getChildren().addAll(topBar);
        return header;
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: #34495e; -fx-padding: 20; -fx-min-width: 250;");
        sidebar.setPrefWidth(250);

        Label chauffeurName = new Label(currentChauffeur.getNom());
        chauffeurName.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: CENTER;");

        Label chauffeurRole = new Label("CHAUFFEUR");
        chauffeurRole.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px; -fx-alignment: CENTER;");

        VBox profileBox = new VBox(5, chauffeurName, chauffeurRole);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setPadding(new Insets(0, 0, 20, 0));

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #2c3e50;");

        // Menu items
        Button dashboardBtn = createMenuButton("Tableau de bord", true);
        dashboardBtn.setOnAction(e -> showDashboard());

        Button livraisonsBtn = createMenuButton("Mes Livraisons", false);
        livraisonsBtn.setOnAction(e -> showLivraisons());

        Button statistiquesBtn = createMenuButton("Statistiques", false);
        statistiquesBtn.setOnAction(e -> showStatistiques());

        
        sidebar.getChildren().addAll(
            profileBox, separator,
            dashboardBtn, livraisonsBtn, statistiquesBtn
        );

        return sidebar;
    }

    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                           "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;";
        String activeStyle = "-fx-background-color: #3498db; -fx-text-fill: white; " +
                           "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;";
        String hoverStyle = "-fx-background-color: #2c3e50; -fx-text-fill: white; " +
                          "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;";
        
        if (active) {
            button.setStyle(activeStyle);
        } else {
            button.setStyle(normalStyle);
        }
        
        button.setOnMouseEntered(e -> {
            if (!active) {
                button.setStyle(hoverStyle);
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!active) {
                button.setStyle(normalStyle);
            }
        });
        
        return button;
    }

    private void showDashboard() {
        ScrollPane mainContent = createMainContent();
        root.setCenter(mainContent);
        
        // Mettre à jour l'état actif des boutons du menu
        updateMenuButtons("dashboard");
    }

    private ScrollPane createMainContent() {
        VBox content = new VBox(25);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white;");

        // Welcome Section
        VBox welcomeBox = new VBox(10);
        welcomeBox.setStyle("-fx-background-color: #e8f4f8; -fx-background-radius: 10; -fx-padding: 20; " +
                          "-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 10;");

        Label welcomeTitle = new Label("👋 Bonjour, " + currentChauffeur.getNom() + " !");
        welcomeTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label welcomeText = new Label("Bienvenue sur votre tableau de bord. Retrouvez ici toutes vos livraisons et statistiques.");
        welcomeText.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
        welcomeText.setWrapText(true);

        welcomeBox.getChildren().addAll(welcomeTitle, welcomeText);

        // Statistics Cards
        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(20);
        statsGrid.setVgap(20);

        // Load statistics
        int totalLivraisons = getNombreLivraisonsChauffeur();
        int livraisonsEnCours = getNombreLivraisonsEnCoursChauffeur();
        int livraisonsTerminees = getNombreLivraisonsTermineesChauffeur();

        System.out.println("📊 Statistiques chargées: Total=" + totalLivraisons + 
                         ", En cours=" + livraisonsEnCours + 
                         ", Terminées=" + livraisonsTerminees);

        // Card 1: Total Livraisons
        VBox card1 = createStatCard("📦 Total Livraisons", String.valueOf(totalLivraisons), 
                                   "#3498db", "#e8f4f8");

        // Card 2: En Cours
        VBox card2 = createStatCard("🔄 En Cours", String.valueOf(livraisonsEnCours), 
                                   "#f39c12", "#fdebd0");

        // Card 3: Terminées
        VBox card3 = createStatCard("✅ Terminées", String.valueOf(livraisonsTerminees), 
                                   "#2ecc71", "#d5f4e6");

        // Card 4: Performance
        double performance = totalLivraisons > 0 ? 
                           (double) livraisonsTerminees / totalLivraisons * 100 : 0;
        VBox card4 = createStatCard("📈 Performance", String.format("%.1f%%", performance), 
                                   "#9b59b6", "#f4ecf7");

        statsGrid.add(card1, 0, 0);
        statsGrid.add(card2, 1, 0);
        statsGrid.add(card3, 0, 1);
        statsGrid.add(card4, 1, 1);

        // Today's Deliveries
        VBox todayDeliveriesBox = new VBox(15);
        todayDeliveriesBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-padding: 20;");

        Label todayTitle = new Label("📅 Livraisons du jour");
        todayTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Get today's deliveries (utilisation d'une méthode temporaire)
        List<Livraison> livraisonsAujourdhui = getLivraisonsDuJourChauffeur();

        if (livraisonsAujourdhui.isEmpty()) {
            Label noDeliveryLabel = new Label("Aucune livraison prévue pour aujourd'hui.");
            noDeliveryLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
            todayDeliveriesBox.getChildren().addAll(todayTitle, noDeliveryLabel);
        } else {
            VBox deliveriesList = new VBox(10);
            for (Livraison livraison : livraisonsAujourdhui) {
                HBox deliveryItem = createDeliveryItem(livraison);
                deliveriesList.getChildren().add(deliveryItem);
            }
            todayDeliveriesBox.getChildren().addAll(todayTitle, deliveriesList);
        }

        content.getChildren().addAll(welcomeBox, statsGrid, todayDeliveriesBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        return scrollPane;
    }

    // Méthodes temporaires pour remplacer celles manquantes dans DemandeService
    private int getNombreLivraisonsChauffeur() {
        try {
            List<Livraison> livraisons = demandeService.getLivraisonsByChauffeurId(currentChauffeur.getId());
            return livraisons != null ? livraisons.size() : 0;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du comptage des livraisons: " + e.getMessage());
            return 0;
        }
    }

    private int getNombreLivraisonsEnCoursChauffeur() {
        try {
            List<Livraison> livraisons = demandeService.getLivraisonsByChauffeurId(currentChauffeur.getId());
            if (livraisons == null) return 0;
            
            int count = 0;
            for (Livraison liv : livraisons) {
                if ("En cours".equals(liv.getStatus())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du comptage des livraisons en cours: " + e.getMessage());
            return 0;
        }
    }

    private int getNombreLivraisonsTermineesChauffeur() {
        try {
            List<Livraison> livraisons = demandeService.getLivraisonsByChauffeurId(currentChauffeur.getId());
            if (livraisons == null) return 0;
            
            int count = 0;
            for (Livraison liv : livraisons) {
                if ("Terminée".equals(liv.getStatus())) {
                    count++;
                }
            }
            return count;
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du comptage des livraisons terminées: " + e.getMessage());
            return 0;
        }
    }

    private List<Livraison> getLivraisonsDuJourChauffeur() {
        try {
            // Pour simplifier, on retourne les 3 premières livraisons du chauffeur
            List<Livraison> allLivraisons = demandeService.getLivraisonsByChauffeurId(currentChauffeur.getId());
            if (allLivraisons == null || allLivraisons.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            
            // Limiter à 3 livraisons maximum pour l'affichage
            int limit = Math.min(allLivraisons.size(), 3);
            return allLivraisons.subList(0, limit);
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la récupération des livraisons du jour: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    private void showLivraisons() {
        new com.transport.ui.chauffeur.UpdateLivraisonView(stage, currentChauffeur).show();
        updateMenuButtons("livraisons");
    }

    private VBox createStatCard(String title, String value, String color, String bgColor) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 10; " +
                     "-fx-padding: 20; -fx-border-color: " + color + "; -fx-border-width: 2; " +
                     "-fx-border-radius: 10;");
        card.setPrefSize(250, 120);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    private HBox createDeliveryItem(Livraison livraison) {
        HBox item = new HBox(15);
        item.setStyle("-fx-background-color: white; -fx-background-radius: 5; " +
                     "-fx-padding: 15; -fx-border-color: #dee2e6; -fx-border-width: 1; " +
                     "-fx-border-radius: 5;");
        item.setAlignment(Pos.CENTER_LEFT);

        // Status indicator
        Circle statusDot = new Circle(5);
        switch (livraison.getStatus()) {
            case "En cours":
                statusDot.setFill(javafx.scene.paint.Color.web("#f39c12"));
                break;
            case "Terminée":
                statusDot.setFill(javafx.scene.paint.Color.web("#2ecc71"));
                break;
            case "Confirmée":
                statusDot.setFill(javafx.scene.paint.Color.web("#3498db"));
                break;
            default:
                statusDot.setFill(javafx.scene.paint.Color.web("#95a5a6"));
        }

        // Delivery info
        VBox infoBox = new VBox(5);
        
        Label destinationLabel = new Label("Destination: " + livraison.getDestination());
        destinationLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label detailsLabel = new Label(
            "Type: " + livraison.getTypeTransport() + " • " +
            "Distance: " + livraison.getDistanceKm() + " km • " +
            "Poids: " + livraison.getPoidsTonnes() + " tonnes • " +
            "Statut: " + livraison.getStatus());
        detailsLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");

        infoBox.getChildren().addAll(destinationLabel, detailsLabel);

        // Action button
        Button actionBtn = new Button("📋 Détails");
        actionBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                         "-fx-padding: 5 15; -fx-border-radius: 5;");
        actionBtn.setOnAction(e -> showLivraisonDetails(livraison));

        item.getChildren().addAll(statusDot, infoBox, actionBtn);
        return item;
    }

    private void showLivraisonDetails(Livraison livraison) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la livraison");
        alert.setHeaderText("Livraison #" + livraison.getId());
        alert.setContentText(
            "ID: " + livraison.getId() + "\n" +
            "Destination: " + livraison.getDestination() + "\n" +
            "Type: " + livraison.getTypeTransport() + "\n" +
            "Distance: " + String.format("%.2f", livraison.getDistanceKm()) + " km\n" +
            "Poids: " + String.format("%.2f", livraison.getPoidsTonnes()) + " tonnes\n" +
            "Client: " + livraison.getNomClient() + "\n" +
            "Statut: " + livraison.getStatus() + "\n" +
            "Prix: " + String.format("%.2f", livraison.getPrixTotal()) + " €\n" +
            "Chauffeur ID: " + livraison.getIdChauffeur() + "\n" +
            "Date: " + (livraison.getDateCreation() != null ? 
                       livraison.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A")
        );
        
        // Ajouter des boutons d'action selon le statut
        if ("Confirmée".equals(livraison.getStatus())) {
            ButtonType demarrerBtn = new ButtonType("▶ Démarrer la livraison");
            ButtonType annulerBtn = new ButtonType("Annuler");
            alert.getButtonTypes().setAll(demarrerBtn, annulerBtn);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == demarrerBtn) {
                // Appeler UpdateLivraisonView pour démarrer la livraison
                new com.transport.ui.chauffeur.UpdateLivraisonView(stage, currentChauffeur).show();
            }
        } else if ("En cours".equals(livraison.getStatus())) {
            ButtonType terminerBtn = new ButtonType("✓ Terminer la livraison");
            ButtonType annulerBtn = new ButtonType("Annuler");
            alert.getButtonTypes().setAll(terminerBtn, annulerBtn);
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == terminerBtn) {
                // Appeler UpdateLivraisonView pour terminer la livraison
                new com.transport.ui.chauffeur.UpdateLivraisonView(stage, currentChauffeur).show();
            }
        } else {
            alert.showAndWait();
        }
    }

    private void showStatistiques() {
        try {
            // Calculer les statistiques
            int total = getNombreLivraisonsChauffeur();
            int enCours = getNombreLivraisonsEnCoursChauffeur();
            int terminees = getNombreLivraisonsTermineesChauffeur();
            double performance = total > 0 ? (double) terminees / total * 100 : 0;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Statistiques");
            alert.setHeaderText("Vos statistiques de performance");
            alert.setContentText(
                "📊 Statistiques du chauffeur " + currentChauffeur.getNom() + "\n\n" +
                "📦 Total livraisons: " + total + "\n" +
                "🔄 Livraisons en cours: " + enCours + "\n" +
                "✅ Livraisons terminées: " + terminees + "\n" +
                "📈 Taux de réussite: " + String.format("%.1f", performance) + "%\n\n" +
                "📅 Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
            );
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Impossible de charger les statistiques");
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.showAndWait();
        }
        updateMenuButtons("statistiques");
    }

    private void showProfil() {
        try {
            // Récupérer les dernières livraisons du chauffeur
            List<Livraison> livraisons = demandeService.getLivraisonsByChauffeurId(currentChauffeur.getId());
            int nbLivraisons = livraisons != null ? livraisons.size() : 0;
            int nbTerminees = 0;
            
            if (livraisons != null) {
                for (Livraison liv : livraisons) {
                    if ("Terminée".equals(liv.getStatus())) {
                        nbTerminees++;
                    }
                }
            }
            
            double tauxReussite = nbLivraisons > 0 ? (double) nbTerminees / nbLivraisons * 100 : 0;
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mon Profil");
            alert.setHeaderText("Profil du chauffeur - " + currentChauffeur.getNom());
            alert.setContentText(
                "👤 Informations personnelles:\n" +
                "   Nom: " + currentChauffeur.getNom() + "\n" +
                "   ID: " + currentChauffeur.getId() + "\n" +
                "   Email: " + currentChauffeur.getEmail() + "\n\n" +
                
                "📊 Statistiques professionnelles:\n" +
                "   Total livraisons: " + nbLivraisons + "\n" +
                "   Livraisons terminées: " + nbTerminees + "\n" +
                "   Taux de réussite: " + String.format("%.1f", tauxReussite) + "%\n\n" 
            );
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Mon Profil");
            alert.setHeaderText("Profil du chauffeur");
            alert.setContentText(
                "Nom: " + currentChauffeur.getNom() + "\n" +
                "ID: " + currentChauffeur.getId() + "\n" +
                "Email: " + currentChauffeur.getEmail() + "\n\n" +
                "Erreur lors du chargement des statistiques."
            );
            alert.showAndWait();
        }
        updateMenuButtons("profil");
    }

    private void commencerJournee() {
        try {
            // Vérifier les livraisons du jour
            List<Livraison> livraisonsDuJour = getLivraisonsDuJourChauffeur();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Début de journée");
            
            if (livraisonsDuJour.isEmpty()) {
                alert.setHeaderText("Aucune livraison prévue aujourd'hui");
                alert.setContentText("Vous n'avez aucune livraison assignée pour aujourd'hui.\n" +
                                   "Consultez régulièrement votre tableau de bord pour les nouvelles affectations.");
            } else {
                alert.setHeaderText("Bonne journée " + currentChauffeur.getNom() + " !");
                StringBuilder content = new StringBuilder();
                content.append("Vous avez ").append(livraisonsDuJour.size()).append(" livraison(s) prévue(s) aujourd'hui:\n\n");
                
                for (int i = 0; i < Math.min(livraisonsDuJour.size(), 3); i++) {
                    Livraison liv = livraisonsDuJour.get(i);
                    content.append("• ").append(liv.getDestination()).append(" (").append(liv.getStatus()).append(")\n");
                }
                
                if (livraisonsDuJour.size() > 3) {
                    content.append("• ... et ").append(livraisonsDuJour.size() - 3).append(" autres\n");
                }
                
                content.append("\nCliquez sur 'Voir mes livraisons' pour les gérer.");
                alert.setContentText(content.toString());
                
                ButtonType voirLivraisonsBtn = new ButtonType("🚚 Voir mes livraisons");
                ButtonType okBtn = new ButtonType("OK");
                alert.getButtonTypes().setAll(voirLivraisonsBtn, okBtn);
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == voirLivraisonsBtn) {
                    showLivraisons();
                }
            }
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Erreur");
            errorAlert.setHeaderText("Impossible de charger les livraisons du jour");
            errorAlert.setContentText("Erreur: " + e.getMessage());
            errorAlert.showAndWait();
        }
    }

    private void signalerProbleme() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Signaler un problème");
        dialog.setHeaderText("Décrivez le problème rencontré");
        dialog.setContentText("Description:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && !result.get().trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Problème signalé");
            alert.setHeaderText("Merci pour votre signalement");
            alert.setContentText("Votre problème a été enregistré et sera traité par l'administration.\n" +
                               "ID du signalement: " + System.currentTimeMillis());
            alert.showAndWait();
        }
    }

    private void updateMenuButtons(String activeButton) {
        // Réinitialiser tous les boutons
        VBox sidebar = (VBox) root.getLeft();
        
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String btnText = btn.getText();
                
                boolean isActive = false;
                switch (activeButton) {
                    case "dashboard":
                        isActive = btnText.contains("Tableau de bord");
                        break;
                    case "livraisons":
                        isActive = btnText.contains("Mes Livraisons");
                        break;
                    case "statistiques":
                        isActive = btnText.contains("Statistiques");
                        break;
                    case "profil":
                        isActive = btnText.contains("Mon Profil");
                        break;
                }
                
                if (isActive) {
                    btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                               "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; " +
                               "-fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;");
                }
            }
        }
    }

    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Déconnexion du compte");
        confirm.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Retour à la page de connexion
            try {
                Class<?> loginClass = Class.forName("com.transport.ui.LoginView");
                java.lang.reflect.Constructor<?> constructor = loginClass.getConstructor(Stage.class);
                Object loginView = constructor.newInstance(stage);
                java.lang.reflect.Method showMethod = loginClass.getMethod("show");
                showMethod.invoke(loginView);
                
                // Fermer la connexion au service
                demandeService.closeConnection();
            } catch (Exception e) {
                e.printStackTrace();
                // Fallback si la classe LoginView n'existe pas
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Page de connexion non trouvée");
                errorAlert.setContentText("Impossible de charger la page de connexion.");
                errorAlert.showAndWait();
            }
        }
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/chauffeur-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour ChauffeurDashboard");
        }
    }
}