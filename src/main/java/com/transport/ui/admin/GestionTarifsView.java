package com.transport.ui.admin;

import com.transport.model.Utilisateur;
import com.transport.model.Tarif; 
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
import javafx.util.Callback;
import java.text.DecimalFormat;
import java.util.List;
import java.time.LocalDateTime;

public class GestionTarifsView {

    private Stage stage;
    private Utilisateur currentAdmin;
    private TableView<Tarif> table;
    private ObservableList<Tarif> tarifs = FXCollections.observableArrayList();
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private TarifService tarifService;
    private ComboBox<String> typeFilter;
    private Label countLabel;
    private VBox stats1, stats2, stats3, stats4;

    public GestionTarifsView(Stage stage, Utilisateur admin) {
        this.stage = stage;
        this.currentAdmin = admin;
        this.tarifService = new TarifService();
        loadTarifsFromDatabase();
    }

    private void loadTarifsFromDatabase() {
        try {
            tarifs.clear();
            List<Tarif> dbTarifs = tarifService.getAllTarifs();
            if (dbTarifs != null && !dbTarifs.isEmpty()) {
                tarifs.addAll(dbTarifs);
                System.out.println("✅ " + tarifs.size() + " tarifs chargés depuis la BD");
            } else {
                System.out.println("⚠️ Aucun tarif trouvé dans la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement des tarifs: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateTableAndStats() {
        table.refresh();
        updateFooterStats();
        if (countLabel != null) {
            countLabel.setText(tarifs.size() + " tarifs");
        }
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader("💰 Gestion des Tarifs");
        
        // Toolbar
        HBox toolbar = createToolbar();
        VBox topBox = new VBox(header, toolbar);
        root.setTop(topBox);

        // Table
        table = createTarifsTable();
        root.setCenter(table);

        // Footer/Stats
        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 1300, 700);
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Gestion des Tarifs - Admin Dashboard");
        stage.centerOnScreen();
        stage.show();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé pour GestionTarifsView");
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

        countLabel = new Label(tarifs.size() + " tarifs");
        countLabel.setStyle("-fx-text-fill: #9b59b6; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 5 15; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, titleLabel, spacer, countLabel);
        return header;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15 25; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Bouton Ajouter
        Button addBtn = new Button("➕ Nouveau Tarif");
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddTarifDialog());

        // Bouton Appliquer augmentation
        Button increaseBtn = new Button("📈 Augmentation 5%");
        increaseBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        increaseBtn.setOnAction(e -> applyIncrease(5));

        // Bouton Réinitialisation
        Button resetBtn = new Button("🔄 Réinitialiser");
        resetBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        resetBtn.setOnAction(e -> resetTarifsToInitial());

        // Bouton Rapport Tarifs
        Button reportBtn = new Button("📊 Rapport Tarifs");
        reportBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        reportBtn.setOnAction(e -> generateTarifReport());

        // Champ recherche
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un tarif...");
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;");
        searchField.setPrefWidth(250);

        // Recherche en temps réel
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterTarifs(newValue, typeFilter.getValue());
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        searchBtn.setOnAction(e -> filterTarifs(searchField.getText(), typeFilter.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filtres
        typeFilter = new ComboBox<>();
        typeFilter.getItems().addAll("Tous", "Standard", "Express", "Fragile", "Refrigéré", "Dangereux", "Volume");
        typeFilter.setValue("Tous");
        typeFilter.setStyle("-fx-background-radius: 5; -fx-pref-width: 120;");
        typeFilter.setOnAction(e -> filterTarifs(searchField.getText(), typeFilter.getValue()));

        toolbar.getChildren().addAll(addBtn, increaseBtn, resetBtn, reportBtn, spacer, 
                                     searchField, searchBtn, 
                                     new Label("Type:"), typeFilter);
        return toolbar;
    }

    private void filterTarifs(String searchText, String typeFilterValue) {
        ObservableList<Tarif> filteredTarifs = FXCollections.observableArrayList();
        
        for (Tarif tarif : tarifs) {
            boolean matchesSearch = searchText == null || searchText.isEmpty() ||
                    tarif.getType().toLowerCase().contains(searchText.toLowerCase());
                    
            boolean matchesType = typeFilterValue == null || typeFilterValue.equals("Tous") || 
                    tarif.getType().equals(typeFilterValue);
            
            if (matchesSearch && matchesType) {
                filteredTarifs.add(tarif);
            }
        }
        
        table.setItems(filteredTarifs);
        updateFooterStats(filteredTarifs);
    }

    private TableView<Tarif> createTarifsTable() {
        TableView<Tarif> tableView = new TableView<>();
        tableView.setItems(tarifs);
        tableView.setStyle("-fx-background-color: transparent;");

        // Colonne ID
        TableColumn<Tarif, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);
        idCol.setStyle("-fx-alignment: CENTER;");

        // Colonne Type
        TableColumn<Tarif, String> typeCol = new TableColumn<>("Type Transport");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        typeCol.setPrefWidth(150);
        typeCol.setCellFactory(column -> new TableCell<Tarif, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    switch (type) {
                        case "Standard" -> setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "Express" -> setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "Fragile" -> setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "Refrigéré" -> setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "Dangereux" -> setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        case "Volume" -> setStyle("-fx-background-color: #1abc9c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                        default -> setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Prix par Km
        TableColumn<Tarif, Double> prixKmCol = new TableColumn<>("Prix/Km (€)");
        prixKmCol.setCellValueFactory(new PropertyValueFactory<>("prixParKm"));
        prixKmCol.setPrefWidth(120);
        prixKmCol.setCellFactory(column -> new TableCell<Tarif, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(df.format(prix) + " €");
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        // Colonne Prix par Tonne
        TableColumn<Tarif, Double> prixTonCol = new TableColumn<>("Prix/Ton (€)");
        prixTonCol.setCellValueFactory(new PropertyValueFactory<>("prixParTon"));
        prixTonCol.setPrefWidth(120);
        prixTonCol.setCellFactory(column -> new TableCell<Tarif, Double>() {
            @Override
            protected void updateItem(Double prix, boolean empty) {
                super.updateItem(prix, empty);
                if (empty || prix == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(df.format(prix) + " €");
                    setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
                }
            }
        });

        // Colonne Statut
        TableColumn<Tarif, String> statutCol = new TableColumn<>("Statut");
        statutCol.setCellValueFactory(cellData -> {
            Tarif tarif = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(tarif.isActif() ? "Actif" : "Inactif");
        });
        statutCol.setPrefWidth(100);
        statutCol.setCellFactory(column -> new TableCell<Tarif, String>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(statut);
                    if (statut.equals("Actif")) {
                        setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 5 10; -fx-alignment: CENTER;");
                    }
                }
            }
        });

        // Colonne Actions
        TableColumn<Tarif, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(new Callback<TableColumn<Tarif, Void>, TableCell<Tarif, Void>>() {
            @Override
            public TableCell<Tarif, Void> call(TableColumn<Tarif, Void> param) {
                return new TableCell<Tarif, Void>() {
                    private final HBox container = new HBox(5);

                    {
                    	Button editBtn = new Button("✎");
                    	editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                    	editBtn.setOnAction(e -> editTarif(getTableView().getItems().get(getIndex())));

                    	Button toggleBtn = new Button("↻");
                    	toggleBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                    	toggleBtn.setOnAction(e -> toggleStatus(getTableView().getItems().get(getIndex())));

                    	Button deleteBtn = new Button("×");
                    	deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10;");
                    	deleteBtn.setOnAction(e -> deleteTarif(getTableView().getItems().get(getIndex())));
                    	
                        container.getChildren().addAll(editBtn, toggleBtn, deleteBtn);
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

        tableView.getColumns().addAll(idCol, typeCol, prixKmCol, prixTonCol, statutCol, actionsCol);
        return tableView;
    }

    private HBox createFooter() {
        HBox footer = new HBox(20);
        footer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15 25; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(Pos.CENTER_LEFT);

        // Créer les éléments de statistiques
        stats1 = createStatItem("Tarifs actifs", "0/0", "#2ecc71");
        stats2 = createStatItem("Prix Km moyen", "0.00 €", "#3498db");
        stats3 = createStatItem("Prix Ton moyen", "0.00 €", "#9b59b6");
        stats4 = createStatItem("Types différents", "0", "#e67e22");

        footer.getChildren().addAll(stats1, stats2, stats3, stats4);
        
        // Mettre à jour les statistiques initiales
        updateFooterStats();
        
        return footer;
    }

    private VBox createStatItem(String label, String value, String color) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 18px; -fx-font-weight: bold;", color));
        
        Label descLabel = new Label(label);
        descLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 12px;");
        
        box.getChildren().addAll(valueLabel, descLabel);
        return box;
    }

    private void updateFooterStats() {
        updateFooterStats(tarifs);
    }

    private void updateFooterStats(ObservableList<Tarif> tarifList) {
        // Calculer les statistiques
        long actifs = tarifList.stream().filter(Tarif::isActif).count();
        double prixKmMoyen = tarifList.stream()
            .filter(Tarif::isActif)
            .mapToDouble(Tarif::getPrixParKm)
            .average().orElse(0);
        double prixTonMoyen = tarifList.stream()
            .filter(Tarif::isActif)
            .mapToDouble(Tarif::getPrixParTon)
            .average().orElse(0);
        long types = tarifList.stream()
            .map(Tarif::getType)
            .distinct()
            .count();

        // Mettre à jour les labels de statistiques
        if (stats1 != null && !stats1.getChildren().isEmpty()) {
            Label valueLabel = (Label) stats1.getChildren().get(0);
            valueLabel.setText(actifs + "/" + tarifList.size());
        }
        
        if (stats2 != null && !stats2.getChildren().isEmpty()) {
            Label valueLabel = (Label) stats2.getChildren().get(0);
            valueLabel.setText(df.format(prixKmMoyen) + " €");
        }
        
        if (stats3 != null && !stats3.getChildren().isEmpty()) {
            Label valueLabel = (Label) stats3.getChildren().get(0);
            valueLabel.setText(df.format(prixTonMoyen) + " €");
        }
        
        if (stats4 != null && !stats4.getChildren().isEmpty()) {
            Label valueLabel = (Label) stats4.getChildren().get(0);
            valueLabel.setText(String.valueOf(types));
        }
    }

    // NOUVELLE MÉTHODE : Réinitialiser les tarifs aux valeurs initiales
    private void resetTarifsToInitial() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Réinitialisation des tarifs");
        confirm.setHeaderText("Réinitialiser tous les tarifs");
        confirm.setContentText("Êtes-vous sûr de vouloir réinitialiser tous les tarifs à leurs valeurs initiales ?\n" +
                              "Toutes les modifications et augmentations seront perdues !");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Créer une boîte de dialogue pour choisir le type de réinitialisation
            Dialog<String> resetDialog = new Dialog<>();
            resetDialog.setTitle("Type de réinitialisation");
            resetDialog.setHeaderText("Choisir le type de réinitialisation");

            // Options de réinitialisation
            ComboBox<String> resetOptions = new ComboBox<>();
            resetOptions.getItems().addAll(
                "Réinitialiser tous les tarifs",
                "Réinitialiser seulement les tarifs standards",
                "Réinitialiser seulement les tarifs express",
                "Réinitialiser tarifs fragiles et réfrigérés"
            );
            resetOptions.setValue("Réinitialiser tous les tarifs");

            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            grid.add(new Label("Type de réinitialisation:"), 0, 0);
            grid.add(resetOptions, 1, 0);

            resetDialog.getDialogPane().setContent(grid);
            
            ButtonType resetButtonType = new ButtonType("Réinitialiser", ButtonBar.ButtonData.OK_DONE);
            resetDialog.getDialogPane().getButtonTypes().addAll(resetButtonType, ButtonType.CANCEL);

            resetDialog.setResultConverter(dialogButton -> {
                if (dialogButton == resetButtonType) {
                    return resetOptions.getValue();
                }
                return null;
            });

            resetDialog.showAndWait().ifPresent(resetType -> {
                try {
                    // Tarifs initiaux basés sur votre image de BD
                    List<Tarif> tarifsInitiaux = List.of(
                        new Tarif(1, "Standard", 1.50, 25.00, true),
                        new Tarif(2, "Express", 2.20, 35.00, true),
                        new Tarif(3, "Fragile", 1.80, 30.00, true),
                        new Tarif(6, "Refrigéré", 2.00, 45.00, true),
                        new Tarif(7, "Dangereux", 3.00, 55.00, false),
                        new Tarif(8, "Volume", 1.30, 22.00, true)
                    );

                    // Appliquer la réinitialisation selon le type choisi
                    int count = 0;
                    for (Tarif tarifInitial : tarifsInitiaux) {
                        // Trouver le tarif correspondant dans la liste actuelle
                        for (Tarif tarifActuel : tarifs) {
                            if (tarifActuel.getId() == tarifInitial.getId()) {
                                // Vérifier le type de réinitialisation
                                boolean shouldReset = false;
                                switch (resetType) {
                                    case "Réinitialiser tous les tarifs":
                                        shouldReset = true;
                                        break;
                                    case "Réinitialiser seulement les tarifs standards":
                                        shouldReset = tarifActuel.getType().equals("Standard");
                                        break;
                                    case "Réinitialiser seulement les tarifs express":
                                        shouldReset = tarifActuel.getType().equals("Express");
                                        break;
                                    case "Réinitialiser tarifs fragiles et réfrigérés":
                                        shouldReset = tarifActuel.getType().equals("Fragile") || 
                                                     tarifActuel.getType().equals("Refrigéré");
                                        break;
                                }

                                if (shouldReset) {
                                    tarifActuel.setPrixParKm(tarifInitial.getPrixParKm());
                                    tarifActuel.setPrixParTon(tarifInitial.getPrixParTon());
                                    
                                    // Mettre à jour dans la BD
                                    if (tarifService.updateTarif(tarifActuel)) {
                                        count++;
                                    }
                                }
                                break;
                            }
                        }
                    }

                    if (count > 0) {
                        // Recharger depuis la BD
                        loadTarifsFromDatabase();
                        updateTableAndStats();
                        showAlert("Succès", count + " tarif(s) réinitialisé(s) avec succès !");
                    } else {
                        showAlert("Information", "Aucun tarif n'a été réinitialisé.");
                    }
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur lors de la réinitialisation: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        }
    }

    // NOUVELLE MÉTHODE : Générer un rapport tarifaire
    private void generateTarifReport() {
        try {
            // Calculer les statistiques
            long totalTarifs = tarifs.size();
            long tarifsActifs = tarifs.stream().filter(Tarif::isActif).count();
            long tarifsInactifs = totalTarifs - tarifsActifs;
            
            double prixKmMin = tarifs.stream()
                .filter(Tarif::isActif)
                .mapToDouble(Tarif::getPrixParKm)
                .min().orElse(0);
            double prixKmMax = tarifs.stream()
                .filter(Tarif::isActif)
                .mapToDouble(Tarif::getPrixParKm)
                .max().orElse(0);
            double prixKmMoyen = tarifs.stream()
                .filter(Tarif::isActif)
                .mapToDouble(Tarif::getPrixParKm)
                .average().orElse(0);
            double prixTonMin = tarifs.stream()
                .filter(Tarif::isActif)
                .mapToDouble(Tarif::getPrixParTon)
                .min().orElse(0);
            double prixTonMax = tarifs.stream()
                .filter(Tarif::isActif)
                .mapToDouble(Tarif::getPrixParTon)
                .max().orElse(0);
            double prixTonMoyen = tarifs.stream()
                .filter(Tarif::isActif)
                .mapToDouble(Tarif::getPrixParTon)
                .average().orElse(0);
            
            // Compter par type
            long standardCount = tarifs.stream().filter(t -> t.getType().equals("Standard")).count();
            long expressCount = tarifs.stream().filter(t -> t.getType().equals("Express")).count();
            long fragileCount = tarifs.stream().filter(t -> t.getType().equals("Fragile")).count();
            long refrigereCount = tarifs.stream().filter(t -> t.getType().equals("Refrigéré")).count();
            long volumeCount = tarifs.stream().filter(t -> t.getType().equals("Volume")).count();
            long dangereuxCount = tarifs.stream().filter(t -> t.getType().equals("Dangereux")).count();

            // Créer le contenu du rapport
            StringBuilder reportContent = new StringBuilder();
            reportContent.append("📊 RAPPORT DES TARIFS - ").append(java.time.LocalDate.now()).append("\n\n");
            reportContent.append("=".repeat(50)).append("\n\n");
            
            reportContent.append("📈 STATISTIQUES GÉNÉRALES\n");
            reportContent.append("   • Total des tarifs: ").append(totalTarifs).append("\n");
            reportContent.append("   • Tarifs actifs: ").append(tarifsActifs).append("\n");
            reportContent.append("   • Tarifs inactifs: ").append(tarifsInactifs).append("\n");
            reportContent.append("   • Prix Km moyen: ").append(df.format(prixKmMoyen)).append(" €\n");
            reportContent.append("   • Prix Tonne moyen: ").append(df.format(prixTonMoyen)).append(" €\n");
            reportContent.append("   • Prix Km min-max: ").append(df.format(prixKmMin)).append(" - ").append(df.format(prixKmMax)).append(" €\n");
            reportContent.append("   • Prix Ton min-max: ").append(df.format(prixTonMin)).append(" - ").append(df.format(prixTonMax)).append(" €\n\n");
            
            reportContent.append("📋 RÉPARTITION PAR TYPE\n");
            reportContent.append("   • Standard: ").append(standardCount).append(" tarif(s)\n");
            reportContent.append("   • Express: ").append(expressCount).append(" tarif(s)\n");
            reportContent.append("   • Fragile: ").append(fragileCount).append(" tarif(s)\n");
            reportContent.append("   • Réfrigéré: ").append(refrigereCount).append(" tarif(s)\n");
            reportContent.append("   • Volume: ").append(volumeCount).append(" tarif(s)\n");
            reportContent.append("   • Dangereux: ").append(dangereuxCount).append(" tarif(s)\n\n");
            
            reportContent.append("💰 DÉTAIL DES TARIFS\n");
            reportContent.append("=".repeat(50)).append("\n");
            
            // Ajouter les tarifs actifs
            reportContent.append("\nTARIFS ACTIFS:\n");
            tarifs.stream()
                .filter(Tarif::isActif)
                .forEach(tarif -> {
                    double totalExemple = (tarif.getPrixParKm() * 100) + (tarif.getPrixParTon() * 5);
                    reportContent.append(String.format("   • %s: %.2f€/km + %.2f€/ton (Ex: 100km + 5t = %.2f€)\n",
                        tarif.getType(), tarif.getPrixParKm(), tarif.getPrixParTon(), totalExemple));
                });
            
            // Ajouter les tarifs inactifs
            if (tarifs.stream().anyMatch(t -> !t.isActif())) {
                reportContent.append("\nTARIFS INACTIFS:\n");
                tarifs.stream()
                    .filter(t -> !t.isActif())
                    .forEach(tarif -> {
                        reportContent.append(String.format("   • %s: %.2f€/km + %.2f€/ton\n",
                            tarif.getType(), tarif.getPrixParKm(), tarif.getPrixParTon()));
                    });
            }
            
            reportContent.append("\n").append("=".repeat(50)).append("\n");
            reportContent.append("🔮 RECOMMANDATIONS\n");
            reportContent.append("   1. Vérifier les tarifs inactifs pour une possible réactivation\n");
            reportContent.append("   2. Comparer les prix avec la concurrence\n");
            reportContent.append("   3. Analyser les demandes clients pour ajuster les tarifs\n");
            reportContent.append("   4. Considérer une augmentation pour les tarifs les plus demandés\n");

            // Afficher le rapport
            TextArea reportArea = new TextArea(reportContent.toString());
            reportArea.setEditable(false);
            reportArea.setWrapText(true);
            reportArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
            
            ScrollPane scrollPane = new ScrollPane(reportArea);
            scrollPane.setFitToWidth(true);
            scrollPane.setPrefSize(800, 600);
            
            Stage reportStage = new Stage();
            reportStage.setTitle("Rapport Tarifaire - " + java.time.LocalDate.now());
            
            VBox vbox = new VBox(10);
            vbox.setPadding(new Insets(15));
            vbox.setStyle("-fx-background-color: #f8f9fa;");
            
            // Boutons d'action
            HBox buttonBox = new HBox(10);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            
            Button closeBtn = new Button("Fermer");
            closeBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-padding: 8 20;");
            closeBtn.setOnAction(e -> reportStage.close());
            
            buttonBox.getChildren().add(closeBtn);
            vbox.getChildren().addAll(scrollPane, buttonBox);
            
            Scene reportScene = new Scene(vbox, 850, 700);
            reportStage.setScene(reportScene);
            reportStage.show();
            
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la génération du rapport: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAddTarifDialog() {
        Dialog<Tarif> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un nouveau tarif");
        dialog.setHeaderText("Définir les paramètres du tarif");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Standard", "Express", "Fragile", "Refrigéré", "Dangereux", "Volume");
        
        TextField prixKmField = new TextField();
        TextField prixTonField = new TextField();
        CheckBox actifCheck = new CheckBox("Tarif actif");
        actifCheck.setSelected(true);

        grid.add(new Label("Type transport:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Prix par km (€):"), 0, 1);
        grid.add(prixKmField, 1, 1);
        grid.add(new Label("Prix par tonne (€):"), 0, 2);
        grid.add(prixTonField, 1, 2);
        grid.add(actifCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    // Validation
                    if (typeCombo.getValue() == null || typeCombo.getValue().isEmpty() ||
                        prixKmField.getText().isEmpty() || prixTonField.getText().isEmpty()) {
                        showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
                        return null;
                    }
                    
                    double prixKm;
                    double prixTon;
                    
                    try {
                        prixKm = Double.parseDouble(prixKmField.getText());
                        prixTon = Double.parseDouble(prixTonField.getText());
                        
                        if (prixKm <= 0 || prixTon <= 0) {
                            showAlert("Erreur", "Les prix doivent être positifs");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Les prix doivent être des nombres valides");
                        return null;
                    }
                    
                    // Créer le tarif
                    Tarif tarif = new Tarif(
                        0, // ID sera généré par la BD
                        typeCombo.getValue(),
                        prixKm,
                        prixTon,
                        actifCheck.isSelected()
                    );
                    
                    // Ajouter à la BD
                    if (tarifService.addTarif(tarif)) {
                        // Recharger depuis la BD
                        loadTarifsFromDatabase();
                        updateTableAndStats();
                        showAlert("Succès", "Tarif ajouté avec succès !");
                    } else {
                        showAlert("Erreur", "Erreur lors de l'ajout à la base de données");
                    }
                    
                    return tarif;
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

    private void editTarif(Tarif tarif) {
        Dialog<Tarif> dialog = new Dialog<>();
        dialog.setTitle("Modifier le tarif");
        dialog.setHeaderText("Modifier les paramètres du tarif: " + tarif.getType());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("Standard", "Express", "Fragile", "Refrigéré", "Dangereux", "Volume");
        typeCombo.setValue(tarif.getType());
        
        TextField prixKmField = new TextField(String.valueOf(tarif.getPrixParKm()));
        TextField prixTonField = new TextField(String.valueOf(tarif.getPrixParTon()));
        CheckBox actifCheck = new CheckBox("Tarif actif");
        actifCheck.setSelected(tarif.isActif());

        grid.add(new Label("Type transport:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Prix par km (€):"), 0, 1);
        grid.add(prixKmField, 1, 1);
        grid.add(new Label("Prix par tonne (€):"), 0, 2);
        grid.add(prixTonField, 1, 2);
        grid.add(actifCheck, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Validation
                    if (typeCombo.getValue() == null || typeCombo.getValue().isEmpty() ||
                        prixKmField.getText().isEmpty() || prixTonField.getText().isEmpty()) {
                        showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
                        return null;
                    }
                    
                    double prixKm;
                    double prixTon;
                    
                    try {
                        prixKm = Double.parseDouble(prixKmField.getText());
                        prixTon = Double.parseDouble(prixTonField.getText());
                        
                        if (prixKm <= 0 || prixTon <= 0) {
                            showAlert("Erreur", "Les prix doivent être positifs");
                            return null;
                        }
                    } catch (NumberFormatException e) {
                        showAlert("Erreur", "Les prix doivent être des nombres valides");
                        return null;
                    }
                    
                    // Mettre à jour le tarif
                    tarif.setType(typeCombo.getValue());
                    tarif.setPrixParKm(prixKm);
                    tarif.setPrixParTon(prixTon);
                    tarif.setActif(actifCheck.isSelected());
                    
                    // Mettre à jour dans la BD
                    if (tarifService.updateTarif(tarif)) {
                        // Recharger depuis la BD
                        loadTarifsFromDatabase();
                        updateTableAndStats();
                        showAlert("Succès", "Tarif modifié avec succès !");
                    } else {
                        showAlert("Erreur", "Erreur lors de la mise à jour");
                    }
                    
                    return tarif;
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

    private void toggleStatus(Tarif tarif) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        String newStatus = tarif.isActif() ? "désactiver" : "activer";
        confirm.setTitle("Changement de statut");
        confirm.setHeaderText(newStatus.substring(0, 1).toUpperCase() + newStatus.substring(1) + " le tarif");
        confirm.setContentText("Voulez-vous " + newStatus + " le tarif " + tarif.getType() + " ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            tarif.setActif(!tarif.isActif());
            
            // Mettre à jour dans la BD
            if (tarifService.updateTarif(tarif)) {
                // Recharger depuis la BD
                loadTarifsFromDatabase();
                updateTableAndStats();
                String status = tarif.isActif() ? "activé" : "désactivé";
                showAlert("Statut modifié", "Le tarif " + tarif.getType() + " a été " + status);
            } else {
                showAlert("Erreur", "Erreur lors de la modification du statut");
            }
        }
    }

    private void applyIncrease(double percentage) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Application d'augmentation");
        confirm.setHeaderText("Augmentation de " + percentage + "%");
        confirm.setContentText("Voulez-vous appliquer une augmentation de " + percentage + "% à tous les tarifs actifs ?");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Appliquer l'augmentation via le service
            if (tarifService.applyIncrease(percentage)) {
                // Recharger depuis la BD
                loadTarifsFromDatabase();
                updateTableAndStats();
                showAlert("Succès", "Augmentation de " + percentage + "% appliquée à tous les tarifs actifs !");
            } else {
                showAlert("Erreur", "Erreur lors de l'application de l'augmentation");
            }
        }
    }

    private void deleteTarif(Tarif tarif) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer le tarif");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer le tarif " + tarif.getType() + " ?\nCette action est irréversible.");

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            // Supprimer de la BD
            if (tarifService.deleteTarif(tarif.getId())) {
                // Recharger depuis la BD
                loadTarifsFromDatabase();
                updateTableAndStats();
                showAlert("Succès", "Tarif supprimé avec succès !");
            } else {
                showAlert("Erreur", "Erreur lors de la suppression");
            }
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goBack() {
        if (tarifService != null) {
            //tarifService.close();
        }
        new com.transport.ui.dashboard.AdminDashboard(stage, currentAdmin).show();
    }
}