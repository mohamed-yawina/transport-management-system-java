package com.transport.ui.dashboard;

import com.transport.model.Utilisateur;
import com.transport.service.DemandeService;
import com.transport.service.FactureService;
import com.transport.ui.client.TarifsTransportView;
import com.transport.ui.client.DemandeTransportView;
import com.transport.ui.client.SuivieTransportView;
import com.transport.ui.client.FactureClient;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;
import java.util.Optional;

public class ClientDashboard {

    private Stage stage;
    private Utilisateur currentClient;
    private BorderPane root;
    private Label welcomeLabel;
    private DemandeService demandeService;
    private FactureService factureService;
    
    // Labels pour les statistiques
    private Label demandesValueLabel;
    private Label enCoursValueLabel;
    private Label termineesValueLabel;
    private Label facturesValueLabel;

    public ClientDashboard(Stage stage, Utilisateur client) {
        this.stage = stage;
        this.currentClient = client;
        this.demandeService = new DemandeService();
        this.factureService = new FactureService();
    }

    public void show() {
        root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f8f9fa, #e9ecef);");

        // Header
        VBox header = createHeader();
        root.setTop(header);

        // Sidebar
        VBox sidebar = createSidebar();
        root.setLeft(sidebar);

        // Content Area - Afficher le dashboard par défaut
        showDashboard();

        Scene scene = new Scene(root, 1200, 700);
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Tableau de Bord Client - " + currentClient.getNom());
        stage.centerOnScreen();
        stage.show();
        
        // Charger les statistiques initiales
        loadStatistics();
    }

    private VBox createHeader() {
        VBox header = new VBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 25;");

        HBox topBar = new HBox();
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setSpacing(15);

        // Logo/Titre
        Label title = new Label("TransportPro Client");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Info client
        welcomeLabel = new Label("Bienvenue, " + currentClient.getNom());
        welcomeLabel.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");

        // Bouton déconnexion
        Button logoutBtn = new Button("Déconnexion");
        logoutBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        logoutBtn.setOnAction(e -> logout());

        topBar.getChildren().addAll(title, spacer, welcomeLabel, logoutBtn);

        // Statistiques rapides
        HBox statsBar = createQuickStats();
        statsBar.setPadding(new Insets(10, 0, 0, 0));

        header.getChildren().addAll(topBar, statsBar);
        return header;
    }

    private HBox createQuickStats() {
        HBox statsBar = new HBox(20);
        statsBar.setAlignment(Pos.CENTER_LEFT);

        // Initialiser les labels de statistiques
        demandesValueLabel = new Label("0");
        enCoursValueLabel = new Label("0");
        termineesValueLabel = new Label("0");
        facturesValueLabel = new Label("0");

        VBox stat1 = createStatBox("Demandes", demandesValueLabel, "#3498db");
        VBox stat2 = createStatBox("En cours", enCoursValueLabel, "#f39c12");
        VBox stat3 = createStatBox("Terminées", termineesValueLabel, "#2ecc71");
        VBox stat4 = createStatBox("Factures", facturesValueLabel, "#9b59b6");

        statsBar.getChildren().addAll(stat1, stat2, stat3, stat4);
        return statsBar;
    }

    private VBox createStatBox(String label, Label valueLabel, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-background-radius: 10;");

        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 24px; -fx-font-weight: bold;", color));

        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 14px;");

        box.getChildren().addAll(valueLabel, descLabel);
        return box;
    }

    private void loadStatistics() {
        try {
            int clientId = currentClient.getId();
            
            // Récupérer toutes les livraisons du client
            List<com.transport.model.Livraison> livraisons = demandeService.getLivraisonsByClient(clientId);
            List<com.transport.model.Facture> factures = factureService.getFacturesByClientId(clientId);
            
            if (livraisons != null) {
                int totalDemandes = livraisons.size();
                int enCours = 0;
                int terminees = 0;
                
                for (com.transport.model.Livraison liv : livraisons) {
                    if ("En cours".equals(liv.getStatus())) {
                        enCours++;
                    } else if ("Terminée".equals(liv.getStatus())) {
                        terminees++;
                    }
                }
                
                // SUPPRIMER la partie sur les factures impayées
                // Calculer simplement le nombre total de factures
                int nbFactures = 0;
                if (factures != null) {
                    nbFactures = factures.size();
                }
                
                // Mettre à jour les labels
                demandesValueLabel.setText(String.valueOf(totalDemandes));
                enCoursValueLabel.setText(String.valueOf(enCours));
                termineesValueLabel.setText(String.valueOf(terminees));
                facturesValueLabel.setText(String.valueOf(nbFactures)); // Nombre de factures au lieu du montant
                
                System.out.println("✅ Statistiques chargées: " + totalDemandes + " demandes, " + 
                                 enCours + " en cours, " + terminees + " terminées, " +
                                 nbFactures + " factures");
            } else {
                System.out.println("⚠️ Aucune livraison trouvée pour le client ID: " + clientId);
                facturesValueLabel.setText("0");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setStyle("-fx-background-color: #34495e; -fx-padding: 20; -fx-min-width: 250;");
        sidebar.setPrefWidth(250);

        // Photo de profil (gestion d'erreur si l'image n'existe pas)
        ImageView profileImage;
        try {
            Image image = new Image(getClass().getResourceAsStream("/com/transport/ressources/profile.png"));
            profileImage = new ImageView(image);
            profileImage.setFitWidth(80);
            profileImage.setFitHeight(80);
        } catch (Exception e) {
            // Image par défaut si non trouvée
            profileImage = new ImageView();
            profileImage.setFitWidth(80);
            profileImage.setFitHeight(80);
            profileImage.setStyle("-fx-background-color: #3498db; -fx-background-radius: 40;");
        }

        Label clientName = new Label(currentClient.getNom());
        clientName.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-alignment: CENTER;");

        Label clientEmail = new Label(currentClient.getEmail());
        clientEmail.setStyle("-fx-text-fill: #bdc3c7; -fx-font-size: 12px; -fx-alignment: CENTER;");

        VBox profileBox = new VBox(5, profileImage, clientName, clientEmail);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setPadding(new Insets(0, 0, 20, 0));

        // Menu items SANS ICÔNES
        Button dashboardBtn = createMenuButton("Tableau de bord", true);
        dashboardBtn.setOnAction(e -> {
            showDashboard();
            loadStatistics(); // Recharger les stats quand on revient au dashboard
        });

        Button tarifsBtn = createMenuButton("Voir les tarifs", false);
        tarifsBtn.setOnAction(e -> showTarifs());

        Button demandeBtn = createMenuButton("Nouvelle demande", false);
        demandeBtn.setOnAction(e -> showDemandeTransport());

        Button suivieBtn = createMenuButton("Suivre demande", false);
        suivieBtn.setOnAction(e -> showSuivieTransport());

        Button facturesBtn = createMenuButton("Mes factures", false);
        facturesBtn.setOnAction(e -> showFactures());

        // Bouton rafraîchir SANS ICÔNE
        Button refreshBtn = new Button("Rafraîchir");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 15; -fx-max-width: Infinity;");
        refreshBtn.setOnAction(e -> {
            loadStatistics();
            showDashboard(); // Recharger aussi le dashboard
        });

        // Séparateur
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #2c3e50;");

        sidebar.getChildren().addAll(
            profileBox, separator,
            dashboardBtn, tarifsBtn, demandeBtn, suivieBtn, 
            facturesBtn, new Separator(), refreshBtn
        );

        return sidebar;
    }

    private Button createMenuButton(String text, boolean active) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        
        String normalStyle = "-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;";
        String activeStyle = "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;";
        String hoverStyle = "-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;";
        
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
        // Contenu du dashboard
        VBox dashboardContent = new VBox(20);
        dashboardContent.setPadding(new Insets(20));
        dashboardContent.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Tableau de Bord Client");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Cartes d'information avec données réelles
        GridPane cardsGrid = new GridPane();
        cardsGrid.setHgap(20);
        cardsGrid.setVgap(20);
        cardsGrid.setPadding(new Insets(20, 0, 0, 0));

        try {
            int clientId = currentClient.getId();
            List<com.transport.model.Livraison> livraisons = demandeService.getLivraisonsByClient(clientId);
            List<com.transport.model.Facture> factures = factureService.getFacturesByClientId(clientId);
            
            // Carte 1: Dernières demandes
            StringBuilder recentDemandes = new StringBuilder();
            if (livraisons != null && !livraisons.isEmpty()) {
                int count = Math.min(livraisons.size(), 3);
                for (int i = 0; i < count; i++) {
                    com.transport.model.Livraison liv = livraisons.get(i);
                    recentDemandes.append("• #").append(liv.getId())
                                 .append(" - ").append(liv.getDestination())
                                 .append("\n  Statut: ").append(liv.getStatus())
                                 .append("\n  Prix: ").append(String.format("%.2f", liv.getPrixTotal())).append(" €")
                                 .append("\n\n");
                }
                if (livraisons.size() > 3) {
                    recentDemandes.append("... et ").append(livraisons.size() - 3).append(" autres");
                }
            } else {
                recentDemandes.append("Aucune demande enregistrée\n");
                recentDemandes.append("Créez votre première demande\npour commencer.");
            }
            // Titre SANS ICÔNE
            VBox recentCard = createDashboardCard("Dernières demandes", recentDemandes.toString(), "#3498db");
            
            // Carte 2: Statut actuel
            StringBuilder statusText = new StringBuilder();
            if (livraisons != null && !livraisons.isEmpty()) {
                boolean hasActive = false;
                for (com.transport.model.Livraison liv : livraisons) {
                    if ("En cours".equals(liv.getStatus())) {
                        hasActive = true;
                        statusText.append("Livraison #").append(liv.getId())
                                 .append("\nDest: ").append(liv.getDestination())
                                 .append("\nStatut: ").append(liv.getStatus())
                                 .append("\nChauffeur: ").append(liv.getChauffeurNom() != null ? liv.getChauffeurNom() : "À assigner")
                                 .append("\n\n");
                    }
                }
                if (!hasActive) {
                    statusText.append("Aucune livraison en cours\n");
                    statusText.append("Toutes les livraisons sont\nterminées ou en attente.");
                }
            } else {
                statusText.append("Aucune livraison en cours\n");
                statusText.append("Créez votre première\ndemande de transport.");
            }
            // Titre SANS ICÔNE
            VBox statusCard = createDashboardCard("Statut actuel", statusText.toString(), "#2ecc71");
            
            // Carte 3: Factures récentes (MODIFIÉ - plus de statut de paiement)
            StringBuilder invoiceText = new StringBuilder();
            int nbFactures = 0;
            if (factures != null && !factures.isEmpty()) {
                nbFactures = factures.size();
                // Afficher seulement les 2 dernières factures
                int count = Math.min(factures.size(), 2);
                for (int i = 0; i < count; i++) {
                    com.transport.model.Facture facture = factures.get(i);
                    invoiceText.append("• ").append(facture.getNumeroFacture())
                             .append("\n  Montant: ").append(String.format("%.2f", facture.getMontantTTC())).append(" €")
                             .append("\n  Échéance: ").append(facture.getDateEcheance())
                             .append("\n\n");
                }
                if (factures.size() > 2) {
                    invoiceText.append("... et ").append(factures.size() - 2).append(" autres");
                }
            } else {
                invoiceText.append("Aucune facture\n");
                invoiceText.append("Créez une demande pour\ngénérer une facture.");
            }
            // Titre SANS ICÔNE
            VBox invoiceCard = createDashboardCard("Factures récentes", invoiceText.toString(), "#f39c12");
            
            // Carte 4: Actions rapides - contenu SANS ICÔNES dans le texte
            String quickActions = "ACTIONS DISPONIBLES:\n\n" +
                                "• Nouvelle demande\n" +
                                "• Voir les tarifs\n" +
                                "• Consulter factures\n" +
                                "• Suivre livraison\n" +
                                "• Actualiser données";
            // Titre SANS ICÔNE
            VBox quickActionsCard = createDashboardCard("Actions rapides", quickActions, "#9b59b6");

            // Ajouter les 4 cartes dans une grille 2x2
            cardsGrid.add(recentCard, 0, 0);      // Première ligne, première colonne
            cardsGrid.add(statusCard, 1, 0);      // Première ligne, deuxième colonne
            cardsGrid.add(invoiceCard, 0, 1);     // Deuxième ligne, première colonne
            cardsGrid.add(quickActionsCard, 1, 1); // Deuxième ligne, deuxième colonne
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des données du dashboard: " + e.getMessage());
            e.printStackTrace();
            
            // Cartes par défaut en cas d'erreur
            String errorMsg = "Erreur de chargement\nVeuillez réessayer\nou contacter le support.";
            VBox recentCard = createDashboardCard("Dernières demandes", errorMsg, "#3498db");
            VBox statusCard = createDashboardCard("Statut actuel", errorMsg, "#2ecc71");
            VBox invoiceCard = createDashboardCard("Factures récentes", errorMsg, "#f39c12");
            VBox quickActionsCard = createDashboardCard("Actions rapides", 
                "• Nouvelle demande\n• Voir les tarifs\n• Consulter factures\n• Suivre livraison", "#9b59b6");
            
            cardsGrid.add(recentCard, 0, 0);
            cardsGrid.add(statusCard, 1, 0);
            cardsGrid.add(invoiceCard, 0, 1);
            cardsGrid.add(quickActionsCard, 1, 1);
        }

        dashboardContent.getChildren().addAll(title, cardsGrid);
        root.setCenter(dashboardContent);
        
        // Mettre à jour l'état actif des boutons du menu
        updateMenuButtons("dashboard");
    }

    private VBox createDashboardCard(String title, String content, String color) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(20));
        card.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 15; " +
            "-fx-border-color: rgba(255,255,255,0.3); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0.2, 0, 2);", 
            color
        ));
        card.setPrefSize(300, 200);
        card.setAlignment(Pos.TOP_LEFT);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        // Utiliser un Label avec wrapText pour l'affichage
        Label contentLabel = new Label(content);
        contentLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-family: 'Arial';");
        contentLabel.setWrapText(true);
        contentLabel.setAlignment(Pos.TOP_LEFT);
        contentLabel.setMaxWidth(260);
        contentLabel.setPrefHeight(120);
        
        // Permettre la sélection de texte pour faciliter la lecture
        contentLabel.setMouseTransparent(false);

        card.getChildren().addAll(titleLabel, contentLabel);
        return card;
    }

    private void showTarifs() {
        new TarifsTransportView(stage, currentClient).show();
        updateMenuButtons("tarifs");
    }

    private void showDemandeTransport() {
        new DemandeTransportView(stage, currentClient).show();
        updateMenuButtons("demande");
    }

    private void showSuivieTransport() {
        new SuivieTransportView(stage, currentClient).show();
        updateMenuButtons("suivie");
    }

    private void showFactures() {
        new FactureClient(stage, currentClient).show();
        updateMenuButtons("factures");
    }

    private void updateMenuButtons(String activeButton) {
        VBox sidebar = (VBox) root.getLeft();
        
        for (javafx.scene.Node node : sidebar.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                String btnText = btn.getText();
                
                // Vérifier si c'est le bouton Rafraîchir
                if (btnText.contains("Rafraîchir")) {
                    // Laisser le style du bouton Rafraîchir inchangé
                    continue;
                }
                
                boolean isActive = false;
                switch (activeButton) {
                    case "dashboard":
                        isActive = btnText.contains("Tableau de bord");
                        break;
                    case "tarifs":
                        isActive = btnText.contains("Voir les tarifs");
                        break;
                    case "demande":
                        isActive = btnText.contains("Nouvelle demande");
                        break;
                    case "suivie":
                        isActive = btnText.contains("Suivre demande");
                        break;
                    case "factures":
                        isActive = btnText.contains("Mes factures");
                        break;
                }
                
                if (isActive) {
                    btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ecf0f1; -fx-font-size: 14px; -fx-padding: 12 15; -fx-background-radius: 5;");
                }
            }
        }
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/client-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé pour ClientDashboard");
        }
    }

    private void logout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Déconnexion");
        confirm.setHeaderText("Déconnexion du compte");
        confirm.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                Class<?> loginClass = Class.forName("com.transport.ui.LoginView");
                java.lang.reflect.Constructor<?> constructor = loginClass.getConstructor(Stage.class);
                Object loginView = constructor.newInstance(stage);
                java.lang.reflect.Method showMethod = loginClass.getMethod("show");
                showMethod.invoke(loginView);
                
                // Fermer les connexions aux services
                if (demandeService != null) {
                    demandeService.closeConnection();
                }
                if (factureService != null) {
                    factureService.closeConnection();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}