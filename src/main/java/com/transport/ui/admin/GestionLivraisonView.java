package com.transport.ui.admin;

import com.transport.model.Utilisateur;
import com.transport.model.Livraison;
import com.transport.service.DemandeService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GestionLivraisonView {

    private Stage stage;
    private Utilisateur currentAdmin;
    private DemandeService demandeService;
    private TableView<Livraison> tableLivraisons;
    private ObservableList<Livraison> livraisonList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private Label countLabel;
    private TextField searchField;
    private ComboBox<String> filterStatus;

    public GestionLivraisonView(Stage stage, Utilisateur admin) {
        this.stage = stage;
        this.currentAdmin = admin;
        this.demandeService = new DemandeService();
        this.livraisonList = FXCollections.observableArrayList();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Content
        VBox content = createContent();
        root.setCenter(content);

        Scene scene = new Scene(root, 1300, 750);
        loadCSS(scene);

        stage.setScene(scene);
        stage.setTitle("Gestion des Livraisons - Admin");
        stage.centerOnScreen();
        stage.show();

        chargerLivraisons();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 25;");
        header.setAlignment(Pos.CENTER_LEFT);
        header.setSpacing(15);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; -fx-border-color: white; -fx-border-radius: 5; -fx-padding: 8 15;");
        backBtn.setOnAction(e -> goBack());

        Label title = new Label("🚚 Gestion des Livraisons");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        countLabel = new Label("0 livraisons");
        countLabel.setStyle("-fx-text-fill: #f39c12; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 5 15; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, title, spacer, countLabel);
        
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Gestion des Livraisons");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label description = new Label("Visualisez et gérez toutes les livraisons du système. Mettez à jour les statuts, assignez des chauffeurs et suivez l'avancement.");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        description.setWrapText(true);

        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(0, 0, 10, 0));

        // Bouton Ajouter
        Button addBtn = new Button("➕ Ajouter");
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 8 15;");
        addBtn.setOnAction(e -> ajouterLivraison());

        searchField = new TextField();
        searchField.setPromptText("Rechercher une livraison...");
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;");
        searchField.setPrefWidth(300);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filtrerRecherche(newVal));

        filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("Tous", "En attente", "Confirmée", "En cours", "Terminée", "Annulée");
        filterStatus.setValue("Tous");
        filterStatus.setStyle("-fx-background-radius: 5; -fx-pref-width: 150;");
        filterStatus.setOnAction(e -> filtrerParStatut(filterStatus.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> chargerLivraisons());

        Button statsBtn = new Button("📈 Statistiques");
        statsBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        statsBtn.setOnAction(e -> showStatistics());

        Button exportBtn = new Button("📤 Exporter");
        exportBtn.setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        exportBtn.setOnAction(e -> exporterLivraisons());

        toolbar.getChildren().addAll(
            addBtn, 
            new Label("Recherche:"), searchField, 
            new Label("Statut:"), filterStatus, 
            spacer, refreshBtn, statsBtn, exportBtn
        );

        // Initialisation de la table
        tableLivraisons = new TableView<>();
        tableLivraisons.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        tableLivraisons.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableLivraisons.setPrefHeight(450);

        configurerColonnes();

        content.getChildren().addAll(title, description, toolbar, tableLivraisons);
        return content;
    }

    private void configurerColonnes() {
        tableLivraisons.getColumns().clear();

        // Colonne ID
        TableColumn<Livraison, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? String.valueOf(cellData.getValue().getId()) : ""));
        colId.setPrefWidth(60);
        colId.setStyle("-fx-alignment: CENTER;");

        // Colonne Nom Client
        TableColumn<Livraison, String> colNomClient = new TableColumn<>("Client");
        colNomClient.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getNomClient() : ""));
        colNomClient.setPrefWidth(120);

        // Colonne Destination
        TableColumn<Livraison, String> colDestination = new TableColumn<>("Destination");
        colDestination.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getDestination() : ""));
        colDestination.setPrefWidth(150);

        // Colonne Type Transport
        TableColumn<Livraison, String> colType = new TableColumn<>("Type");
        colType.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getTypeTransport() : ""));
        colType.setPrefWidth(100);

        // Colonne Distance
        TableColumn<Livraison, String> colDistance = new TableColumn<>("Distance (km)");
        colDistance.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? String.format("%.1f", cellData.getValue().getDistanceKm()) : ""));
        colDistance.setPrefWidth(90);
        colDistance.setStyle("-fx-alignment: CENTER_RIGHT;");

        // Colonne Poids
        TableColumn<Livraison, String> colPoids = new TableColumn<>("Poids (t)");
        colPoids.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? String.format("%.2f", cellData.getValue().getPoidsTonnes()) : ""));
        colPoids.setPrefWidth(80);
        colPoids.setStyle("-fx-alignment: CENTER_RIGHT;");

        // Colonne Prix
        TableColumn<Livraison, String> colPrix = new TableColumn<>("Prix (€)");
        colPrix.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? String.format("%.2f", cellData.getValue().getPrixTotal()) : ""));
        colPrix.setPrefWidth(100);
        colPrix.setStyle("-fx-alignment: CENTER_RIGHT;");

        // Colonne Date Création
        TableColumn<Livraison, String> colDateCreation = new TableColumn<>("Création");
        colDateCreation.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Livraison liv = cellData.getValue();
                return liv != null && liv.getDateCreation() != null ? 
                       liv.getDateCreation().format(dateFormatter) : "";
            }));
        colDateCreation.setPrefWidth(100);
        colDateCreation.setStyle("-fx-alignment: CENTER;");

        // Colonne Date Modification
        TableColumn<Livraison, String> colDateModification = new TableColumn<>("Modification");
        colDateModification.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Livraison liv = cellData.getValue();
                return liv != null && liv.getDateModification() != null ? 
                       liv.getDateModification().format(dateFormatter) : "";
            }));
        colDateModification.setPrefWidth(100);
        colDateModification.setStyle("-fx-alignment: CENTER;");

        // Colonne ID Chauffeur
        TableColumn<Livraison, String> colIdChauffeur = new TableColumn<>("ID Chauffeur");
        colIdChauffeur.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Livraison liv = cellData.getValue();
                return liv != null && liv.getIdChauffeur() > 0 ? 
                       String.valueOf(liv.getIdChauffeur()) : "Non assigné";
            }));
        colIdChauffeur.setPrefWidth(90);
        colIdChauffeur.setStyle("-fx-alignment: CENTER;");

        // Colonne Nom Chauffeur
        TableColumn<Livraison, String> colNomChauffeur = new TableColumn<>("Chauffeur");
        colNomChauffeur.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Livraison liv = cellData.getValue();
                return liv != null && liv.getChauffeurNom() != null && !liv.getChauffeurNom().isEmpty() ? 
                       liv.getChauffeurNom() : "Non assigné";
            }));
        colNomChauffeur.setPrefWidth(120);

        // Colonne Statut
        TableColumn<Livraison, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getStatus() : ""));
        colStatut.setPrefWidth(120);
        colStatut.setCellFactory(column -> new TableCell<Livraison, String>() {
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
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-background-color: #fef9e7; -fx-background-radius: 10; -fx-padding: 3 8; -fx-alignment: CENTER;");
                            break;
                        case "Confirmée":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-background-color: #ebf5fb; -fx-background-radius: 10; -fx-padding: 3 8; -fx-alignment: CENTER;");
                            break;
                        case "En cours":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-background-color: #eafaf1; -fx-background-radius: 10; -fx-padding: 3 8; -fx-alignment: CENTER;");
                            break;
                        case "Terminée":
                            setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-background-color: #e8f6f3; -fx-background-radius: 10; -fx-padding: 3 8; -fx-alignment: CENTER;");
                            break;
                        case "Annulée":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-background-color: #fdedec; -fx-background-radius: 10; -fx-padding: 3 8; -fx-alignment: CENTER;");
                            break;
                        default:
                            setStyle("-fx-text-fill: #7f8c8d; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Actions
        TableColumn<Livraison, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(180);
        colActions.setMinWidth(180);
        colActions.setCellFactory(column -> new TableCell<Livraison, Void>() {
            private final HBox container = new HBox(3);

            {
                container.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty) {
                    setGraphic(null);
                } else {
                    // Créer les boutons avec texte court
                    Button detailsBtn = new Button("👁");
                    Button editBtn = new Button("✏");
                    Button statusBtn = new Button("📊");
                    Button assignBtn = new Button("👨‍✈️");
                    Button deleteBtn = new Button("🗑");
                    
                    // Taille fixe pour tous les boutons
                    detailsBtn.setPrefWidth(35);
                    editBtn.setPrefWidth(35);
                    statusBtn.setPrefWidth(35);
                    assignBtn.setPrefWidth(35);
                    deleteBtn.setPrefWidth(35);
                    
                    detailsBtn.setMinWidth(35);
                    editBtn.setMinWidth(35);
                    statusBtn.setMinWidth(35);
                    assignBtn.setMinWidth(35);
                    deleteBtn.setMinWidth(35);
                    
                    detailsBtn.setMaxWidth(35);
                    editBtn.setMaxWidth(35);
                    statusBtn.setMaxWidth(35);
                    assignBtn.setMaxWidth(35);
                    deleteBtn.setMaxWidth(35);
                    
                    // Styles
                    detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5; -fx-font-size: 12px;");
                    editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5; -fx-font-size: 12px;");
                    statusBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5; -fx-font-size: 12px;");
                    assignBtn.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5; -fx-font-size: 12px;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5; -fx-font-size: 12px;");
                    
                    // Tooltips
                    detailsBtn.setTooltip(new Tooltip("Voir les détails"));
                    editBtn.setTooltip(new Tooltip("Modifier"));
                    statusBtn.setTooltip(new Tooltip("Changer statut"));
                    assignBtn.setTooltip(new Tooltip("Assigner chauffeur"));
                    deleteBtn.setTooltip(new Tooltip("Supprimer"));
                    
                    // Actions
                    detailsBtn.setOnAction(e -> {
                        Livraison livraison = getTableView().getItems().get(getIndex());
                        if (livraison != null) voirDetails(livraison);
                    });
                    
                    editBtn.setOnAction(e -> {
                        Livraison livraison = getTableView().getItems().get(getIndex());
                        if (livraison != null) modifierLivraison(livraison);
                    });
                    
                    statusBtn.setOnAction(e -> {
                        Livraison livraison = getTableView().getItems().get(getIndex());
                        if (livraison != null) updateLivraisonStatus(livraison);
                    });
                    
                    assignBtn.setOnAction(e -> {
                        Livraison livraison = getTableView().getItems().get(getIndex());
                        if (livraison != null) assignerChauffeur(livraison);
                    });
                    
                    deleteBtn.setOnAction(e -> {
                        Livraison livraison = getTableView().getItems().get(getIndex());
                        if (livraison != null) supprimerLivraison(livraison);
                    });
                    
                    // Ajouter au container
                    container.getChildren().clear();
                    container.getChildren().addAll(detailsBtn, editBtn, statusBtn, assignBtn, deleteBtn);
                    setGraphic(container);
                }
            }
        });
        
        tableLivraisons.getColumns().addAll(
            colId, colNomClient,
            colDestination, colType, colDistance, colPoids, colPrix,
            colDateCreation, colDateModification, colIdChauffeur, colNomChauffeur,
            colStatut, colActions
        );
    }

    private void chargerLivraisons() {
        try {
            tableLivraisons.setPlaceholder(new Label("Chargement des livraisons en cours..."));
            
            List<Livraison> livraisons = demandeService.getAllLivraisons();
            
            livraisonList.clear();
            
            if (livraisons != null && !livraisons.isEmpty()) {
                livraisonList.addAll(livraisons);
                tableLivraisons.setItems(livraisonList);
                
                if (countLabel != null) {
                    countLabel.setText(livraisons.size() + " livraisons");
                }
                
                System.out.println("✅ " + livraisons.size() + " livraison(s) chargée(s)");
                
            } else {
                Label noDataLabel = new Label("Aucune livraison trouvée dans la base de données.");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                tableLivraisons.setPlaceholder(noDataLabel);
                
                if (countLabel != null) {
                    countLabel.setText("0 livraisons");
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des livraisons: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            tableLivraisons.setPlaceholder(errorLabel);
            
            if (countLabel != null) {
                countLabel.setText("Erreur");
            }
        }
    }

    private void filtrerRecherche(String recherche) {
        if (recherche == null || recherche.isEmpty()) {
            tableLivraisons.setItems(livraisonList);
        } else {
            ObservableList<Livraison> filteredList = FXCollections.observableArrayList();
            String searchLower = recherche.toLowerCase();
            
            for (Livraison livraison : livraisonList) {
                if ((livraison.getNomClient() != null && livraison.getNomClient().toLowerCase().contains(searchLower)) ||
                    (livraison.getDestination() != null && livraison.getDestination().toLowerCase().contains(searchLower)) ||
                    (livraison.getTypeTransport() != null && livraison.getTypeTransport().toLowerCase().contains(searchLower)) ||
                    (livraison.getChauffeurNom() != null && livraison.getChauffeurNom().toLowerCase().contains(searchLower)) ||
                    String.valueOf(livraison.getId()).contains(searchLower)) {
                    filteredList.add(livraison);
                }
            }
            tableLivraisons.setItems(filteredList);
        }
    }

    private void filtrerParStatut(String statut) {
        if (statut.equals("Tous")) {
            tableLivraisons.setItems(livraisonList);
        } else {
            ObservableList<Livraison> filteredList = FXCollections.observableArrayList();
            for (Livraison livraison : livraisonList) {
                if (livraison.getStatus().equals(statut)) {
                    filteredList.add(livraison);
                }
            }
            tableLivraisons.setItems(filteredList);
        }
    }

    private void voirDetails(Livraison livraison) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la livraison #" + livraison.getId());
        dialog.setHeaderText("Informations complètes");

        ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 30, 10, 10));

        // Informations client
        Label clientTitle = new Label("📋 Informations Client");
        clientTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        grid.add(clientTitle, 0, 0, 2, 1);
        
        grid.add(new Label("Nom Client:"), 0, 1);
        grid.add(new Label(livraison.getNomClient()), 1, 1);

        // Informations livraison
        Label livraisonTitle = new Label("📦 Informations Livraison");
        livraisonTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        grid.add(livraisonTitle, 0, 2, 2, 1);

        grid.add(new Label("Destination:"), 0, 3);
        grid.add(new Label(livraison.getDestination()), 1, 3);
        
        grid.add(new Label("Type Transport:"), 0, 4);
        grid.add(new Label(livraison.getTypeTransport()), 1, 4);
        
        grid.add(new Label("Distance:"), 0, 5);
        grid.add(new Label(String.format("%.1f km", livraison.getDistanceKm())), 1, 5);
        
        grid.add(new Label("Poids:"), 0, 6);
        grid.add(new Label(String.format("%.2f tonnes", livraison.getPoidsTonnes())), 1, 6);
        
        grid.add(new Label("Prix Total:"), 0, 7);
        grid.add(new Label(String.format("%.2f €", livraison.getPrixTotal())), 1, 7);

        // Informations chauffeur
        Label chauffeurTitle = new Label("👨‍✈️ Informations Chauffeur");
        chauffeurTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        grid.add(chauffeurTitle, 0, 8, 2, 1);

        grid.add(new Label("ID Chauffeur:"), 0, 9);
        grid.add(new Label(livraison.getIdChauffeur() > 0 ? 
                          String.valueOf(livraison.getIdChauffeur()) : "Non assigné"), 1, 9);
        
        grid.add(new Label("Nom Chauffeur:"), 0, 10);
        grid.add(new Label(livraison.getChauffeurNom() != null ? 
                          livraison.getChauffeurNom() : "Non assigné"), 1, 10);

        // Informations dates et statut
        Label autresTitle = new Label("📅 Autres Informations");
        autresTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
        grid.add(autresTitle, 0, 11, 2, 1);

        grid.add(new Label("Statut:"), 0, 12);
        Label statutLabel = new Label(livraison.getStatus());
        statutLabel.setStyle("-fx-font-weight: bold;");
        grid.add(statutLabel, 1, 12);
        
        grid.add(new Label("Date Création:"), 0, 13);
        grid.add(new Label(livraison.getDateCreation() != null ? 
                          livraison.getDateCreation().format(dateFormatter) : "Non définie"), 1, 13);
        
        grid.add(new Label("Dernière Modification:"), 0, 14);
        grid.add(new Label(livraison.getDateModification() != null ? 
                          livraison.getDateModification().format(dateFormatter) : "Non modifiée"), 1, 14);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().setPrefSize(500, 550);
        dialog.showAndWait();
    }

    private void ajouterLivraison() {
        Dialog<Livraison> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une nouvelle livraison");
        dialog.setHeaderText("Entrez les détails de la nouvelle livraison");

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Informations client
        TextField nomClientField = new TextField();
        nomClientField.setPromptText("Nom du client");

        // Informations livraison
        TextField destinationField = new TextField();
        destinationField.setPromptText("Destination");
        
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Routier", "Maritime", "Aérien", "Ferroviaire", "Standard", "Express", "Fragile", "Refrigéré", "Volume");
        typeCombo.setValue("Routier");
        
        TextField distanceField = new TextField();
        distanceField.setPromptText("Distance en km (ex: 150.5)");
        
        TextField poidsField = new TextField();
        poidsField.setPromptText("Poids en tonnes (ex: 2.5)");
        
        TextField prixField = new TextField();
        prixField.setPromptText("Prix total (ex: 450.75)");

        // Informations chauffeur
        TextField chauffeurIdField = new TextField();
        chauffeurIdField.setPromptText("ID Chauffeur (facultatif)");
        
        TextField chauffeurNomField = new TextField();
        chauffeurNomField.setPromptText("Nom du chauffeur (facultatif)");

        // Statut
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("En attente", "Confirmée", "En cours", "Terminée", "Annulée");
        statusCombo.setValue("En attente");

        // Layout
        int row = 0;
        
        // Section client
        Label clientSection = new Label("📋 Informations Client");
        clientSection.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        grid.add(clientSection, 0, row++, 2, 1);
        
        grid.add(new Label("Nom Client*:"), 0, row);
        grid.add(nomClientField, 1, row++);

        // Section livraison
        Label livraisonSection = new Label("📦 Informations Livraison");
        livraisonSection.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        grid.add(livraisonSection, 0, row++, 2, 1);
        
        grid.add(new Label("Destination*:"), 0, row);
        grid.add(destinationField, 1, row++);
        
        grid.add(new Label("Type*:"), 0, row);
        grid.add(typeCombo, 1, row++);
        
        grid.add(new Label("Distance (km)*:"), 0, row);
        grid.add(distanceField, 1, row++);
        
        grid.add(new Label("Poids (t)*:"), 0, row);
        grid.add(poidsField, 1, row++);
        
        grid.add(new Label("Prix (€)*:"), 0, row);
        grid.add(prixField, 1, row++);

        // Section chauffeur
        Label chauffeurSection = new Label("👨‍✈️ Informations Chauffeur");
        chauffeurSection.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        grid.add(chauffeurSection, 0, row++, 2, 1);
        
        grid.add(new Label("ID Chauffeur:"), 0, row);
        grid.add(chauffeurIdField, 1, row++);
        
        grid.add(new Label("Nom Chauffeur:"), 0, row);
        grid.add(chauffeurNomField, 1, row++);

        // Section statut
        grid.add(new Label("Statut*:"), 0, row);
        grid.add(statusCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Validation
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        Runnable validateFields = () -> {
            boolean valid = !nomClientField.getText().trim().isEmpty() &&
                           !destinationField.getText().trim().isEmpty() &&
                           !distanceField.getText().trim().isEmpty() &&
                           !poidsField.getText().trim().isEmpty() &&
                           !prixField.getText().trim().isEmpty();
            
            try {
                Double.parseDouble(distanceField.getText().trim());
                Double.parseDouble(poidsField.getText().trim());
                Double.parseDouble(prixField.getText().trim());
                
                // Vérifier chauffeur ID si renseigné
                if (!chauffeurIdField.getText().trim().isEmpty()) {
                    Integer.parseInt(chauffeurIdField.getText().trim());
                }
            } catch (NumberFormatException e) {
                valid = false;
            }
            
            saveButton.setDisable(!valid);
        };

        nomClientField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        destinationField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        distanceField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        poidsField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        prixField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());
        chauffeurIdField.textProperty().addListener((obs, oldVal, newVal) -> validateFields.run());

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Livraison livraison = new Livraison();
                    
                    // Informations client
                    livraison.setNomClient(nomClientField.getText().trim());
                    
                    // Informations livraison
                    livraison.setDestination(destinationField.getText().trim());
                    livraison.setTypeTransport(typeCombo.getValue());
                    livraison.setDistanceKm(Double.parseDouble(distanceField.getText().trim()));
                    livraison.setPoidsTonnes(Double.parseDouble(poidsField.getText().trim()));
                    livraison.setPrixTotal(Double.parseDouble(prixField.getText().trim()));
                    
                    // Informations chauffeur
                    if (!chauffeurIdField.getText().trim().isEmpty()) {
                        livraison.setIdChauffeur(Integer.parseInt(chauffeurIdField.getText().trim()));
                    }
                    if (!chauffeurNomField.getText().trim().isEmpty()) {
                        livraison.setChauffeurNom(chauffeurNomField.getText().trim());
                    }
                    
                    // Statut et dates
                    livraison.setStatus(statusCombo.getValue());
                    livraison.setDateCreation(LocalDate.now());
                    
                    return livraison;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez vérifier les valeurs numériques (distance, poids, prix, ID chauffeur).");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(livraison -> {
            if (livraison != null) {
                boolean success = demandeService.createLivraison(livraison);
                
                if (success) {
                    showAlert("Succès", "La livraison a été créée avec succès.");
                    chargerLivraisons();
                } else {
                    showAlert("Erreur", "Une erreur est survenue lors de la création de la livraison.");
                }
            }
        });
    }

    private void modifierLivraison(Livraison livraison) {
        Dialog<Livraison> dialog = new Dialog<>();
        dialog.setTitle("Modifier la livraison");
        dialog.setHeaderText("Modifier les détails de la livraison #" + livraison.getId());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Informations client
        TextField nomClientField = new TextField(livraison.getNomClient());

        // Informations livraison
        TextField destinationField = new TextField(livraison.getDestination());
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Routier", "Maritime", "Aérien", "Ferroviaire", "Standard", "Express", "Fragile", "Refrigéré", "Volume");
        typeCombo.setValue(livraison.getTypeTransport());
        
        TextField distanceField = new TextField(String.valueOf(livraison.getDistanceKm()));
        TextField poidsField = new TextField(String.valueOf(livraison.getPoidsTonnes()));
        TextField prixField = new TextField(String.valueOf(livraison.getPrixTotal()));

        // Informations chauffeur
        TextField chauffeurIdField = new TextField(livraison.getIdChauffeur() > 0 ? 
                                                  String.valueOf(livraison.getIdChauffeur()) : "");
        TextField chauffeurNomField = new TextField(livraison.getChauffeurNom() != null ? 
                                                   livraison.getChauffeurNom() : "");

        // Statut
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("En attente", "Confirmée", "En cours", "Terminée", "Annulée");
        statusCombo.setValue(livraison.getStatus());

        // Layout
        int row = 0;
        
        grid.add(new Label("Nom Client*:"), 0, row);
        grid.add(nomClientField, 1, row++);
        
        grid.add(new Label("Destination*:"), 0, row);
        grid.add(destinationField, 1, row++);
        
        grid.add(new Label("Type*:"), 0, row);
        grid.add(typeCombo, 1, row++);
        
        grid.add(new Label("Distance (km)*:"), 0, row);
        grid.add(distanceField, 1, row++);
        
        grid.add(new Label("Poids (t)*:"), 0, row);
        grid.add(poidsField, 1, row++);
        
        grid.add(new Label("Prix (€)*:"), 0, row);
        grid.add(prixField, 1, row++);
        
        grid.add(new Label("ID Chauffeur:"), 0, row);
        grid.add(chauffeurIdField, 1, row++);
        
        grid.add(new Label("Nom Chauffeur:"), 0, row);
        grid.add(chauffeurNomField, 1, row++);
        
        grid.add(new Label("Statut*:"), 0, row);
        grid.add(statusCombo, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Validation
        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(false);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Livraison updatedLivraison = new Livraison();
                    updatedLivraison.setId(livraison.getId());
                    
                    // Informations client
                    updatedLivraison.setNomClient(nomClientField.getText().trim());
                    
                    // Informations livraison
                    updatedLivraison.setDestination(destinationField.getText().trim());
                    updatedLivraison.setTypeTransport(typeCombo.getValue());
                    updatedLivraison.setDistanceKm(Double.parseDouble(distanceField.getText().trim()));
                    updatedLivraison.setPoidsTonnes(Double.parseDouble(poidsField.getText().trim()));
                    updatedLivraison.setPrixTotal(Double.parseDouble(prixField.getText().trim()));
                    
                    // Informations chauffeur
                    if (!chauffeurIdField.getText().trim().isEmpty()) {
                        updatedLivraison.setIdChauffeur(Integer.parseInt(chauffeurIdField.getText().trim()));
                    } else {
                        updatedLivraison.setIdChauffeur(0);
                    }
                    if (!chauffeurNomField.getText().trim().isEmpty()) {
                        updatedLivraison.setChauffeurNom(chauffeurNomField.getText().trim());
                    }
                    
                    // Statut et dates
                    updatedLivraison.setStatus(statusCombo.getValue());
                    updatedLivraison.setDateCreation(livraison.getDateCreation());
                    
                    return updatedLivraison;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Veuillez vérifier les valeurs numériques.");
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedLivraison -> {
            if (updatedLivraison != null) {
                boolean success = demandeService.updateLivraison(updatedLivraison);
                
                if (success) {
                    showAlert("Succès", "La livraison #" + updatedLivraison.getId() + " a été mise à jour.");
                    chargerLivraisons();
                } else {
                    showAlert("Erreur", "Une erreur est survenue lors de la mise à jour.");
                }
            }
        });
    }

    private void assignerChauffeur(Livraison livraison) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assigner un chauffeur");
        dialog.setHeaderText("Assigner un chauffeur à la livraison #" + livraison.getId());

        ButtonType saveButtonType = new ButtonType("Assigner", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField chauffeurIdField = new TextField();
        if (livraison.getIdChauffeur() > 0) {
            chauffeurIdField.setText(String.valueOf(livraison.getIdChauffeur()));
        }
        chauffeurIdField.setPromptText("ID Chauffeur");

        TextField chauffeurNomField = new TextField();
        if (livraison.getChauffeurNom() != null) {
            chauffeurNomField.setText(livraison.getChauffeurNom());
        }
        chauffeurNomField.setPromptText("Nom du chauffeur");

        grid.add(new Label("Livraison:"), 0, 0);
        grid.add(new Label("#" + livraison.getId() + " - " + livraison.getDestination()), 1, 0);
        grid.add(new Label("ID Chauffeur*:"), 0, 1);
        grid.add(chauffeurIdField, 1, 1);
        grid.add(new Label("Nom Chauffeur*:"), 0, 2);
        grid.add(chauffeurNomField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);

        chauffeurIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = !newVal.trim().isEmpty() && 
                           !chauffeurNomField.getText().trim().isEmpty();
            saveButton.setDisable(!valid);
        });

        chauffeurNomField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean valid = !newVal.trim().isEmpty() && 
                           !chauffeurIdField.getText().trim().isEmpty();
            saveButton.setDisable(!valid);
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    int chauffeurId = Integer.parseInt(chauffeurIdField.getText().trim());
                    String chauffeurNom = chauffeurNomField.getText().trim();
                    
                    if (chauffeurId > 0 && !chauffeurNom.isEmpty()) {
                        boolean success = demandeService.updateChauffeurLivraison(livraison.getId(), chauffeurId, chauffeurNom);
                        
                        if (success) {
                            showAlert("Succès", "Chauffeur assigné avec succès à la livraison #" + livraison.getId());
                            chargerLivraisons();
                        } else {
                            showAlert("Erreur", "Échec de l'assignation du chauffeur.");
                        }
                    }
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "L'ID chauffeur doit être un nombre valide.");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void updateLivraisonStatus(Livraison livraison) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Mettre à jour le statut");
        dialog.setHeaderText("Livraison #" + livraison.getId() + " - " + livraison.getDestination());

        ButtonType confirmButtonType = new ButtonType("Mettre à jour", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("En attente", "Confirmée", "En cours", "Terminée", "Annulée");
        statusCombo.setValue(livraison.getStatus());

        grid.add(new Label("Nouveau statut:"), 0, 0);
        grid.add(statusCombo, 1, 0);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == confirmButtonType) {
                return statusCombo.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newStatus -> {
            boolean success = demandeService.updateLivraisonStatus(livraison.getId(), newStatus);
            
            if (success) {
                showAlert("Succès", "Le statut de la livraison #" + livraison.getId() + " a été mis à jour à: " + newStatus);
                chargerLivraisons();
            } else {
                showAlert("Erreur", "Une erreur est survenue lors de la mise à jour du statut.");
            }
        });
    }

    private void supprimerLivraison(Livraison livraison) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Supprimer la livraison");
        confirm.setHeaderText("Confirmez-vous la suppression de cette livraison ?");
        confirm.setContentText(
            "ID: " + livraison.getId() + "\n" +
            "Destination: " + livraison.getDestination() + "\n" +
            "Client: " + livraison.getNomClient() + "\n\n" +
            "⚠️ Cette action est irréversible !"
        );
        
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            boolean success = demandeService.deleteLivraison(livraison.getId());
            
            if (success) {
                showAlert("Succès", "La livraison #" + livraison.getId() + " a été supprimée avec succès.");
                chargerLivraisons();
            } else {
                showAlert("Erreur", "Une erreur est survenue lors de la suppression de la livraison.");
            }
        }
    }

    private void showStatistics() {
        List<Livraison> livraisons = demandeService.getAllLivraisons();
        
        if (livraisons == null || livraisons.isEmpty()) {
            showAlert("Statistiques", "Aucune donnée disponible.");
            return;
        }
        
        int total = livraisons.size();
        int enAttente = 0, confirmees = 0, enCours = 0, terminees = 0, annulees = 0;
        double totalRevenu = 0;
        double totalDistance = 0;
        double totalPoids = 0;
        
        for (Livraison liv : livraisons) {
            switch (liv.getStatus()) {
                case "En attente": enAttente++; break;
                case "Confirmée": confirmees++; break;
                case "En cours": enCours++; break;
                case "Terminée": 
                    terminees++; 
                    totalRevenu += liv.getPrixTotal();
                    break;
                case "Annulée": annulees++; break;
            }
            
            totalDistance += liv.getDistanceKm();
            totalPoids += liv.getPoidsTonnes();
        }
        
        Alert stats = new Alert(Alert.AlertType.INFORMATION);
        stats.setTitle("📈 Statistiques des Livraisons");
        stats.setHeaderText("Aperçu global");
        stats.setContentText(
            "📊 **STATISTIQUES GLOBALES**\n\n" +
            "📦 Total livraisons: " + total + "\n" +
            "⏳ En attente: " + enAttente + " (" + String.format("%.1f", (double)enAttente/total*100) + "%)\n" +
            "✅ Confirmées: " + confirmees + " (" + String.format("%.1f", (double)confirmees/total*100) + "%)\n" +
            "🚚 En cours: " + enCours + " (" + String.format("%.1f", (double)enCours/total*100) + "%)\n" +
            "🏁 Terminées: " + terminees + " (" + String.format("%.1f", (double)terminees/total*100) + "%)\n" +
            "❌ Annulées: " + annulees + " (" + String.format("%.1f", (double)annulees/total*100) + "%)\n\n" +
            "💰 Revenu total: " + String.format("%.2f", totalRevenu) + " €\n" +
            "📏 Distance totale: " + String.format("%.1f", totalDistance) + " km\n" +
            "⚖️  Poids total: " + String.format("%.1f", totalPoids) + " tonnes\n" +
            "📈 Taux de complétion: " + String.format("%.1f", (double)terminees/total*100) + "%\n\n" +
            "🔄 Dernière mise à jour: " + new java.util.Date()
        );
        stats.showAndWait();
    }

    private void exporterLivraisons() {
        try {
            List<Livraison> livraisons = demandeService.getAllLivraisons();
            
            if (livraisons == null || livraisons.isEmpty()) {
                showAlert("Export", "Aucune donnée à exporter.");
                return;
            }
            
            // Création du contenu CSV
            StringBuilder csvContent = new StringBuilder();
            csvContent.append("ID;Nom Client;Destination;Type Transport;Distance (km);Poids (tonnes);Prix Total;Statut;Date Création;Date Modification;ID Chauffeur;Nom Chauffeur\n");
            
            for (Livraison liv : livraisons) {
                csvContent.append(liv.getId()).append(";")
                         .append(liv.getNomClient()).append(";")
                         .append(liv.getDestination()).append(";")
                         .append(liv.getTypeTransport()).append(";")
                         .append(String.format("%.1f", liv.getDistanceKm())).append(";")
                         .append(String.format("%.2f", liv.getPoidsTonnes())).append(";")
                         .append(String.format("%.2f", liv.getPrixTotal())).append(";")
                         .append(liv.getStatus()).append(";")
                         .append(liv.getDateCreation() != null ? liv.getDateCreation().format(dateFormatter) : "").append(";")
                         .append(liv.getDateModification() != null ? liv.getDateModification().format(dateFormatter) : "").append(";")
                         .append(liv.getIdChauffeur()).append(";")
                         .append(liv.getChauffeurNom() != null ? liv.getChauffeurNom() : "").append("\n");
            }
            
            // Sauvegarde dans un fichier
            java.io.File file = new java.io.File("export_livraisons_" + 
                java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
            
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(csvContent.toString());
            }
            
            showAlert("Export Réussi", "Les données ont été exportées avec succès dans le fichier:\n" + file.getAbsolutePath());
            
        } catch (Exception e) {
            showAlert("Erreur d'Export", "Une erreur est survenue lors de l'export: " + e.getMessage());
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        alert.showAndWait();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour GestionLivraisonView");
        }
    }

    private void goBack() {
        new com.transport.ui.dashboard.AdminDashboard(stage, currentAdmin).show();
    }
}