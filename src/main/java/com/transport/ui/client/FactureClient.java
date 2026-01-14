package com.transport.ui.client;

import com.transport.model.Utilisateur;
import com.transport.model.Facture;
import com.transport.service.FactureService;
import com.transport.ui.dashboard.ClientDashboard;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Node;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.awt.Desktop;
import javafx.util.Callback;

import java.io.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class FactureClient {
    private Stage stage;
    private Utilisateur currentClient;
    private FactureService factureService;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private TableView<Facture> tableFactures;
    private ObservableList<Facture> factureList;

    public FactureClient(Stage stage, Utilisateur client) {
        this.stage = stage;
        this.currentClient = client;
        this.factureService = new FactureService();
        this.factureList = FXCollections.observableArrayList();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Contenu principal
        VBox content = createContent();
        root.setCenter(content);

        Scene scene = new Scene(root, 1200, 800);
        loadCSS(scene);

        stage.setScene(scene);
        stage.setTitle("Mes Factures - Client");
        stage.centerOnScreen();
        stage.show();

        // Charger les factures
        chargerFactures();
    }

    private HBox createHeader() {
        HBox header = new HBox(15);
        header.setStyle("-fx-background-color: #2c3e50; -fx-padding: 15 25;");
        header.setAlignment(Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-weight: bold; " +
                        "-fx-border-color: white; -fx-border-radius: 5; -fx-padding: 8 15;");
        backBtn.setOnAction(e -> goBack());

        Label title = new Label("📄 Mes Factures");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Remplacer par d'autres statistiques si nécessaire, ou supprimer
        HBox statsBox = new HBox(20);
        statsBox.setAlignment(Pos.CENTER);
        
        // Exemple: Statistique du nombre total de factures
        VBox statTotal = createStatBox("Total", "", "#3498db");
        
        statsBox.getChildren().addAll(statTotal);

        header.getChildren().addAll(backBtn, title, spacer, statsBox);
        return header;
    }

    private VBox createStatBox(String label, String icon, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px;");

        Label countLabel = new Label("0");
        countLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        Label textLabel = new Label(label);
        textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        box.getChildren().addAll(iconLabel, countLabel, textLabel);
        return box;
    }

    private VBox createContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-radius: 10;");

        // Titre et description
        Label title = new Label("Liste de mes factures");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label description = new Label("Retrouvez ici toutes vos factures de transport. Sélectionnez une facture pour voir les détails.");
        description.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        description.setWrapText(true);

        // Boutons d'action
        HBox actionBox = new HBox(15);
        actionBox.setAlignment(Pos.CENTER_LEFT);

        Button refreshBtn = new Button("🔄 Actualiser");
        refreshBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; " +
                           "-fx-background-radius: 5; -fx-padding: 10 20;");
        refreshBtn.setOnAction(e -> chargerFactures());

        actionBox.getChildren().addAll(refreshBtn);

        // Table des factures
        VBox tableContainer = new VBox(10);
        tableContainer.setPadding(new Insets(15));
        tableContainer.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label tableTitle = new Label("Factures");
        tableTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        tableFactures = new TableView<>();
        tableFactures.setStyle("-fx-background-color: white; -fx-border-color: #dee2e6; -fx-border-radius: 5;");
        tableFactures.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableFactures.setPrefHeight(400);

        // Configurer les colonnes
        configurerColonnes();

        // Ajouter un placeholder
        Label placeholder = new Label("Chargement des factures...");
        placeholder.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        tableFactures.setPlaceholder(placeholder);

        tableContainer.getChildren().addAll(tableTitle, tableFactures);

        content.getChildren().addAll(title, description, actionBox, tableContainer);
        return content;
    }

    private void configurerColonnes() {
        // Colonne Numéro
        TableColumn<Facture, String> colNumero = new TableColumn<>("Numéro");
        colNumero.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? cellData.getValue().getNumeroFacture() : ""));
        colNumero.setPrefWidth(150);

        // Colonne Date
        TableColumn<Facture, String> colDate = new TableColumn<>("Date");
        colDate.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Facture facture = cellData.getValue();
                return facture != null && facture.getDateFacture() != null ? 
                       facture.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }));
        colDate.setPrefWidth(100);

        // Colonne Échéance
        TableColumn<Facture, String> colEcheance = new TableColumn<>("Échéance");
        colEcheance.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> {
                Facture facture = cellData.getValue();
                return facture != null && facture.getDateEcheance() != null ? 
                       facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "";
            }));
        colEcheance.setPrefWidth(100);

        // Colonne Montant TTC
        TableColumn<Facture, String> colTTC = new TableColumn<>("Montant TTC");
        colTTC.setCellValueFactory(cellData -> 
            javafx.beans.binding.Bindings.createStringBinding(() -> 
                cellData.getValue() != null ? 
                df.format(cellData.getValue().getMontantTTC()) + " €" : "0.00 €"));
        colTTC.setPrefWidth(120);

        // Colonne Actions
        TableColumn<Facture, Void> colActions = new TableColumn<>("Actions");
        colActions.setPrefWidth(150);
        colActions.setCellFactory(new Callback<TableColumn<Facture, Void>, TableCell<Facture, Void>>() {
            @Override
            public TableCell<Facture, Void> call(TableColumn<Facture, Void> param) {
                return new TableCell<Facture, Void>() {
                    private final Button pdfBtn = new Button("📥 PDF");
                    
                    {
                        pdfBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 12px; " +
                                       "-fx-padding: 5 10; -fx-border-radius: 5;");
                        pdfBtn.setOnAction(event -> {
                            Facture facture = getTableView().getItems().get(getIndex());
                            if (facture != null) {
                                genererPDF(facture);
                            }
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox hbox = new HBox(5, pdfBtn);
                            hbox.setAlignment(Pos.CENTER);
                            setGraphic(hbox);
                        }
                    }
                };
            }
        });

        // SUPPRIMER colStatut de la liste des colonnes
        tableFactures.getColumns().addAll(colNumero, colDate, colEcheance, colTTC, colActions);
    }

    private void afficherDetailsFacture(Facture facture, Label num, Label date, Label echeance, 
                                       Label statut, Label ht, Label tva, Label ttc) {
        num.setText(facture.getNumeroFacture());
        
        if (facture.getDateFacture() != null) {
            date.setText(facture.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            date.setText("-");
        }
        
        if (facture.getDateEcheance() != null) {
            echeance.setText(facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        } else {
            echeance.setText("-");
        }
        
        statut.setText("-"); // Ou supprimer complètement le label statut
        
        ht.setText(df.format(facture.getMontantTotal()) + " €");
        tva.setText(df.format(facture.getTva()) + " €");
        ttc.setText(df.format(facture.getMontantTTC()) + " €");
        
    }

    private void reinitialiserDetails(Label num, Label date, Label echeance, Label statut, 
                                     Label ht, Label tva, Label ttc) {
        num.setText("-");
        date.setText("-");
        echeance.setText("-");
        statut.setText("-");
        ht.setText("- €");
        tva.setText("- €");
        ttc.setText("- €");
        statut.setStyle("");
    }

    private void chargerFactures() {
        try {
            // Afficher un message de chargement
            tableFactures.setPlaceholder(new Label("Chargement des factures en cours..."));
            
            System.out.println("DEBUG: Chargement des factures pour client ID = " + currentClient.getId());
            
            // Récupérer les factures réelles depuis la base de données
            List<Facture> factures = factureService.getFacturesByClientId(currentClient.getId());
            
            factureList.clear();
            
            if (factures != null && !factures.isEmpty()) {
                factureList.addAll(factures);
                tableFactures.setItems(factureList);
                
                System.out.println("✅ " + factures.size() + " facture(s) chargée(s) pour le client " + 
                                 currentClient.getNom() + " (ID: " + currentClient.getId() + ")");
                
                // Sélectionner la première facture si disponible
                if (!factureList.isEmpty()) {
                    tableFactures.getSelectionModel().selectFirst();
                }
                
            } else {
                // Si aucune facture, afficher un message explicatif
                Label noDataLabel = new Label("Aucune facture disponible pour le moment.");
                noDataLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
                tableFactures.setPlaceholder(noDataLabel);
                
                System.out.println("ℹ️ Aucune facture trouvée pour le client " + currentClient.getId());
            }
            
            // Mettre à jour les statistiques
            updateStatistics();
            
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Erreur lors du chargement des factures: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
            tableFactures.setPlaceholder(errorLabel);
        }
    }

    private void updateStatistics() {
        if (factureList == null || factureList.isEmpty()) {
            // Mettre les compteurs à 0
            updateHeaderStatistics(0); // Modifier selon vos nouvelles statistiques
            return;
        }
        
        // Calculer le nombre total de factures
        long total = factureList.stream()
                .filter(f -> f != null)
                .count();
        
        updateHeaderStatistics(total);
    }
    
    private void updateHeaderStatistics(long total) {
        // Mettre à jour l'header
        if (stage != null && stage.getScene() != null) {
            BorderPane root = (BorderPane) stage.getScene().getRoot();
            if (root != null && root.getTop() instanceof HBox) {
                HBox header = (HBox) root.getTop();
                if (header.getChildren().size() > 3) {
                    Node statsNode = header.getChildren().get(3);
                    if (statsNode instanceof HBox) {
                        HBox statsBox = (HBox) statsNode;
                        
                        // Mettre à jour les compteurs
                        if (statsBox.getChildren().size() >= 1) {
                            for (int i = 0; i < 1; i++) {
                                Node node = statsBox.getChildren().get(i);
                                if (node instanceof VBox) {
                                    VBox vbox = (VBox) node;
                                    if (vbox.getChildren().size() > 1) {
                                        Node countNode = vbox.getChildren().get(1);
                                        if (countNode instanceof Label) {
                                            Label countLabel = (Label) countNode;
                                            countLabel.setText(String.valueOf(total));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void genererPDF(Facture facture) {
        try {
            // Créer le sélecteur de fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la facture PDF");
            fileChooser.setInitialFileName("Facture_" + facture.getNumeroFacture() + ".html");
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Fichiers HTML", "*.html"),
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
            );
            
            File file = fileChooser.showSaveDialog(stage);
            
            if (file != null) {
                // Générer le contenu HTML de la facture
                String htmlContent = genererHTMLFacture(facture);
                
                // Écrire dans le fichier
                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.print(htmlContent);
                }
                
                // Ouvrir le fichier dans le navigateur par défaut
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(file);
                }
                
                showAlert("Succès", "Facture générée avec succès :\n" + file.getAbsolutePath() + 
                         "\n\nVous pouvez l'imprimer en PDF depuis votre navigateur (Ctrl+P → Enregistrer au format PDF).");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la génération de la facture : " + e.getMessage());
        }
    }

    private String genererHTMLFacture(Facture facture) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang='fr'>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset='UTF-8'>\n");
        sb.append("    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n");
        sb.append("    <title>Facture ").append(facture.getNumeroFacture()).append("</title>\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; margin: 40px; color: #333; }\n");
        sb.append("        .header { text-align: center; margin-bottom: 40px; }\n");
        sb.append("        .facture-title { color: #2c3e50; font-size: 28px; font-weight: bold; margin-bottom: 5px; }\n");
        sb.append("        .facture-numero { color: #7f8c8d; font-size: 18px; margin-bottom: 20px; }\n");
        sb.append("        .company-info { margin-bottom: 30px; padding: 15px; background-color: #f8f9fa; border-radius: 5px; }\n");
        sb.append("        .company-name { color: #2c3e50; font-size: 20px; font-weight: bold; margin-bottom: 10px; }\n");
        sb.append("        .info-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 30px; margin-bottom: 30px; }\n");
        sb.append("        .info-box { padding: 15px; background-color: #e8f4f8; border-radius: 5px; border: 1px solid #3498db; }\n");
        sb.append("        .info-title { color: #2c3e50; font-size: 16px; font-weight: bold; margin-bottom: 10px; }\n");
        sb.append("        .table-container { margin: 30px 0; }\n");
        sb.append("        table { width: 100%; border-collapse: collapse; }\n");
        sb.append("        th { background-color: #2c3e50; color: white; padding: 12px; text-align: left; }\n");
        sb.append("        td { padding: 10px; border-bottom: 1px solid #ddd; }\n");
        sb.append("        .total-row { background-color: #f8f9fa; font-weight: bold; }\n");
        sb.append("        .total-amount { color: #2ecc71; font-size: 20px; }\n");
        sb.append("        .footer { margin-top: 40px; padding-top: 20px; border-top: 2px solid #2c3e50; font-size: 12px; color: #7f8c8d; }\n");
        sb.append("        .logo { font-size: 24px; font-weight: bold; color: #3498db; margin-bottom: 10px; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");
        
        // En-tête
        sb.append("    <div class='header'>\n");
        sb.append("        <div class='logo'>🚛 TRANSPORT EXPRESS</div>\n");
        sb.append("        <div class='facture-title'>FACTURE</div>\n");
        sb.append("        <div class='facture-numero'>").append(facture.getNumeroFacture()).append("</div>\n");
        sb.append("    </div>\n");
        
        // Informations de l'entreprise
        sb.append("    <div class='company-info'>\n");
        sb.append("        <div class='company-name'>TRANSPORT EXPRESS SARL</div>\n");
        sb.append("        <div>123 Avenue des Transports, Zone industriel Ain Sebaa Casablanca, Maroc</div>\n");
        sb.append("        <div>Tél: 05 23 45 67 89 • Email: contact@transport-express.ma</div>\n");
        sb.append("        <div>SIRET: 123 456 789 00012 • TVA Intra: MA12345678901</div>\n");
        sb.append("    </div>\n");
        
        // Grille d'informations
        sb.append("    <div class='info-grid'>\n");
        
        // Client
        sb.append("        <div class='info-box'>\n");
        sb.append("            <div class='info-title'>FACTURÉ À</div>\n");
        sb.append("            <div><strong>").append(currentClient.getNom()).append("</strong></div>\n");
        sb.append("            <div>ID Client: ").append(currentClient.getId()).append("</div>\n");
        sb.append("        </div>\n");
        
        // Détails facture
        sb.append("        <div class='info-box'>\n");
        sb.append("            <div class='info-title'>DÉTAILS FACTURE</div>\n");
        sb.append("            <div><strong>Date facture:</strong> ").append(facture.getDateFacture().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</div>\n");
        sb.append("            <div><strong>Date échéance:</strong> ").append(facture.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</div>\n");
        // SUPPRIMER la section statut
        sb.append("        </div>\n");
        sb.append("    </div>\n");
        
        // Tableau des montants
        sb.append("    <div class='table-container'>\n");
        sb.append("        <h3>DÉTAILS FINANCIERS</h3>\n");
        sb.append("        <table>\n");
        sb.append("            <thead>\n");
        sb.append("                <tr>\n");
        sb.append("                    <th>DESCRIPTION</th>\n");
        sb.append("                    <th>MONTANT</th>\n");
        sb.append("                </tr>\n");
        sb.append("            </thead>\n");
        sb.append("            <tbody>\n");
        sb.append("                <tr>\n");
        sb.append("                    <td>Transport de marchandises</td>\n");
        sb.append("                    <td>").append(df.format(facture.getMontantTotal())).append(" €</td>\n");
        sb.append("                </tr>\n");
        sb.append("                <tr>\n");
        sb.append("                    <td>TVA (20%)</td>\n");
        sb.append("                    <td>").append(df.format(facture.getTva())).append(" €</td>\n");
        sb.append("                </tr>\n");
        sb.append("                <tr class='total-row'>\n");
        sb.append("                    <td><strong>TOTAL TTC</strong></td>\n");
        sb.append("                    <td class='total-amount'><strong>").append(df.format(facture.getMontantTTC())).append(" €</strong></td>\n");
        sb.append("                </tr>\n");
        sb.append("            </tbody>\n");
        sb.append("        </table>\n");
        sb.append("    </div>\n");
        
        // Informations de paiement
        sb.append("    <div class='payment-info'>\n");
        sb.append("        <h3>CONDITIONS DE PAIEMENT</h3>\n");
        sb.append("        <ul>\n");
        sb.append("            <li>Paiement à réception de la facture</li>\n");
        sb.append("            <li>Paiement par virement bancaire uniquement</li>\n");
        sb.append("            <li><strong>IBAN:</strong> FR76 1234 5678 9012 3456 7890 123</li>\n");
        sb.append("            <li><strong>BIC:</strong> TRSPFRPP</li>\n");
        sb.append("            <li><strong>Référence à mentionner:</strong> ").append(facture.getNumeroFacture()).append("</li>\n");
        sb.append("            <li>En cas de retard, pénalités conformément à l'article L441-6 du code de commerce</li>\n");
        sb.append("        </ul>\n");
        sb.append("    </div>\n");
        
        // Mentions légales
        sb.append("    <div class='footer'>\n");
        sb.append("        <p>TVA applicable au taux en vigueur. Ce document a valeur de facture. Toute réclamation doit être formulée dans les 15 jours suivant la réception.</p>\n");
        sb.append("        <p><strong>TRANSPORT EXPRESS SARL</strong> - Capital social: 50 000 € - RCS Paris 123 456 789</p>\n");
        sb.append("    </div>\n");
        
        sb.append("</body>\n");
        sb.append("</html>\n");
        
        return sb.toString();
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
            String cssPath = getClass().getResource("/com/transport/ressources/client-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour FactureClient");
        }
    }

    private void goBack() {
        new ClientDashboard(stage, currentClient).show();
    }
}