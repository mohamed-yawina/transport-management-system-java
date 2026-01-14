package com.transport.ui.client;

import com.transport.model.Utilisateur;
import com.transport.ui.dashboard.ClientDashboard;
import com.transport.model.Tarif;
import com.transport.model.Livraison;
import com.transport.model.Facture;
import com.transport.service.DemandeService;
import com.transport.service.TarifService;
import com.transport.service.FactureService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class DemandeTransportView {
    private Stage stage;
    private Utilisateur currentClient;
    private DemandeService demandeService;
    private TarifService tarifService;
    private FactureService factureService;
    private DecimalFormat df = new DecimalFormat("#,##0.00");
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Champs du formulaire
    private TextField telephoneField;
    private TextArea adresseField;
    private ComboBox<String> typeCombo;
    private TextField destinationField;
    private TextField distanceField;
    private TextField poidsField;
    private TextArea descriptionField;
    private Label prixDetailLabel;
    private Label prixTotalLabel;
    private Label infoLabel;

    public DemandeTransportView(Stage stage, Utilisateur client) {
        this.stage = stage;
        this.currentClient = client;
        this.demandeService = new DemandeService();
        this.tarifService = new TarifService();
        this.factureService = new FactureService();
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f8f9fa;");

        // Header
        HBox header = createHeader("🚛 Nouvelle Demande de Transport");
        root.setTop(header);

        // Formulaire
        VBox form = createDemandeForm();

        ScrollPane scrollPane = new ScrollPane(form);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        root.setCenter(scrollPane);

        Scene scene = new Scene(root, 1000, 700);
        loadCSS(scene);

        stage.setScene(scene);
        stage.setTitle("Nouvelle Demande - Client");
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

        header.getChildren().addAll(backBtn, titleLabel, spacer);
        return header;
    }

    private VBox createDemandeForm() {
        VBox form = new VBox(25);
        form.setPadding(new Insets(30));
        form.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        Label title = new Label("Formulaire de Demande de Transport");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label subtitle = new Label("Remplissez tous les champs pour créer votre demande de transport");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        VBox clientInfo = createClientInfoSection();
        VBox livraisonInfo = createLivraisonInfoSection();
        VBox prixInfo = createPrixInfoSection();
        HBox buttonBox = createButtonBox();

        form.getChildren().addAll(title, subtitle, clientInfo, livraisonInfo, prixInfo, buttonBox);
        return form;
    }

    private VBox createClientInfoSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label sectionTitle = new Label("👤 Informations du Client");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        TextField nomField = new TextField(currentClient.getNom());
        nomField.setEditable(false);
        nomField.setStyle("-fx-background-color: #e9ecef;");

        TextField emailField = new TextField(currentClient.getEmail());
        emailField.setEditable(false);
        emailField.setStyle("-fx-background-color: #e9ecef;");

        telephoneField = new TextField();
        telephoneField.setPromptText("Votre numéro de téléphone (ex: 06 12 34 56 78)");

        adresseField = new TextArea();
        adresseField.setPromptText("Votre adresse complète de départ (rue, code postal, ville)");
        adresseField.setPrefRowCount(3);

        String fieldStyle = "-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;";
        telephoneField.setStyle(fieldStyle);
        adresseField.setStyle(fieldStyle);

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Téléphone*:"), 0, 2);
        grid.add(telephoneField, 1, 2);
        grid.add(new Label("Adresse de départ*:"), 0, 3);
        grid.add(adresseField, 1, 3);

        Label obligatoireNote = new Label("* Champs obligatoires");
        obligatoireNote.setStyle("-fx-font-size: 12px; -fx-text-fill: #e74c3c; -fx-font-style: italic;");
        grid.add(obligatoireNote, 1, 4);

        section.getChildren().addAll(sectionTitle, grid);
        return section;
    }

    private VBox createLivraisonInfoSection() {
        VBox section = new VBox(10);
        section.setPadding(new Insets(15));
        section.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 10;");

        Label sectionTitle = new Label("📍 Détails de la Livraison");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(15);
        grid.setPadding(new Insets(10, 0, 0, 0));

        // Chargement des types de transport depuis la BD
        typeCombo = new ComboBox<>();
        List<Tarif> tarifsActifs = tarifService.getAllTarifs().stream()
                .filter(Tarif::isActif)
                .collect(Collectors.toList());

        ObservableList<String> types = FXCollections.observableArrayList();
        for (Tarif tarif : tarifsActifs) {
            types.add(tarif.getType());
        }

        if (types.isEmpty()) {
            types.addAll("Standard", "Express", "Fragile", "Refrigéré", "Volume");
        }

        typeCombo.setItems(types);
        if (!types.isEmpty()) {
            typeCombo.setValue(types.get(0));
        }
        typeCombo.setStyle("-fx-background-radius: 5; -fx-pref-width: 250;");

        destinationField = new TextField();
        destinationField.setPromptText("Adresse complète de destination");

        distanceField = new TextField();
        distanceField.setPromptText("Distance en kilomètres (ex: 150.5)");

        poidsField = new TextField();
        poidsField.setPromptText("Poids en tonnes (ex: 2.5)");

        descriptionField = new TextArea();
        descriptionField.setPromptText("Description détaillée des marchandises (facultatif)");
        descriptionField.setPrefRowCount(3);

        String fieldStyle = "-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;";
        destinationField.setStyle(fieldStyle);
        distanceField.setStyle(fieldStyle);
        poidsField.setStyle(fieldStyle);
        descriptionField.setStyle(fieldStyle);

        grid.add(new Label("Type de transport*:"), 0, 0);
        grid.add(typeCombo, 1, 0);
        grid.add(new Label("Destination*:"), 0, 1);
        grid.add(destinationField, 1, 1);
        grid.add(new Label("Distance (km)*:"), 0, 2);
        grid.add(distanceField, 1, 2);
        grid.add(new Label("Poids (tonnes)*:"), 0, 3);
        grid.add(poidsField, 1, 3);
        grid.add(new Label("Description:"), 0, 4);
        grid.add(descriptionField, 1, 4);

        Button calculerBtn = new Button("💰 Calculer le prix");
        calculerBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        calculerBtn.setOnAction(e -> calculerPrix());

        HBox buttonBox = new HBox(calculerBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        section.getChildren().addAll(sectionTitle, grid, buttonBox);
        return section;
    }

    private VBox createPrixInfoSection() {
        VBox section = new VBox(15);
        section.setPadding(new Insets(20));
        section.setStyle("-fx-background-color: #e8f4f8; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-width: 2;");

        Label sectionTitle = new Label("💰 Estimation du Prix");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        infoLabel = new Label("Remplissez les informations de livraison et cliquez sur 'Calculer le prix'");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        infoLabel.setWrapText(true);

        prixDetailLabel = new Label("");
        prixDetailLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        prixDetailLabel.setWrapText(true);

        prixTotalLabel = new Label("");
        prixTotalLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2ecc71;");

        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: #3498db;");

        section.getChildren().addAll(sectionTitle, infoLabel, separator, prixDetailLabel, prixTotalLabel);
        return section;
    }

    private void calculerPrix() {
        try {
            String type = typeCombo.getValue();
            String distanceStr = distanceField.getText().trim();
            String poidsStr = poidsField.getText().trim();

            if (type == null || distanceStr.isEmpty() || poidsStr.isEmpty()) {
                showAlert("Erreur", "Veuillez sélectionner le type et saisir la distance et le poids.");
                return;
            }

            double distance = Double.parseDouble(distanceStr);
            double poids = Double.parseDouble(poidsStr);

            if (distance <= 0 || poids <= 0) {
                showAlert("Erreur", "La distance et le poids doivent être supérieurs à 0.");
                return;
            }

            Tarif tarif = tarifService.getTarifByType(type);
            if (tarif == null || !tarif.isActif()) {
                showAlert("Erreur", "Ce type de transport n'est pas disponible.");
                return;
            }

            double prixKm = tarif.getPrixParKm();
            double prixTon = tarif.getPrixParTon();
            double totalHT = prixKm * distance + prixTon * poids;
            double tva = totalHT * 0.20;
            double totalTTC = totalHT + tva;

            infoLabel.setText("Estimation pour le transport " + type + " :");
            prixDetailLabel.setText(
                    "• " + df.format(distance) + " km × " + df.format(prixKm) + " €/km = " + df.format(prixKm * distance) + " €\n" +
                    "• " + df.format(poids) + " t × " + df.format(prixTon) + " €/t = " + df.format(prixTon * poids) + " €\n" +
                    "• Sous-total HT = " + df.format(totalHT) + " €\n" +
                    "• TVA (20%) = " + df.format(tva) + " €"
            );
            prixTotalLabel.setText("TOTAL TTC : " + df.format(totalTTC) + " €");

        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez entrer des valeurs numériques valides pour la distance et le poids.");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors du calcul : " + e.getMessage());
        }
    }

    private HBox createButtonBox() {
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button annulerBtn = new Button("Annuler");
        annulerBtn.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 12 30;");
        annulerBtn.setOnAction(e -> goBack());

        Button soumettreBtn = new Button("Soumettre la demande");
        soumettreBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 12 30;");
        soumettreBtn.setOnAction(e -> soumettreDemande());

        buttonBox.getChildren().addAll(annulerBtn, soumettreBtn);
        return buttonBox;
    }

    private void soumettreDemande() {
        // Validation des champs obligatoires
        StringBuilder erreurs = new StringBuilder();

        if (telephoneField.getText().trim().isEmpty()) {
            erreurs.append("• Numéro de téléphone obligatoire\n");
        }
        
        if (adresseField.getText().trim().isEmpty()) {
            erreurs.append("• Adresse de départ obligatoire\n");
        }
        
        if (destinationField.getText().trim().isEmpty()) {
            erreurs.append("• Destination obligatoire\n");
        }
        
        if (distanceField.getText().trim().isEmpty()) {
            erreurs.append("• Distance obligatoire\n");
        }
        
        if (poidsField.getText().trim().isEmpty()) {
            erreurs.append("• Poids obligatoire\n");
        }
        
        // Vérifier si le prix a été calculé
        if (prixTotalLabel.getText() == null || prixTotalLabel.getText().isEmpty() || 
            prixTotalLabel.getText().contains("Remplissez")) {
            erreurs.append("• Veuillez calculer le prix avant de soumettre\n");
        }

        if (erreurs.length() > 0) {
            showAlert("Champs obligatoires", "Veuillez corriger les erreurs suivantes :\n\n" + erreurs.toString());
            return;
        }

        try {
            // Récupérer les valeurs
            double distance = Double.parseDouble(distanceField.getText().trim());
            double poids = Double.parseDouble(poidsField.getText().trim());
            String type = typeCombo.getValue();
            
            // Récupérer le tarif
            Tarif tarif = tarifService.getTarifByType(type);
            
            if (tarif == null) {
                showAlert("Erreur", "Type de transport invalide.");
                return;
            }
            
            if (!tarif.isActif()) {
                showAlert("Erreur", "Ce type de transport n'est plus disponible");
                return;
            }
            
            // Calculer les prix
            double prixHT = (tarif.getPrixParKm() * distance) + (tarif.getPrixParTon() * poids);
            double tva = prixHT * 0.20;
            double prixTTC = prixHT + tva;
            
            // Générer un numéro de référence
            String reference = "DEM-" + System.currentTimeMillis();
            
            // Afficher la confirmation
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmation de soumission");
            confirm.setHeaderText("Confirmez-vous la soumission de cette demande ?");
            confirm.setContentText(
                "Type: " + type + "\n" +
                "De: " + adresseField.getText().trim() + "\n" +
                "À: " + destinationField.getText().trim() + "\n" +
                "Téléphone: " + telephoneField.getText().trim() + "\n" +
                "Distance: " + df.format(distance) + " km\n" +
                "Poids: " + df.format(poids) + " tonnes\n" +
                "Total TTC: " + df.format(prixTTC) + " €\n\n" +
                "Référence: " + reference
            );
            
            // Ajouter des boutons personnalisés
            confirm.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            
            // Afficher et attendre la réponse
            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    // 1. Sauvegarder la demande dans la base de données
                    Livraison livraison = sauvegarderDemande(reference, tarif, distance, poids, prixHT, tva, prixTTC);
                    
                    if (livraison != null && livraison.getId() > 0) {
                        // 2. Générer automatiquement une facture
                        boolean factureGeneree = genererFactureDansBD(livraison.getId(), prixHT, tva, prixTTC);
                        
                        // 3. Préparer le message de succès
                        String messageSucces = "✅ Votre demande a été soumise avec succès !\n\n" +
                            "Référence: " + reference + "\n" +
                            "ID Livraison: " + livraison.getId() + "\n" +
                            "Total: " + df.format(prixTTC) + " €\n" +
                            "Statut: " + livraison.getStatus() + "\n";
                        
                        if (factureGeneree) {
                            // Récupérer les détails de la facture générée
                            Facture facture = factureService.getFactureByLivraisonId(livraison.getId());
                            if (facture != null) {
                                messageSucces += "\n📄 FACTURE GÉNÉRÉE AUTOMATIQUEMENT :\n" +
                                    "Numéro facture: " + facture.getNumeroFacture() + "\n" +
                                    "Date facturation: " + facture.getDateFacture().format(dateFormatter) + "\n" +
                                    "Échéance: " + facture.getDateEcheance().format(dateFormatter) + "\n";
                            } else {
                                messageSucces += "\n📄 Une facture a été générée automatiquement.";
                            }
                        } else {
                            messageSucces += "\n⚠️ La facture n'a pas pu être générée automatiquement.\n" +
                                           "Contactez le service administratif.";
                        }
                        
                        showAlert("Succès", messageSucces);
                        
                        // 4. Afficher les détails dans la console
                        genererFactureConsole(reference, tarif, distance, poids, prixHT, tva, prixTTC, livraison.getId());
                        
                        // 5. Retour au dashboard
                        goBack();
                    } else {
                        showAlert("Erreur", "Une erreur est survenue lors de la sauvegarde de la demande.");
                    }
                }
            });
            
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Veuillez vérifier les valeurs numériques (distance et poids)");
        } catch (Exception e) {
            showAlert("Erreur", "Erreur lors de la soumission: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Livraison sauvegarderDemande(String reference, Tarif tarif, double distance, 
                                        double poids, double prixHT, double tva, double prixTTC) {
        try {
            // Créer l'objet Livraison
            Livraison livraison = new Livraison();
            livraison.setNomClient(currentClient.getNom());
            livraison.setDestination(destinationField.getText().trim());
            livraison.setTypeTransport(tarif.getType());
            livraison.setDistanceKm(distance);
            livraison.setPoidsTonnes(poids);
            livraison.setPrixTotal(prixTTC);
            livraison.setStatus("En attente");
            
            // Sauvegarder dans la base de données
            boolean success = demandeService.createLivraison(livraison);
            
            if (success) {
                System.out.println("\n=== DEMANDE SAUVEGARDÉE DANS LA BD ===");
                System.out.println("ID Livraison: " + livraison.getId());
                System.out.println("Client: " + currentClient.getNom() + " (ID: " + currentClient.getId() + ")");
                System.out.println("Destination: " + destinationField.getText().trim());
                System.out.println("Type transport: " + tarif.getType());
                System.out.println("Distance: " + distance + " km");
                System.out.println("Poids: " + poids + " tonnes");
                System.out.println("Prix HT: " + df.format(prixHT) + " €");
                System.out.println("TVA (20%): " + df.format(tva) + " €");
                System.out.println("Prix total TTC: " + df.format(prixTTC) + " €");
                System.out.println("Référence: " + reference);
                System.out.println("Téléphone client: " + telephoneField.getText().trim());
                System.out.println("Adresse de départ: " + adresseField.getText().trim());
                if (!descriptionField.getText().trim().isEmpty()) {
                    System.out.println("Description: " + descriptionField.getText().trim());
                }
                System.out.println("=======================================\n");
                
                return livraison;
            } else {
                System.err.println("❌ Échec de la sauvegarde de la livraison dans la BD");
                return null;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("❌ Exception lors de la sauvegarde: " + e.getMessage());
            return null;
        }
    }

    /**
     * Génère une facture dans la base de données
     */
    private boolean genererFactureDansBD(int idLivraison, double montantTotal, double tva, double montantTTC) {
        try {
            System.out.println("\n=== TENTATIVE DE GÉNÉRATION DE FACTURE ===");
            System.out.println("ID Livraison: " + idLivraison);
            System.out.println("Montant HT: " + df.format(montantTotal) + " €");
            System.out.println("TVA: " + df.format(tva) + " €");
            System.out.println("Montant TTC: " + df.format(montantTTC) + " €");
            
            // S'assurer que la table existe
            factureService.createTableIfNotExists();
            
            // Générer la facture
            boolean success = factureService.genererFacture(idLivraison, montantTotal, tva, montantTTC);
            
            if (success) {
                // Récupérer et afficher les détails
                Facture facture = factureService.getFactureByLivraisonId(idLivraison);
                if (facture != null) {
                    System.out.println("\n✅ FACTURE GÉNÉRÉE AVEC SUCCÈS DANS LA BD");
                    System.out.println("Numéro facture: " + facture.getNumeroFacture());
                    System.out.println("ID Livraison: " + facture.getIdLivraison());
                    System.out.println("Montant HT: " + df.format(facture.getMontantTotal()) + " €");
                    System.out.println("TVA: " + df.format(facture.getTva()) + " €");
                    System.out.println("Montant TTC: " + df.format(facture.getMontantTTC()) + " €");
                    System.out.println("Date facture: " + facture.getDateFacture());
                    System.out.println("Date échéance: " + facture.getDateEcheance());
                    System.out.println("=========================================\n");
                }
                return true;
            } else {
                System.err.println("❌ Échec de la génération de la facture dans la BD");
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Erreur lors de la génération de la facture: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private void genererFactureConsole(String reference, Tarif tarif, double distance,
                                      double poids, double prixHT, double tva, double prixTTC, int idLivraison) {
        System.out.println("\n=== FACTURE (DÉTAIL CONSOLE) ===");
        System.out.println("RÉFÉRENCE DEMANDE: " + reference);
        System.out.println("ID LIVRAISON: " + idLivraison);
        System.out.println("DATE: " + LocalDate.now());
        System.out.println("\n--- CLIENT ---");
        System.out.println("Nom: " + currentClient.getNom());
        System.out.println("Email: " + currentClient.getEmail());
        System.out.println("Téléphone: " + telephoneField.getText().trim());
        System.out.println("Adresse de départ: " + adresseField.getText().trim());
        System.out.println("\n--- TRANSPORT ---");
        System.out.println("Type: " + tarif.getType());
        System.out.println("Destination: " + destinationField.getText().trim());
        System.out.println("Distance: " + distance + " km");
        System.out.println("Poids: " + poids + " tonnes");
        if (!descriptionField.getText().trim().isEmpty()) {
            System.out.println("Description: " + descriptionField.getText().trim());
        }
        System.out.println("\n--- DÉTAIL DES PRIX ---");
        System.out.println("Transport (" + distance + " km × " + tarif.getPrixParKm() + " €/km): " + df.format(tarif.getPrixParKm() * distance) + " €");
        System.out.println("Manutention (" + poids + " t × " + tarif.getPrixParTon() + " €/t): " + df.format(tarif.getPrixParTon() * poids) + " €");
        System.out.println("Sous-total HT: " + df.format(prixHT) + " €");
        System.out.println("TVA (20%): " + df.format(tva) + " €");
        System.out.println("\nTOTAL TTC: " + df.format(prixTTC) + " €");
        System.out.println("==================================\n");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Ajuster la taille de l'alerte
        alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
        
        alert.showAndWait();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/client-style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception e) {
            System.out.println("CSS non trouvé pour DemandeTransportView");
        }
    }

    private void goBack() {
        new ClientDashboard(stage, currentClient).show();
    }
}