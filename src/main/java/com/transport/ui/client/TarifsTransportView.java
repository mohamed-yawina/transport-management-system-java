package com.transport.ui.client;

import com.transport.model.Utilisateur;
import com.transport.service.TarifService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.util.List;

public class TarifsTransportView {

    private Stage stage;
    private Utilisateur currentClient;
    private TableView<Tarif> table;
    private ObservableList<Tarif> tarifs = FXCollections.observableArrayList();
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private TarifService tarifService;

    // Classe pour représenter un tarif (UNE SEULE DÉFINITION)
    public static class Tarif {
        private String type;
        private double prixParKm;
        private double prixParTon;
        private String statut;

        public Tarif(String type, double prixParKm, double prixParTon, String statut) {
            this.type = type;
            this.prixParKm = prixParKm;
            this.prixParTon = prixParTon;
            this.statut = statut;
        }

        // Getters
        public String getType() { return type; }
        public double getPrixParKm() { return prixParKm; }
        public double getPrixParTon() { return prixParTon; }
        public String getStatut() { return statut; }
        
        // Méthode pour calculer l'exemple
        public String getExemple(DecimalFormat df) {
            double total = (prixParKm * 100) + (prixParTon * 5);
            return df.format(total) + " €";
        }
    }

    public TarifsTransportView(Stage stage, Utilisateur client) {
        this.stage = stage;
        this.currentClient = client;
        this.tarifService = new TarifService();
        loadTarifs();
    }

    private void loadTarifs() {
        try {
            tarifs.clear();
            List<com.transport.model.Tarif> dbTarifs = tarifService.getAllTarifs();
            
            for (com.transport.model.Tarif dbTarif : dbTarifs) {
                if (dbTarif.isActif()) {
                    tarifs.add(new Tarif(
                        dbTarif.getType(),
                        dbTarif.getPrixParKm(),
                        dbTarif.getPrixParTon(),
                        dbTarif.isActif() ? "Actif" : "Inactif"
                    ));
                }
            }
            
            System.out.println("✅ " + tarifs.size() + " tarifs actifs chargés");
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des tarifs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader("💰 Tarifs de Transport");
        root.setTop(header);

        // Content
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Nos Tarifs de Transport");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Découvrez nos tarifs compétitifs pour tous vos besoins de transport");
        subtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");

        // Table des tarifs
        table = createTarifsTable();
        VBox tableContainer = new VBox(10);
        tableContainer.getChildren().addAll(createTableToolbar(), table);

        // Informations supplémentaires
        VBox infoBox = createInfoBox();

        content.getChildren().addAll(title, subtitle, tableContainer, infoBox);
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        
        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1200, 700);
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Tarifs de Transport - Client");
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

        Label countLabel = new Label(tarifs.size() + " tarifs disponibles");
        countLabel.setStyle("-fx-text-fill: #f39c12; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 5 15; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, titleLabel, spacer, countLabel);
        return header;
    }

    private HBox createTableToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un type de transport...");
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;");
        searchField.setPrefWidth(300);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTarifs(newValue);
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button newDemandeBtn = new Button("🚛 Nouvelle demande");
        newDemandeBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        newDemandeBtn.setOnAction(e -> {
            new DemandeTransportView(stage, currentClient).show();
        });

        toolbar.getChildren().addAll(searchField, spacer, newDemandeBtn);
        return toolbar;
    }

    private void filterTarifs(String searchText) {
        ObservableList<Tarif> filteredTarifs = FXCollections.observableArrayList();
        
        for (Tarif tarif : tarifs) {
            if (searchText == null || searchText.isEmpty() ||
                tarif.getType().toLowerCase().contains(searchText.toLowerCase())) {
                filteredTarifs.add(tarif);
            }
        }
        
        table.setItems(filteredTarifs);
    }

    private TableView<Tarif> createTarifsTable() {
        TableView<Tarif> tableView = new TableView<>();
        tableView.setItems(tarifs);
        tableView.setStyle("-fx-background-color: transparent;");

        // Colonne Type
        TableColumn<Tarif, String> typeCol = new TableColumn<>("Type Transport");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(200);
        typeCol.setCellFactory(column -> new TableCell<Tarif, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    setStyle("-fx-font-weight: bold; -fx-alignment: CENTER;");
                }
            }
        });

        // Colonne Prix par Km
        TableColumn<Tarif, Double> prixKmCol = new TableColumn<>("Prix par Km (€)");
        prixKmCol.setCellValueFactory(new PropertyValueFactory<>("prixParKm"));
        prixKmCol.setPrefWidth(150);
        prixKmCol.setCellFactory(column -> new TableCell<Tarif, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(df.format(prix) + " €");
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        // Colonne Prix par Tonne
        TableColumn<Tarif, Double> prixTonCol = new TableColumn<>("Prix par Tonne (€)");
        prixTonCol.setCellValueFactory(new PropertyValueFactory<>("prixParTon"));
        prixTonCol.setPrefWidth(150);
        prixTonCol.setCellFactory(column -> new TableCell<Tarif, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(df.format(prix) + " €");
                    setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });


        // Colonne Description
        TableColumn<Tarif, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(300);
        descCol.setCellFactory(column -> new TableCell<Tarif, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setStyle("");
                } else {
                    Tarif tarif = getTableView().getItems().get(getIndex());
                    String description = getDescription(tarif.getType());
                    setText(description);
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-text-fill: #7f8c8d;");
                }
            }
        });

        tableView.getColumns().addAll(typeCol, prixKmCol, prixTonCol, descCol);
        return tableView;
    }

    private String getDescription(String type) {
        switch (type) {
            case "Standard":
                return "Transport standard pour marchandises générales";
            case "Express":
                return "Livraison rapide avec délai garanti";
            case "Fragile":
                return "Transport spécialisé pour objets fragiles";
            case "Refrigéré":
                return "Transport avec contrôle de température";
            case "Dangereux":
                return "Transport de matières dangereuses";
            case "Volume":
                return "Transport pour marchandises volumineuses";
            default:
                return "Service de transport";
        }
    }

    private VBox createInfoBox() {
        VBox infoBox = new VBox(15);
        infoBox.setPadding(new Insets(20));
        infoBox.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10; -fx-border-color: #dee2e6; -fx-border-width: 1;");

        Label infoTitle = new Label("ℹ️ Informations importantes");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        TextArea infoContent = new TextArea(
            "• Les tarifs sont indiqués hors taxes\n" +
            "• TVA applicable: 20%\n" +
            "• Distance minimale facturée: 50 km\n" +
            "• Poids minimal facturé: 1 tonne\n" +
            "• Frais supplémentaires possibles selon la destination\n" +
            "• Pour un devis précis, créez une demande de transport"
        );
        infoContent.setEditable(false);
        infoContent.setWrapText(true);
        infoContent.setStyle("-fx-background-color: transparent; -fx-border-color: transparent; -fx-font-size: 14px;");
        infoContent.setPrefHeight(150);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button contactBtn = new Button("📞 Nous contacter");
        contactBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");

        Button demandeBtn = new Button("🚛 Créer une demande");
        demandeBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        demandeBtn.setOnAction(e -> {
            new DemandeTransportView(stage, currentClient).show();
        });

        buttonBox.getChildren().addAll(contactBtn, demandeBtn);

        infoBox.getChildren().addAll(infoTitle, infoContent, buttonBox);
        return infoBox;
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/client-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé pour TarifsTransportView");
        }
    }

    private void goBack() {
        // IMPORTANT: Si ClientDashboard est dans com.transport.ui.dashboard
        new com.transport.ui.dashboard.ClientDashboard(stage, currentClient).show();
    }
}