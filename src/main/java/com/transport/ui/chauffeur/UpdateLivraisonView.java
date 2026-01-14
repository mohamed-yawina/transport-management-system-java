package com.transport.ui.chauffeur;

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
import javafx.util.Callback;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UpdateLivraisonView {
    private Stage stage;
    private Utilisateur currentChauffeur;
    private DemandeService demandeService;
    private TableView<Livraison> tableLivraisons;
    private ObservableList<Livraison> livraisonList;

    public UpdateLivraisonView(Stage stage, Utilisateur chauffeur) {
        this.stage = stage;
        this.currentChauffeur = chauffeur;
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

        Scene scene = new Scene(root, 1200, 800);
        loadCSS(scene);

        stage.setScene(scene);
        stage.setTitle("Mes Livraisons - Chauffeur");
        stage.centerOnScreen();
        stage.show();

        // Charger les livraisons
        chargerLivraisons();
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 25;");
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-border-color: white; -fx-border-radius: 5; -fx-padding: 8 15;");
        backBtn.setOnAction(e -> goBack());

        Label title = new Label("🚚 Mes Livraisons");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filtres
        ComboBox<String> filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("Tous", "En attente", "Confirmée", "En cours", "Terminée", "Annulée");
        filterStatus.setValue("Tous");
        filterStatus.setStyle("-fx-background-color: white; -fx-border-radius: 5; -fx-padding: 5 10;");
        filterStatus.setOnAction(e -> filtrerLivraisons(filterStatus.getValue()));

        header.getChildren().addAll(backBtn, title, spacer, filterStatus);
        return header;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Titre et description
        Label title = new Label("Gestion des Livraisons");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label description = new Label("Liste de toutes vos livraisons. Mettez à jour le statut de chaque livraison.");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        description.setWrapText(true);

        // Boutons d'action
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-background-radius: 5; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> {
            System.out.println("=== BOUTON ACTUALISER CLIQUE ===");
            chargerLivraisons();
        });

        actionBox.getChildren().addAll(refreshBtn);

        // Table des livraisons
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(15));
        tableContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label tableTitle = new Label("Livraisons assignées");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        tableLivraisons = new TableView<>();
        tableLivraisons.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        tableLivraisons.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableLivraisons.setPrefHeight(400);

        // Configurer les colonnes
        configurerColonnes();

        // Ajouter un placeholder
        Label placeholder = new Label("Chargement des livraisons...");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        tableLivraisons.setPlaceholder(placeholder);

        tableContainer.getChildren().addAll(tableTitle, tableLivraisons);

        content.getChildren().addAll(title, description, actionBox, tableContainer);
        return content;
    }

    private void configurerColonnes() {
        // Colonne ID
        TableColumn<Livraison, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? String.valueOf(cellData.getValue().getId()) : ""));
        colId.setPrefWidth(50);

        // Colonne Destination
        TableColumn<Livraison, String> colDestination = new TableColumn<>("Destination");
        colDestination.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getDestination() : ""));
        colDestination.setPrefWidth(150);

        // Colonne Client
        TableColumn<Livraison, String> colClient = new TableColumn<>("Client");
        colClient.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getNomClient() : ""));
        colClient.setPrefWidth(120);

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
                cellData.getValue() != null ? String.valueOf(cellData.getValue().getDistanceKm()) : ""));
        colDistance.setPrefWidth(80);

        // Colonne Poids
        TableColumn<Livraison, String> colPoids = new TableColumn<>("Poids (t)");
        colPoids.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? String.valueOf(cellData.getValue().getPoidsTonnes()) : ""));
        colPoids.setPrefWidth(80);

        // Colonne Date
        TableColumn<Livraison, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Livraison liv = cellData.getValue();
                return liv != null && liv.getDateCreation() != null ? 
                       liv.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }));
        colDate.setPrefWidth(100);

        // Colonne Statut
        TableColumn<Livraison, String> colStatut = new TableColumn<>("Statut");
        colStatut.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getStatus() : ""));
        colStatut.setPrefWidth(120);
        
        // Colorer les cellules de statut
        colStatut.setCellFactory(new Callback<TableColumn<Livraison, String>, TableCell<Livraison, String>>() {
            @Override
            public TableCell<Livraison, String> call(TableColumn<Livraison, String> param) {
                return new TableCell<Livraison, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);
                            switch (item) {
                                case "En attente":
                                    setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold; -fx-alignment: CENTER;");
                                    break;
                                case "Confirmée":
                                    setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold; -fx-alignment: CENTER;");
                                    break;
                                case "En cours":
                                    setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold; -fx-alignment: CENTER;");
                                    break;
                                case "Terminée":
                                    setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-alignment: CENTER;");
                                    break;
                                case "Annulée":
                                    setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                                    break;
                                default:
                                    setStyle("-fx-text-fill: #34495e; -fx-alignment: CENTER;");
                            }
                        }
                    }
                };
            }
        });

        // Colonne Actions
        TableColumn<Livraison, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(200);
        colActions.setCellFactory(new Callback<TableColumn<Livraison, Void>, TableCell<Livraison, Void>>() {
            @Override
            public TableCell<Livraison, Void> call(TableColumn<Livraison, Void> param) {
                return new TableCell<Livraison, Void>() {
                    private final Button demarrerBtn = new Button("▶ Démarrer");
                    private final Button terminerBtn = new Button("✓ Terminer");
                    
                    {
                        // Style des boutons
                        String buttonStyle = "-fx-font-size: 11px; -fx-padding: 5 10; -fx-border-radius: 5; -fx-font-weight: bold;";
                        
                        demarrerBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;" + buttonStyle);
                        demarrerBtn.setOnAction(event -> {
                            Livraison livraison = getTableView().getItems().get(getIndex());
                            if (livraison != null) {
                                demarrerLivraison(livraison);
                            }
                        });
                        
                        terminerBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white;" + buttonStyle);
                        terminerBtn.setOnAction(event -> {
                            Livraison livraison = getTableView().getItems().get(getIndex());
                            if (livraison != null) {
                                terminerLivraison(livraison);
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Livraison livraison = getTableView().getItems().get(getIndex());
                            HBox hbox = new HBox(5);
                            
                            if (livraison != null) {
                                switch (livraison.getStatus()) {
                                    case "En attente":
                                        Label labelAttente = new Label("En attente de confirmation");
                                        labelAttente.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                                        hbox.getChildren().add(labelAttente);
                                        break;
                                    case "Confirmée":
                                        hbox.getChildren().addAll(demarrerBtn);
                                        break;
                                    case "En cours":
                                        hbox.getChildren().addAll(terminerBtn);
                                        break;
                                    case "Terminée":
                                    case "Annulée":
                                        Label label = new Label("Statut: " + livraison.getStatus());
                                        label.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                                        hbox.getChildren().add(label);
                                        break;
                                }
                            }
                            
                            hbox.setAlignment(Pos.CENTER);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });

        tableLivraisons.getColumns().addAll(colId, colDestination, colClient, colType, 
                                          colDistance, colPoids, colDate, colStatut, colActions);
    }

    private void chargerLivraisons() {
        try {
            System.out.println("\n=== CHARGEMENT DES LIVRAISONS ===");
            System.out.println("Chauffeur ID: " + currentChauffeur.getId());
            System.out.println("Chauffeur nom: " + currentChauffeur.getNom());
            
            // Afficher un message de chargement
            Label loadingLabel = new Label("Chargement des livraisons en cours...");
            loadingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
            tableLivraisons.setPlaceholder(loadingLabel);
            
            // Récupérer les livraisons du chauffeur
            List<Livraison> livraisons = demandeService.getLivraisonsByChauffeurId(currentChauffeur.getId());
            
            livraisonList.clear();
            
            if (livraisons != null && !livraisons.isEmpty()) {
                livraisonList.addAll(livraisons);
                tableLivraisons.setItems(livraisonList);
                
                System.out.println("✅ " + livraisons.size() + " livraison(s) chargée(s)");
                
                // Afficher le statut de chaque livraison chargée
                for (Livraison liv : livraisons) {
                    System.out.println("  - ID: " + liv.getId() + 
                                     ", Destination: " + liv.getDestination() + 
                                     ", Statut: " + liv.getStatus() +
                                     ", Chauffeur ID: " + liv.getIdChauffeur());
                }
                
                // Sélectionner la première livraison si disponible
                if (!livraisonList.isEmpty()) {
                    tableLivraisons.getSelectionModel().selectFirst();
                }
                
            } else {
                // Si aucune livraison, afficher un message explicatif
                Label noDataLabel = new Label("Aucune livraison assignée pour le moment.");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                tableLivraisons.setPlaceholder(noDataLabel);
                
                System.out.println("ℹ️ Aucune livraison trouvée pour le chauffeur");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des livraisons: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            tableLivraisons.setPlaceholder(errorLabel);
            
            System.err.println("❌ Erreur: " + e.getMessage());
        }
    }

    private void filtrerLivraisons(String filtre) {
        if (filtre.equals("Tous")) {
            tableLivraisons.setItems(livraisonList);
        } else {
            ObservableList<Livraison> filteredList = FXCollections.observableArrayList();
            for (Livraison livraison : livraisonList) {
                if (livraison.getStatus().equals(filtre)) {
                    filteredList.add(livraison);
                }
            }
            tableLivraisons.setItems(filteredList);
            
            if (filteredList.isEmpty()) {
                tableLivraisons.setPlaceholder(new Label("Aucune livraison avec le statut \"" + filtre + "\""));
            }
        }
    }

    private void demarrerLivraison(Livraison livraison) {
        System.out.println("\n=== DÉMARRER LIVRAISON ===");
        System.out.println("ID: " + livraison.getId());
        System.out.println("Destination: " + livraison.getDestination());
        System.out.println("Client: " + livraison.getNomClient());
        System.out.println("Statut actuel: " + livraison.getStatus());
        System.out.println("Chauffeur ID (livraison): " + livraison.getIdChauffeur());
        System.out.println("Chauffeur ID (current): " + currentChauffeur.getId());
        
        // Vérifier que la livraison appartient bien à ce chauffeur
        if (livraison.getIdChauffeur() != currentChauffeur.getId()) {
            System.err.println("❌ ERREUR: Cette livraison n'est pas assignée à ce chauffeur");
            showAlert("Erreur", "Cette livraison n'est pas assignée à votre compte.\n" +
                     "Livraison chauffeur ID: " + livraison.getIdChauffeur() + "\n" +
                     "Votre ID: " + currentChauffeur.getId());
            return;
        }
        
        // Vérifier que le statut est bien "Confirmée"
        if (!"Confirmée".equals(livraison.getStatus())) {
            showAlert("Erreur", "Impossible de démarrer cette livraison.\n" +
                     "Statut actuel: " + livraison.getStatus() + "\n" +
                     "Seules les livraisons 'Confirmée' peuvent être démarrées.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Démarrer la livraison");
        confirm.setHeaderText("Confirmez-vous le démarrage de cette livraison ?");
        confirm.setContentText(
            "ID: " + livraison.getId() + "\n" +
            "Destination: " + livraison.getDestination() + "\n" +
            "Client: " + livraison.getNomClient() + "\n\n" +
            "Le statut sera changé à 'En cours'."
        );
        
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.out.println("✅ Confirmation OK - Mise à jour du statut...");
                
                try {
                    // TEST: Vérifier d'abord que la livraison existe en BD
                    System.out.println("🔍 Vérification de la livraison en BD avant mise à jour...");
                    Livraison beforeUpdate = demandeService.getLivraisonById(livraison.getId());
                    if (beforeUpdate != null) {
                        System.out.println("   Statut en BD avant: " + beforeUpdate.getStatus());
                    }
                    
                    // Appeler la méthode de mise à jour
                    System.out.println("📤 Appel de updateLivraisonStatus...");
                    boolean success = demandeService.updateLivraisonStatus(livraison.getId(), "En cours");
                    
                    System.out.println("📥 Résultat updateLivraisonStatus: " + success);
                    
                    if (success) {
                        // Vérifier immédiatement après la mise à jour
                        System.out.println("🔍 Vérification immédiate après mise à jour...");
                        Livraison afterUpdate = demandeService.getLivraisonById(livraison.getId());
                        if (afterUpdate != null) {
                            System.out.println("   Statut en BD après: " + afterUpdate.getStatus());
                        }
                        
                        // Mettre à jour l'objet local
                        livraison.setStatus("En cours");
                        
                        // Rafraîchir la table
                        int index = tableLivraisons.getItems().indexOf(livraison);
                        if (index >= 0) {
                            tableLivraisons.getItems().set(index, livraison);
                            System.out.println("✓ Ligne mise à jour dans la table à l'index: " + index);
                        }
                        
                        // Forcer le rafraîchissement
                        tableLivraisons.refresh();
                        
                        showAlert("Succès", "✅ La livraison #" + livraison.getId() + 
                                  " a été démarrée avec succès !\n\n" +
                                  "Statut: En cours");
                        
                        // Recharger après un délai
                        new Thread(() -> {
                            try {
                                Thread.sleep(500);
                                javafx.application.Platform.runLater(() -> {
                                    System.out.println("🔄 Rechargement complet...");
                                    chargerLivraisons();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        
                    } else {
                        showAlert("Erreur", "❌ Échec de la mise à jour.\n" +
                                 "Veuillez vérifier les logs du serveur.");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Exception: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Erreur", "❌ Erreur technique: " + e.getMessage());
                }
            } else {
                System.out.println("❌ Confirmation annulée");
            }
        });
    }

    private void terminerLivraison(Livraison livraison) {
        System.out.println("\n=== TERMINER LIVRAISON ===");
        System.out.println("ID: " + livraison.getId());
        System.out.println("Destination: " + livraison.getDestination());
        System.out.println("Statut actuel: " + livraison.getStatus());
        
        // Vérifier que le statut est bien "En cours"
        if (!"En cours".equals(livraison.getStatus())) {
            showAlert("Erreur", "Impossible de terminer cette livraison.\n" +
                     "Statut actuel: " + livraison.getStatus() + "\n" +
                     "Seules les livraisons 'En cours' peuvent être terminées.");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Terminer la livraison");
        confirm.setHeaderText("Confirmez-vous la fin de cette livraison ?");
        confirm.setContentText(
            "ID: " + livraison.getId() + "\n" +
            "Destination: " + livraison.getDestination() + "\n" +
            "Client: " + livraison.getNomClient() + "\n\n" +
            "La livraison sera marquée comme 'Terminée'."
        );
        
        confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                System.out.println("✅ Confirmation OK - Début du traitement...");
                
                try {
                    // Utiliser la même méthode updateLivraisonStatus
                    boolean success = demandeService.updateLivraisonStatus(livraison.getId(), "Terminée");
                    
                    System.out.println("Résultat updateLivraisonStatus: " + success);
                    
                    if (success) {
                        // Mettre à jour l'objet local immédiatement
                        livraison.setStatus("Terminée");
                        
                        // Forcer le rafraîchissement de la ligne
                        int index = tableLivraisons.getItems().indexOf(livraison);
                        if (index >= 0) {
                            tableLivraisons.getItems().set(index, livraison);
                        }
                        
                        // Rafraîchir toute la table
                        tableLivraisons.refresh();
                        
                        // Afficher message de succès
                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("Succès");
                        successAlert.setHeaderText(null);
                        successAlert.setContentText("✅ La livraison #" + livraison.getId() + 
                                                  " a été terminée avec succès !\n\n" +
                                                  "Le statut a été mis à jour à: Terminée");
                        successAlert.showAndWait();
                        
                        // Recharger les données après un court délai
                        new Thread(() -> {
                            try {
                                Thread.sleep(1000);
                                javafx.application.Platform.runLater(() -> {
                                    System.out.println("🔄 Rechargement des données après terminaison...");
                                    chargerLivraisons();
                                });
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }).start();
                        
                    } else {
                        showAlert("Erreur", "❌ Échec de la mise à jour du statut.\n" +
                                 "La méthode updateLivraisonStatus a retourné false.");
                    }
                    
                } catch (Exception e) {
                    System.err.println("❌ Exception dans terminerLivraison: " + e.getMessage());
                    e.printStackTrace();
                    showAlert("Erreur", "❌ Erreur technique: " + e.getMessage());
                }
            } else {
                System.out.println("❌ Confirmation annulée");
            }
        });
    }

    private void testerFonctionnalite() {
        System.out.println("\n=== TEST DE FONCTIONNALITÉ ===");
        
        // Vérifier les livraisons chargées
        System.out.println("Nombre de livraisons chargées: " + livraisonList.size());
        
        if (!livraisonList.isEmpty()) {
            Livraison liv = livraisonList.get(0);
            System.out.println("Première livraison:");
            System.out.println("  ID: " + liv.getId());
            System.out.println("  Destination: " + liv.getDestination());
            System.out.println("  Statut: " + liv.getStatus());
            System.out.println("  Chauffeur ID: " + liv.getIdChauffeur());
            
            // Tester la méthode getLivraisonById
            System.out.println("\n🔍 Test getLivraisonById:");
            Livraison testLiv = demandeService.getLivraisonById(liv.getId());
            if (testLiv != null) {
                System.out.println("  ✓ Livraison trouvée");
                System.out.println("  Statut: " + testLiv.getStatus());
            } else {
                System.out.println("  ✗ Livraison non trouvée");
            }
            
            // Tester la mise à jour
            System.out.println("\n🔧 Test updateLivraisonStatus:");
            String nouveauStatut = "Confirmée".equals(liv.getStatus()) ? "En cours" : "Terminée";
            System.out.println("  Nouveau statut à tester: " + nouveauStatut);
            
            boolean result = demandeService.updateLivraisonStatus(liv.getId(), nouveauStatut);
            System.out.println("  Résultat: " + result);
            
            // Recharger et vérifier
            System.out.println("\n🔄 Rechargement...");
            chargerLivraisons();
            
            showAlert("Test", "Test effectué. Voir la console pour les résultats.");
        } else {
            showAlert("Test", "Aucune livraison disponible pour tester.");
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
            String cssPath = getClass().getResource("/com/transport/ressources/chauffeur-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour UpdateLivraisonView");
        }
    }

    private void goBack() {
        new com.transport.ui.dashboard.ChauffeurDashboard(stage, currentChauffeur).show();
    }
}