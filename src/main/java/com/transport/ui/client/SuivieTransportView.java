package com.transport.ui.client;

import com.transport.model.Utilisateur;
import com.transport.model.Livraison;
import com.transport.service.DemandeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SuivieTransportView {

    private Stage stage;
    private Utilisateur currentClient;
    private TableView<Livraison> table;
    private ObservableList<Livraison> livraisons = FXCollections.observableArrayList();
    private DemandeService demandeService;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SuivieTransportView(Stage stage, Utilisateur client) {
        this.stage = stage;
        this.currentClient = client;
        this.demandeService = new DemandeService();
        loadLivraisonsFromDatabase();
    }

    private void loadLivraisonsFromDatabase() {
        try {
            livraisons.clear();
            List<Livraison> dbLivraisons = demandeService.getLivraisonsByClient(currentClient.getId());
            
            if (dbLivraisons != null && !dbLivraisons.isEmpty()) {
                livraisons.addAll(dbLivraisons);
                System.out.println("✅ " + livraisons.size() + " livraisons chargées depuis la BD pour le client " + currentClient.getId());
                
                // Afficher les livraisons dans la console pour vérification
                for (Livraison livraison : livraisons) {
                    System.out.println("  - ID: " + livraison.getId() + 
                                     ", Destination: " + livraison.getDestination() +
                                     ", Statut: " + livraison.getStatus());
                }
            } else {
                System.out.println("⚠️ Aucune livraison trouvée dans la base de données pour ce client");
                // Ajouter un message d'information pour l'utilisateur
                showAlert("Information", "Vous n'avez aucune livraison en cours.\nCliquez sur 'Nouvelle demande' pour créer votre première commande.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors du chargement des livraisons: " + e.getMessage());
            showAlert("Erreur", "Impossible de charger vos livraisons depuis la base de données.");
        }
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader("📍 Suivi des Demandes");
        root.setTop(header);

        // Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Suivi de vos Demandes de Transport");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Suivez l'état de toutes vos demandes en temps réel");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Table des livraisons
        table = createLivraisonsTable();
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(createTableToolbar(), table);

        HBox mainContent = new HBox(20);
        mainContent.getChildren().addAll(tableContainer);
        HBox.setHgrow(tableContainer, Priority.ALWAYS);

        content.getChildren().addAll(title, subtitle, mainContent);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 700);
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Suivi des Demandes - Client");
        stage.centerOnScreen();
        stage.show();
    }

    private HBox createHeader(String title) {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 25;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 5; -fx-padding: 8 15;");
        backBtn.setOnAction(e -> goBack());

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label countLabel = new Label(livraisons.size() + " demandes");
        countLabel.setStyle("-fx-text-fill: #f39c12; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 5 15; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, titleLabel, spacer, countLabel);
        return header;
    }

    private HBox createTableToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher une demande...");
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;");
        searchField.setPrefWidth(300);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterLivraisons(newValue);
        });

        ComboBox<String> statusFilter = new ComboBox<>();
        statusFilter.getItems().addAll("Tous", "En attente", "Confirmée", "En cours", "Terminée", "Annulée");
        statusFilter.setValue("Tous");
        statusFilter.setStyle("-fx-background-radius: 5; -fx-pref-width: 150;");
        statusFilter.setOnAction(e -> filterByStatus(statusFilter.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> refreshData());

        Button newDemandeBtn = new Button("🚛 Nouvelle demande");
        newDemandeBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        newDemandeBtn.setOnAction(e -> {
            new DemandeTransportView(stage, currentClient).show();
        });

        toolbar.getChildren().addAll(searchField, new Label("Statut:"), statusFilter, spacer, refreshBtn, newDemandeBtn);
        return toolbar;
    }

    private void filterLivraisons(String searchText) {
        ObservableList<Livraison> filteredLivraisons = FXCollections.observableArrayList();
        
        for (Livraison livraison : livraisons) {
            if (searchText == null || searchText.isEmpty() ||
                livraison.getDestination().toLowerCase().contains(searchText.toLowerCase()) ||
                livraison.getTypeTransport().toLowerCase().contains(searchText.toLowerCase()) ||
                livraison.getNomClient().toLowerCase().contains(searchText.toLowerCase()) ||
                String.valueOf(livraison.getId()).contains(searchText)) {
                filteredLivraisons.add(livraison);
            }
        }
        
        table.setItems(filteredLivraisons);
    }

    private void filterByStatus(String status) {
        if (status.equals("Tous")) {
            table.setItems(livraisons);
        } else {
            ObservableList<Livraison> filtered = FXCollections.observableArrayList();
            for (Livraison livraison : livraisons) {
                if (livraison.getStatus().equals(status)) {
                    filtered.add(livraison);
                }
            }
            table.setItems(filtered);
        }
    }

    private void refreshData() {
        loadLivraisonsFromDatabase();
        table.setItems(livraisons);
        showAlert("Actualisation", "Les données ont été actualisées.");
    }

    private TableView<Livraison> createLivraisonsTable() {
        TableView<Livraison> tableView = new TableView<>();
        tableView.setItems(livraisons);
        tableView.setStyle("-fx-background-color: transparent;");
        tableView.setPrefHeight(400);

        // Colonne ID
        TableColumn<Livraison, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);
        idCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Destination
        TableColumn<Livraison, String> destCol = new TableColumn<>("Destination");
        destCol.setCellValueFactory(new PropertyValueFactory<>("destination"));
        destCol.setPrefWidth(200);

        // Colonne Type
        TableColumn<Livraison, String> typeCol = new TableColumn<>("Type");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("typeTransport"));
        typeCol.setPrefWidth(120);

        // Colonne Statut
        TableColumn<Livraison, String> statusCol = new TableColumn<>("Statut");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setPrefWidth(150);
        statusCol.setCellFactory(column -> new TableCell<Livraison, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "En attente":
                        case "En préparation":
                            setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                            break;
                        case "Confirmée":
                            setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                            break;
                        case "En cours":
                            setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                            break;
                        case "Terminée":
                        case "Livré":
                            setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                            break;
                        case "Annulée":
                        case "Annulé":
                            setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                            break;
                        default:
                            setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Date
        TableColumn<Livraison, String> dateCol = new TableColumn<>("Date création");
        dateCol.setCellValueFactory(cellData -> {
            Livraison livraison = cellData.getValue();
            if (livraison.getDateCreation() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    livraison.getDateCreation().format(dateFormatter)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        dateCol.setPrefWidth(150);
        dateCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Poids
        TableColumn<Livraison, String> poidsCol = new TableColumn<>("Poids (t)");
        poidsCol.setCellValueFactory(cellData -> {
            Livraison livraison = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f", livraison.getPoidsTonnes())
            );
        });
        poidsCol.setPrefWidth(80);
        poidsCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Prix
        TableColumn<Livraison, String> prixCol = new TableColumn<>("Prix (€)");
        prixCol.setCellValueFactory(cellData -> {
            Livraison livraison = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f", livraison.getPrixTotal())
            );
        });
        prixCol.setPrefWidth(100);
        prixCol.setStyle("-fx-alignment: CENTER_RIGHT;");

        // Colonne Actions
        TableColumn<Livraison, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(150);
        actionsCol.setCellFactory(column -> new TableCell<Livraison, Void>() {
            private final Button detailsBtn = new Button("📋 Détails");
            
            {
                detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 5 10;");
                detailsBtn.setOnAction(e -> {
                    Livraison livraison = getTableView().getItems().get(getIndex());
                    showDetailsDialog(livraison);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(detailsBtn);
                }
            }
        });

        tableView.getColumns().addAll(idCol, destCol, typeCol, statusCol, dateCol, poidsCol, prixCol, actionsCol);
        
        // Sélectionner la première ligne
        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateDetailsPanel(newSelection);
            }
        });

        return tableView;
    }

    private Label createDetailLabel(String label, String value) {
        HBox hbox = new HBox(5);
        hbox.setAlignment(Pos.CENTER_LEFT);
        
        Label keyLabel = new Label(label);
        keyLabel.setStyle("-fx-font-weight: bold; -fx-min-width: 120;");
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-text-fill: #2c3e50;");
        valueLabel.setWrapText(true);
        
        hbox.getChildren().addAll(keyLabel, valueLabel);
        
        Label container = new Label();
        container.setGraphic(hbox);
        container.setStyle("-fx-padding: 5 0;");
        
        return container;
    }

    private void updateDetailsPanel(Livraison livraison) {
        VBox detailsPanel = (VBox) stage.getScene().lookup("#detailsPanel");
        if (detailsPanel != null) {
            updateDetailLabel((Label) detailsPanel.getProperties().get("idLabel"), "ID: ", String.valueOf(livraison.getId()));
            updateDetailLabel((Label) detailsPanel.getProperties().get("clientLabel"), "Client: ", livraison.getNomClient());
            updateDetailLabel((Label) detailsPanel.getProperties().get("destLabel"), "Destination: ", livraison.getDestination());
            updateDetailLabel((Label) detailsPanel.getProperties().get("typeLabel"), "Type: ", livraison.getTypeTransport());
            updateDetailLabel((Label) detailsPanel.getProperties().get("statusLabel"), "Statut: ", livraison.getStatus());
            
            String dateCreation = livraison.getDateCreation() != null ? 
                livraison.getDateCreation().format(dateFormatter) : "-";
            updateDetailLabel((Label) detailsPanel.getProperties().get("dateLabel"), "Date création: ", dateCreation);
            
            updateDetailLabel((Label) detailsPanel.getProperties().get("distanceLabel"), "Distance (km): ", 
                String.format("%.2f", livraison.getDistanceKm()));
            updateDetailLabel((Label) detailsPanel.getProperties().get("poidsLabel"), "Poids (t): ", 
                String.format("%.2f", livraison.getPoidsTonnes()));
            updateDetailLabel((Label) detailsPanel.getProperties().get("prixLabel"), "Prix total: ", 
                String.format("%.2f €", livraison.getPrixTotal()));
            
            // SUPPRESSION des références aux champs supprimés
            // String camion = livraison.getMatriculeCamion() != null && !livraison.getMatriculeCamion().isEmpty() ? 
            //     livraison.getMatriculeCamion() : "Non assigné";
            // updateDetailLabel((Label) detailsPanel.getProperties().get("camionLabel"), "Camion: ", camion);
            
            // SUPPRESSION: String capacite = String.format("%.2f t", livraison.getCapaciteCamion());
            // updateDetailLabel((Label) detailsPanel.getProperties().get("capaciteLabel"), "Capacité: ", capacite);
            
            // Mettre à jour la timeline
            TextArea timelineArea = (TextArea) detailsPanel.getProperties().get("timelineArea");
            if (timelineArea != null) {
                timelineArea.setText(generateTimeline(livraison));
            }
        }
    }

    private void updateDetailLabel(Label container, String key, String value) {
        HBox hbox = (HBox) container.getGraphic();
        if (hbox.getChildren().size() >= 2) {
            ((Label) hbox.getChildren().get(1)).setText(value);
        }
    }

    private String generateTimeline(Livraison livraison) {
        if (livraison.getDateCreation() == null) {
            return "Aucune information de chronologie disponible.";
        }
        
        StringBuilder timeline = new StringBuilder();
        timeline.append("• ").append(livraison.getDateCreation().format(dateFormatter))
                .append(": Demande créée\n");
        
        String status = livraison.getStatus();
        switch (status) {
            case "En attente":
            case "En préparation":
                timeline.append("• En attente de traitement\n");
                timeline.append("• Estimation de traitement: 24-48h\n");
                break;
            case "Confirmée":
                timeline.append("• ").append(livraison.getDateCreation().plusDays(1).format(dateFormatter))
                        .append(": Commande en préparation\n");
                timeline.append("• Chargement prévu dans 24h\n");
                break;
            case "En cours":
                timeline.append("• ").append(livraison.getDateCreation().plusDays(2).format(dateFormatter))
                        .append(": En route vers ").append(livraison.getDestination()).append("\n");
                // SUPPRESSION de la référence à matriculeCamion
                // if (livraison.getMatriculeCamion() != null && !livraison.getMatriculeCamion().isEmpty()) {
                //     timeline.append("• Camion: ").append(livraison.getMatriculeCamion()).append("\n");
                // }
                break;
            case "Terminée":
            case "Livré":
                timeline.append("• ").append(livraison.getDateCreation().plusDays(3).format(dateFormatter))
                        .append(": Livraison effectuée\n");
                timeline.append("• Client notifié\n");
                break;
            case "Annulée":
            case "Annulé":
                timeline.append("• Commande annulée\n");
                timeline.append("• Remboursement en cours\n");
                break;
            default:
                timeline.append("• Statut: ").append(status).append("\n");
        }
        
        return timeline.toString();
    }

    private void showContactDialog() {
        Alert contactDialog = new Alert(Alert.AlertType.INFORMATION);
        contactDialog.setTitle("Contact Service Client");
        contactDialog.setHeaderText("Comment nous contacter");
        contactDialog.setContentText(
            "📞 Téléphone: 01 23 45 67 89\n" +
            "📧 Email: service.client@transportpro.com\n" +
            "🕐 Horaires: Lun-Ven 8h-18h\n\n" +
            "N'hésitez pas à nous contacter pour toute question concernant votre livraison."
        );
        contactDialog.showAndWait();
    }

    private void showFactureDialog() {
        Alert factureDialog = new Alert(Alert.AlertType.INFORMATION);
        factureDialog.setTitle("Facture");
        factureDialog.setHeaderText("Informations sur la facture");
        factureDialog.setContentText(
            "Votre facture sera disponible une fois la livraison terminée.\n\n" +
            "Vous recevrez un email avec:\n" +
            "• Le détail des frais\n" +
            "• Le numéro de facture\n" +
            "• Les modalités de paiement\n\n" +
            "Pour toute question concernant la facturation, contactez notre service comptabilité."
        );
        factureDialog.showAndWait();
    }

    private void showDetailsDialog(Livraison livraison) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Détails de la Livraison #" + livraison.getId());
        details.setHeaderText("Informations complètes");
        
        String dateCreation = livraison.getDateCreation() != null ? 
            livraison.getDateCreation().format(dateFormatter) : "Non disponible";
        
        String dateModification = livraison.getDateModification() != null ? 
            livraison.getDateModification().format(dateFormatter) : "Non disponible";
        
        String content = String.format(
            "📋 **Détails de la livraison**\n\n" +
            "🔢 ID: %d\n" +
            "👤 Client: %s\n" +
            "📍 Destination: %s\n" +
            "🚛 Type: %s\n" +
            "🔄 Statut: %s\n" +
            "📏 Distance: %.2f km\n" +
            "⚖️ Poids: %.2f tonnes\n" +
            "💰 Prix total: %.2f €\n" +
            "📅 Date création: %s\n" +
            "🔄 Dernière modification: %s\n" +
            "👨‍✈️ Chauffeur: %s\n\n" +
            "📞 Pour plus d'informations, contactez notre service client.",
            livraison.getId(),
            livraison.getNomClient(),
            livraison.getDestination(),
            livraison.getTypeTransport(),
            livraison.getStatus(),
            livraison.getDistanceKm(),
            livraison.getPoidsTonnes(),
            livraison.getPrixTotal(),
            dateCreation,
            dateModification,
            livraison.getChauffeurNom() != null && !livraison.getChauffeurNom().isEmpty() ? 
                livraison.getChauffeurNom() : "Non assigné"
        );
        
        details.setContentText(content);
        details.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/client-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé pour SuivieTransportView");
        }
    }

    private void goBack() {
        new com.transport.ui.dashboard.ClientDashboard(stage, currentClient).show();
    }
}