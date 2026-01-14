package com.transport.ui.admin;

import com.transport.model.Utilisateur;
import com.transport.service.CamionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GestionCamionsView {

    private Stage stage;
    private Utilisateur currentAdmin;
    private TableView<Camion> table;
    private ObservableList<Camion> camions = FXCollections.observableArrayList();
    private CamionService camionService;
    private ComboBox<String> etatFilter;
    private TextField searchField;
    private Label countLabel;
    private ObservableList<String> chauffeursList = FXCollections.observableArrayList();
    private HBox footerContainer;
    private VBox statDisponible;
    private VBox statEnRoute;
    private VBox statMaintenance;
    private VBox statCapacite;

    // Classe interne pour représenter un camion
    public static class Camion {
        private int id;
        private String matricule;
        private String modele;
        private double capacite;
        private String etat;
        private String chauffeur;
        private Date derniereMaintenance;
        private int kilometrage;
        private double consommation;

        public Camion(int id, String matricule, String modele, double capacite, 
                     String etat, String chauffeur, Date derniereMaintenance, 
                     int kilometrage, double consommation) {
            this.id = id;
            this.matricule = matricule;
            this.modele = modele;
            this.capacite = capacite;
            this.etat = etat;
            this.chauffeur = chauffeur;
            this.derniereMaintenance = derniereMaintenance;
            this.kilometrage = kilometrage;
            this.consommation = consommation;
        }

        // Constructeur simplifié pour compatibilité
        public Camion(int id, String matricule, String modele, double capacite, 
                     String etat, String chauffeur) {
            this(id, matricule, modele, capacite, etat, chauffeur, null, 0, 0.0);
        }

        // Getters
        public int getId() { return id; }
        public String getMatricule() { return matricule; }
        public String getModele() { return modele; }
        public double getCapacite() { return capacite; }
        public String getEtat() { return etat; }
        public String getChauffeur() { return chauffeur; }
        public Date getDerniereMaintenance() { return derniereMaintenance; }
        public int getKilometrage() { return kilometrage; }
        public double getConsommation() { return consommation; }

        // Setters
        public void setId(int id) { this.id = id; }
        public void setMatricule(String matricule) { this.matricule = matricule; }
        public void setModele(String modele) { this.modele = modele; }
        public void setCapacite(double capacite) { this.capacite = capacite; }
        public void setEtat(String etat) { this.etat = etat; }
        public void setChauffeur(String chauffeur) { this.chauffeur = chauffeur; }
        public void setDerniereMaintenance(Date derniereMaintenance) { this.derniereMaintenance = derniereMaintenance; }
        public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }
        public void setConsommation(double consommation) { this.consommation = consommation; }
    }

    public GestionCamionsView(Stage stage, Utilisateur admin) {
        this.stage = stage;
        this.currentAdmin = admin;
        this.camionService = new CamionService();
        loadChauffeursFromDatabase();
        loadCamionsFromDatabase();
    }

    private void loadChauffeursFromDatabase() {
        try {
            chauffeursList.clear();
            List<String> dbChauffeurs = camionService.getAllChauffeurs();
            if (dbChauffeurs != null && !dbChauffeurs.isEmpty()) {
                chauffeursList.addAll(dbChauffeurs);
                chauffeursList.add(0, ""); // Option vide
                System.out.println("✅ " + chauffeursList.size() + " chauffeurs chargés depuis la BD");
            } else {
                chauffeursList.add(""); // Option vide
                chauffeursList.add("Aucun chauffeur disponible");
                System.out.println("⚠️ Aucun chauffeur trouvé dans la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des chauffeurs: " + e.getMessage());
        }
    }

    private void loadCamionsFromDatabase() {
        try {
            camions.clear();
            List<Camion> dbCamions = camionService.getAllCamions();
            if (dbCamions != null && !dbCamions.isEmpty()) {
                camions.addAll(dbCamions);
                System.out.println("✅ " + camions.size() + " camions chargés depuis la BD");
            } else {
                System.out.println("⚠️ Aucun camion trouvé dans la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des camions: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTableAndStats() {
        table.refresh();
        updateFooterStats();
        if (countLabel != null) {
            countLabel.setText(camions.size() + " camions");
        }
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader("🚚 Gestion des Camions");
        
        // Toolbar
        HBox toolbar = createToolbar();
        
        VBox topContainer = new VBox(header, toolbar);
        root.setTop(topContainer);

        // Table - Augmenter la largeur pour les nouvelles colonnes
        table = createCamionsTable();
        root.setCenter(table);

        // Footer/Stats
        footerContainer = createFooter();
        root.setBottom(footerContainer);

        // Agrandir la scène pour accommoder les nouvelles colonnes
        Scene scene = new Scene(root, 1400, 700); // Augmenté de 1200 à 1400
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Gestion des Camions - Admin Dashboard");
        stage.centerOnScreen();
        stage.show();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé pour GestionCamionsView");
        }
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

        countLabel = new Label(camions.size() + " camions");
        countLabel.setStyle("-fx-text-fill: #e67e22; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 5 15; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, titleLabel, spacer, countLabel);
        return header;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15 25; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Bouton Ajouter
        Button addBtn = new Button("➕ Ajouter Camion");
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddCamionDialog());

        // Champ recherche
        searchField = new TextField();
        searchField.setPromptText("Rechercher un camion...");
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;");
        searchField.setPrefWidth(250);

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCamions(newValue, etatFilter.getValue());
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        searchBtn.setOnAction(e -> filterCamions(searchField.getText(), etatFilter.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filtres
        etatFilter = new ComboBox<>();
        etatFilter.getItems().addAll("Tous", "Disponible", "En route", "En maintenance", "En réparation", "En chargement");
        etatFilter.setValue("Tous");
        etatFilter.setStyle("-fx-background-radius: 5; -fx-pref-width: 150;");
        etatFilter.setOnAction(e -> filterCamions(searchField.getText(), etatFilter.getValue()));

        toolbar.getChildren().addAll(addBtn, spacer, searchField, searchBtn, new Label("État:"), etatFilter);
        return toolbar;
    }

    private void filterCamions(String searchText, String etatFilterValue) {
        ObservableList<Camion> filteredCamions = FXCollections.observableArrayList();
        
        for (Camion camion : camions) {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    camion.getMatricule().toLowerCase().contains(searchText.toLowerCase()) ||
                    camion.getModele().toLowerCase().contains(searchText.toLowerCase()) ||
                    (camion.getChauffeur() != null && camion.getChauffeur().toLowerCase().contains(searchText.toLowerCase()));
                    
            boolean matchesEtat = etatFilterValue == null || etatFilterValue.equals("Tous") || 
                    camion.getEtat().equals(etatFilterValue);
            
            if (matchesSearch && matchesEtat) {
                filteredCamions.add(camion);
            }
        }
        
        table.setItems(filteredCamions);
        updateFooterStats(filteredCamions);
    }

    private TableView<Camion> createCamionsTable() {
        TableView<Camion> tableView = new TableView<>();
        tableView.setItems(camions);
        tableView.setStyle("-fx-background-color: transparent;");

        // Colonne ID
        TableColumn<Camion, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60); // Réduit
        idCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Matricule
        TableColumn<Camion, String> matriculeCol = new TableColumn<>("Matricule");
        matriculeCol.setCellValueFactory(new PropertyValueFactory<>("matricule"));
        matriculeCol.setPrefWidth(100); // Réduit

        // Colonne Modèle
        TableColumn<Camion, String> modeleCol = new TableColumn<>("Modèle");
        modeleCol.setCellValueFactory(new PropertyValueFactory<>("modele"));
        modeleCol.setPrefWidth(150); // Réduit

        // Colonne Capacité
        TableColumn<Camion, Double> capaciteCol = new TableColumn<>("Capacité (T)");
        capaciteCol.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        capaciteCol.setPrefWidth(100); // Réduit
        capaciteCol.setCellFactory(column -> new TableCell<Camion, Double>() {
            @Override
            protected void updateItem(Double capacite, boolean empty) {
                super.updateItem(capacite, empty);
                if (empty || capacite == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.1f T", capacite));
                    
                    // Couleur selon la capacité
                    if (capacite >= 40) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else if (capacite >= 35) {
                        setStyle("-fx-text-fill: #f39c12; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #e74c3c; -fx-alignment: CENTER; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Colonne État
        TableColumn<Camion, String> etatCol = new TableColumn<>("État");
        etatCol.setCellValueFactory(new PropertyValueFactory<>("etat"));
        etatCol.setPrefWidth(120); // Réduit
        etatCol.setCellFactory(column -> new TableCell<Camion, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(etat);
                    switch (etat) {
                        case "Disponible" -> setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "En route" -> setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "En chargement" -> setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "En maintenance", "En réparation" -> setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        default -> setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Chauffeur
        TableColumn<Camion, String> chauffeurCol = new TableColumn<>("Chauffeur");
        chauffeurCol.setCellValueFactory(new PropertyValueFactory<>("chauffeur"));
        chauffeurCol.setPrefWidth(120); // Réduit

        // NOUVELLE Colonne: Dernière Maintenance
        TableColumn<Camion, Date> maintenanceCol = new TableColumn<>("Dernière Maintenance");
        maintenanceCol.setCellValueFactory(new PropertyValueFactory<>("derniereMaintenance"));
        maintenanceCol.setPrefWidth(130);
        maintenanceCol.setCellFactory(column -> new TableCell<Camion, Date>() {
            private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("-");
                    setStyle("-fx-text-fill: #95a5a6; -fx-alignment: CENTER; -fx-font-style: italic;");
                } else {
                    setText(dateFormat.format(date));
                    setStyle("-fx-alignment: CENTER;");
                    
                    // Changer la couleur selon l'ancienneté de la maintenance
                    long diff = System.currentTimeMillis() - date.getTime();
                    long days = diff / (1000 * 60 * 60 * 24);
                    
                    if (days > 90) { // Plus de 3 mois
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (days > 60) { // Plus de 2 mois
                        setStyle("-fx-text-fill: #f39c12; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // NOUVELLE Colonne: Kilométrage
        TableColumn<Camion, Integer> kilometrageCol = new TableColumn<>("Kilométrage");
        kilometrageCol.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        kilometrageCol.setPrefWidth(110);
        kilometrageCol.setCellFactory(column -> new TableCell<Camion, Integer>() {
            @Override
            protected void updateItem(Integer km, boolean empty) {
                super.updateItem(km, empty);
                if (empty || km == null || km == 0) {
                    setText("-");
                    setStyle("-fx-text-fill: #95a5a6; -fx-alignment: CENTER; -fx-font-style: italic;");
                } else {
                    // Formater avec séparateur de milliers
                    String formatted = String.format("%,d km", km);
                    setText(formatted);
                    setStyle("-fx-alignment: CENTER;");
                    
                    // Couleur selon le kilométrage
                    if (km > 200000) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (km > 100000) {
                        setStyle("-fx-text-fill: #f39c12; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // NOUVELLE Colonne: Consommation
        TableColumn<Camion, Double> consommationCol = new TableColumn<>("Consommation");
        consommationCol.setCellValueFactory(new PropertyValueFactory<>("consommation"));
        consommationCol.setPrefWidth(120);
        consommationCol.setCellFactory(column -> new TableCell<Camion, Double>() {
            @Override
            protected void updateItem(Double conso, boolean empty) {
                super.updateItem(conso, empty);
                if (empty || conso == null || conso == 0.0) {
                    setText("-");
                    setStyle("-fx-text-fill: #95a5a6; -fx-alignment: CENTER; -fx-font-style: italic;");
                } else {
                    setText(String.format("%.1f L/100km", conso));
                    setStyle("-fx-alignment: CENTER;");
                    
                    // Couleur selon la consommation
                    if (conso > 35) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else if (conso > 30) {
                        setStyle("-fx-text-fill: #f39c12; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Actions
        TableColumn<Camion, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200); // Légèrement réduit
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Camion, Void> call(TableColumn<Camion, Void> param) {
                return new TableCell<>() {
                    private final HBox container = new HBox(5);

                    {
                    	Button editBtn = new Button("✎");
                    	editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                    	editBtn.setOnAction(e -> editCamion(getTableView().getItems().get(getIndex())));

                    	Button deleteBtn = new Button("×");
                    	deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                    	deleteBtn.setOnAction(e -> deleteCamion(getTableView().getItems().get(getIndex())));

                    	Button detailsBtn = new Button("📄");
                    	detailsBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                    	detailsBtn.setOnAction(e -> showDetails(getTableView().getItems().get(getIndex())));
                    	
                        container.getChildren().addAll(editBtn, detailsBtn, deleteBtn);
                        container.setAlignment(Pos.CENTER);
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : container);
                    }
                };
            }
        });

        // Ajouter toutes les colonnes au tableau
        tableView.getColumns().addAll(
            idCol, matriculeCol, modeleCol, capaciteCol, etatCol, 
            chauffeurCol, maintenanceCol, kilometrageCol, consommationCol, actionsCol
        );
        
        return tableView;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15 25; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(Pos.CENTER_LEFT);

        // Créer les éléments de statistiques
        statDisponible = createStatItem("Camions disponibles", "0/0", "#2ecc71");
        statEnRoute = createStatItem("En route", "0", "#3498db");
        statMaintenance = createStatItem("En maintenance", "0", "#e74c3c");
        statCapacite = createStatItem("Capacité totale", "0.0 T", "#9b59b6");

        footer.getChildren().addAll(statDisponible, statEnRoute, statMaintenance, statCapacite);
        
        // Mettre à jour les statistiques initiales
        updateFooterStats();
        
        return footer;
    }

    private VBox createStatItem(String label, String value, String color) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 18px; -fx-font-weight: bold;", color));
        valueLabel.setId("stat-" + label.replace(" ", "-").toLowerCase());
        
        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        box.getChildren().addAll(valueLabel, descLabel);
        return box;
    }

    private void updateFooterStats() {
        updateFooterStats(camions);
    }

    private void updateFooterStats(ObservableList<Camion> camionList) {
        // Calculer les statistiques
        long disponibles = camionList.stream().filter(c -> "Disponible".equals(c.getEtat())).count();
        long enRoute = camionList.stream().filter(c -> "En route".equals(c.getEtat())).count();
        long maintenance = camionList.stream().filter(c -> 
            "En maintenance".equals(c.getEtat()) || "En réparation".equals(c.getEtat())).count();
        double capaciteTotale = camionList.stream().mapToDouble(Camion::getCapacite).sum();

        // Mettre à jour les labels de statistiques
        if (statDisponible != null && !statDisponible.getChildren().isEmpty()) {
            Label valueLabel = (Label) statDisponible.getChildren().get(0);
            valueLabel.setText(disponibles + "/" + camionList.size());
        }
        
        if (statEnRoute != null && !statEnRoute.getChildren().isEmpty()) {
            Label valueLabel = (Label) statEnRoute.getChildren().get(0);
            valueLabel.setText(String.valueOf(enRoute));
        }
        
        if (statMaintenance != null && !statMaintenance.getChildren().isEmpty()) {
            Label valueLabel = (Label) statMaintenance.getChildren().get(0);
            valueLabel.setText(String.valueOf(maintenance));
        }
        
        if (statCapacite != null && !statCapacite.getChildren().isEmpty()) {
            Label valueLabel = (Label) statCapacite.getChildren().get(0);
            valueLabel.setText(String.format("%.1f T", capaciteTotale));
        }
    }

    private void showAddCamionDialog() {
        Dialog<Camion> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un nouveau camion");
        dialog.setHeaderText("Remplissez les informations du camion");

        // Création du formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField matriculeField = new TextField();
        TextField modeleField = new TextField();
        TextField capaciteField = new TextField();
        DatePicker dateMaintenancePicker = new DatePicker();
        TextField kilometrageField = new TextField();
        TextField consommationField = new TextField();
        ComboBox<String> etatCombo = new ComboBox<>();
        etatCombo.getItems().addAll("Disponible", "En route", "En maintenance", "En réparation", "En chargement");
        etatCombo.setValue("Disponible");
        
        ComboBox<String> chauffeurCombo = new ComboBox<>();
        chauffeurCombo.setItems(chauffeursList);
        chauffeurCombo.setValue("");

        grid.add(new Label("Matricule:"), 0, 0);
        grid.add(matriculeField, 1, 0);
        grid.add(new Label("Modèle:"), 0, 1);
        grid.add(modeleField, 1, 1);
        grid.add(new Label("Capacité (T):"), 0, 2);
        grid.add(capaciteField, 1, 2);
        grid.add(new Label("État:"), 0, 3);
        grid.add(etatCombo, 1, 3);
        grid.add(new Label("Chauffeur:"), 0, 4);
        grid.add(chauffeurCombo, 1, 4);
        grid.add(new Label("Dernière maintenance:"), 0, 5);
        grid.add(dateMaintenancePicker, 1, 5);
        grid.add(new Label("Kilométrage:"), 0, 6);
        grid.add(kilometrageField, 1, 6);
        grid.add(new Label("Consommation (L/100km):"), 0, 7);
        grid.add(consommationField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        // Boutons
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Validation
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    // Validation des champs obligatoires
                    if (matriculeField.getText().isEmpty() || modeleField.getText().isEmpty() || 
                        capaciteField.getText().isEmpty()) {
                        showAlert("Erreur", "Veuillez remplir tous les champs obligatoires (*)");
                        return null;
                    }
                    
                    // Vérifier si le matricule existe déjà
                    if (camionService.matriculeExists(matriculeField.getText())) {
                        showAlert("Erreur", "Ce matricule existe déjà");
                        return null;
                    }
                    
                    double capacite;
                    try {
                        capacite = Double.parseDouble(capaciteField.getText());
                        if (capacite <= 0) {
                            showAlert("Erreur", "La capacité doit être positive");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "La capacité doit être un nombre valide");
                        return null;
                    }
                    
                    // Traitement des nouvelles colonnes
                    Date derniereMaintenance = null;
                    if (dateMaintenancePicker.getValue() != null) {
                        derniereMaintenance = java.sql.Date.valueOf(dateMaintenancePicker.getValue());
                    }
                    
                    int kilometrage = 0;
                    if (!kilometrageField.getText().isEmpty()) {
                        try {
                            kilometrage = Integer.parseInt(kilometrageField.getText());
                            if (kilometrage < 0) {
                                showAlert("Erreur", "Le kilométrage ne peut pas être négatif");
                                return null;
                            }
                        } catch (NumberFormatException e) {
                            showAlert("Erreur", "Le kilométrage doit être un nombre entier");
                            return null;
                        }
                    }
                    
                    double consommation = 0.0;
                    if (!consommationField.getText().isEmpty()) {
                        try {
                            consommation = Double.parseDouble(consommationField.getText());
                            if (consommation < 0) {
                                showAlert("Erreur", "La consommation ne peut pas être négative");
                                return null;
                            }
                        } catch (NumberFormatException e) {
                            showAlert("Erreur", "La consommation doit être un nombre valide");
                            return null;
                        }
                    }
                    
                    String chauffeur = chauffeurCombo.getValue();
                    if (chauffeur == null || chauffeur.isEmpty()) {
                        chauffeur = "";
                    }
                    
                    // Créer le camion
                    Camion camion = new Camion(
                        0,
                        matriculeField.getText(),
                        modeleField.getText(),
                        capacite,
                        etatCombo.getValue(),
                        chauffeur,
                        derniereMaintenance,
                        kilometrage,
                        consommation
                    );
                    
                    // Ajouter à la BD
                    if (camionService.addCamion(camion)) {
                        // Recharger depuis la BD
                        loadCamionsFromDatabase();
                        updateTableAndStats();
                        showAlert("Succès", "Camion ajouté avec succès !");
                    } else {
                        showAlert("Erreur", "Erreur lors de l'ajout à la base de données");
                    }
                    
                    return camion;
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editCamion(Camion camion) {
        Dialog<Camion> dialog = new Dialog<>();
        dialog.setTitle("Modifier le camion");
        dialog.setHeaderText("Modifier les informations du camion: " + camion.getMatricule());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField matriculeField = new TextField(camion.getMatricule());
        TextField modeleField = new TextField(camion.getModele());
        TextField capaciteField = new TextField(String.valueOf(camion.getCapacite()));
        DatePicker dateMaintenancePicker = new DatePicker();
        if (camion.getDerniereMaintenance() != null) {
            dateMaintenancePicker.setValue(((java.sql.Date) camion.getDerniereMaintenance()).toLocalDate());
        }
        TextField kilometrageField = new TextField(String.valueOf(camion.getKilometrage()));
        TextField consommationField = new TextField(String.valueOf(camion.getConsommation()));
        ComboBox<String> etatCombo = new ComboBox<>();
        etatCombo.getItems().addAll("Disponible", "En route", "En maintenance", "En réparation", "En chargement");
        etatCombo.setValue(camion.getEtat());
        
        ComboBox<String> chauffeurCombo = new ComboBox<>();
        chauffeurCombo.setItems(chauffeursList);
        chauffeurCombo.setValue(camion.getChauffeur() != null ? camion.getChauffeur() : "");

        grid.add(new Label("Matricule:"), 0, 0);
        grid.add(matriculeField, 1, 0);
        grid.add(new Label("Modèle:"), 0, 1);
        grid.add(modeleField, 1, 1);
        grid.add(new Label("Capacité (T):"), 0, 2);
        grid.add(capaciteField, 1, 2);
        grid.add(new Label("État:"), 0, 3);
        grid.add(etatCombo, 1, 3);
        grid.add(new Label("Chauffeur:"), 0, 4);
        grid.add(chauffeurCombo, 1, 4);
        grid.add(new Label("Dernière maintenance:"), 0, 5);
        grid.add(dateMaintenancePicker, 1, 5);
        grid.add(new Label("Kilométrage:"), 0, 6);
        grid.add(kilometrageField, 1, 6);
        grid.add(new Label("Consommation (L/100km):"), 0, 7);
        grid.add(consommationField, 1, 7);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validation
                    if (matriculeField.getText().isEmpty() || modeleField.getText().isEmpty() || 
                        capaciteField.getText().isEmpty()) {
                        showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
                        return null;
                    }
                    
                    // Vérifier si le matricule existe déjà (sauf pour ce camion)
                    if (!camion.getMatricule().equals(matriculeField.getText()) && 
                        camionService.matriculeExists(matriculeField.getText(), camion.getId())) {
                        showAlert("Erreur", "Ce matricule existe déjà");
                        return null;
                    }
                    
                    double capacite;
                    try {
                        capacite = Double.parseDouble(capaciteField.getText());
                        if (capacite <= 0) {
                            showAlert("Erreur", "La capacité doit être positive");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "La capacité doit être un nombre valide");
                        return null;
                    }
                    
                    // Traitement des nouvelles colonnes
                    Date derniereMaintenance = null;
                    if (dateMaintenancePicker.getValue() != null) {
                        derniereMaintenance = java.sql.Date.valueOf(dateMaintenancePicker.getValue());
                    }
                    
                    int kilometrage;
                    try {
                        kilometrage = Integer.parseInt(kilometrageField.getText());
                        if (kilometrage < 0) {
                            showAlert("Erreur", "Le kilométrage ne peut pas être négatif");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Le kilométrage doit être un nombre entier");
                        return null;
                    }
                    
                    double consommation;
                    try {
                        consommation = Double.parseDouble(consommationField.getText());
                        if (consommation < 0) {
                            showAlert("Erreur", "La consommation ne peut pas être négative");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "La consommation doit être un nombre valide");
                        return null;
                    }
                    
                    String chauffeur = chauffeurCombo.getValue();
                    if (chauffeur == null || chauffeur.isEmpty()) {
                        chauffeur = "";
                    }
                    
                    // Mettre à jour le camion
                    camion.setMatricule(matriculeField.getText());
                    camion.setModele(modeleField.getText());
                    camion.setCapacite(capacite);
                    camion.setEtat(etatCombo.getValue());
                    camion.setChauffeur(chauffeur);
                    camion.setDerniereMaintenance(derniereMaintenance);
                    camion.setKilometrage(kilometrage);
                    camion.setConsommation(consommation);
                    
                    // Mettre à jour dans la BD
                    if (camionService.updateCamion(camion)) {
                        // Recharger depuis la BD
                        loadCamionsFromDatabase();
                        updateTableAndStats();
                        showAlert("Succès", "Camion modifié avec succès !");
                    } else {
                        showAlert("Erreur", "Erreur lors de la mise à jour");
                    }
                    
                    return camion;
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur: " + e.getMessage());
                    e.printStackTrace();
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteCamion(Camion camion) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le camion");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le camion " + camion.getMatricule() + " ?\nCette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Supprimer de la BD
                if (camionService.deleteCamion(camion.getId())) {
                    // Recharger depuis la BD
                    loadCamionsFromDatabase();
                    updateTableAndStats();
                    showAlert("Succès", "Camion supprimé avec succès !");
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression");
                }
            }
        });
    }

    private void showDetails(Camion camion) {
        Alert details = new Alert(Alert.AlertType.INFORMATION);
        details.setTitle("Détails du camion");
        details.setHeaderText("Informations détaillées - " + camion.getMatricule());
        
        String chauffeurInfo = camion.getChauffeur() != null && !camion.getChauffeur().isEmpty() ? 
                               camion.getChauffeur() : "Aucun chauffeur assigné";
        
        // Formater la date de maintenance
        String dateMaintenance = "Non spécifiée";
        if (camion.getDerniereMaintenance() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            dateMaintenance = sdf.format(camion.getDerniereMaintenance());
        }
        
        String content = String.format(
            "📋 **Informations du camion**\n\n" +
            "🔢 ID: %d\n" +
            "🚛 Matricule: %s\n" +
            "🏷️ Modèle: %s\n" +
            "⚖️ Capacité: %.1f tonnes\n" +
            "🔄 État: %s\n" +
            "👤 Chauffeur: %s\n\n" +
            "🔧 **Informations techniques**\n" +
            "📅 Dernière maintenance: %s\n" +
            "🛣️ Kilométrage: %s km\n" +
            "⛽ Consommation moyenne: %.1f L/100km",
            camion.getId(), 
            camion.getMatricule(), 
            camion.getModele(), 
            camion.getCapacite(), 
            camion.getEtat(), 
            chauffeurInfo,
            dateMaintenance,
            String.format("%,d", camion.getKilometrage()),
            camion.getConsommation()
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

    private void goBack() {
        if (camionService != null) {
            camionService.close();
        }
        new com.transport.ui.dashboard.AdminDashboard(stage, currentAdmin).show();
    }
}