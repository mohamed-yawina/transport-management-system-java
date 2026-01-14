package com.transport.ui.admin;

import com.transport.model.Utilisateur;
import com.transport.service.UserService;
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
import java.util.List; // IMPORT AJOUTÉ ICI
import java.util.ArrayList; // Peut être utile

public class GestionUsersView {

    private Stage stage;
    private Utilisateur currentAdmin;
    private TableView<Utilisateur> table;
    private ObservableList<Utilisateur> users = FXCollections.observableArrayList();
    private UserService userService;
    private ComboBox<String> roleFilter;

    public GestionUsersView(Stage stage, Utilisateur admin) {
        this.stage = stage;
        this.currentAdmin = admin;
        this.userService = new UserService();
        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        try {
            users.clear();
            List<Utilisateur> dbUsers = userService.getAllUsers();
            if (dbUsers != null && !dbUsers.isEmpty()) {
                users.addAll(dbUsers);
                System.out.println("✅ " + users.size() + " utilisateurs chargés depuis la BD");
                // Debug: afficher les utilisateurs chargés
                for (Utilisateur user : users) {
                    System.out.println("  - " + user.getNom() + " (" + user.getEmail() + ") - " + user.getRole());
                }
            } else {
                System.out.println("⚠️ Aucun utilisateur trouvé dans la base de données");
            }
        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement depuis la BD : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void show() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dashboard-root");

        // Header
        HBox header = createHeader("👥 Gestion des Utilisateurs");
        root.setTop(header);

        // Toolbar
        HBox toolbar = createToolbar();
        root.setTop(new VBox(header, toolbar));

        // Table
        table = createUsersTable();
        root.setCenter(table);

        // Footer/Stats
        HBox footer = createFooter();
        root.setBottom(footer);

        Scene scene = new Scene(root, 1100, 650);
        loadCSS(scene);
        stage.setScene(scene);
        stage.setTitle("Gestion des Utilisateurs - Admin Dashboard");
        stage.centerOnScreen();
        stage.show();
    }

    private void loadCSS(Scene scene) {
        try {
            String cssPath = getClass().getResource("/com/transport/ressources/style.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (NullPointerException e) {
            System.out.println("CSS non trouvé pour GestionUsersView");
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

        Label countLabel = new Label(users.size() + " utilisateurs");
        countLabel.setStyle("-fx-text-fill: #3498db; -fx-background-color: white; -fx-background-radius: 10; -fx-padding: 5 15; -fx-font-weight: bold;");

        header.getChildren().addAll(backBtn, titleLabel, spacer, countLabel);
        return header;
    }

    private HBox createToolbar() {
        HBox toolbar = new HBox(10);
        toolbar.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15 25; -fx-border-color: #dee2e6; -fx-border-width: 0 0 1 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // Bouton Ajouter
        Button addBtn = new Button("➕ Ajouter Utilisateur");
        addBtn.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5; -fx-padding: 10 20;");
        addBtn.setOnAction(e -> showAddUserDialog());

        // Champ recherche
        TextField searchField = new TextField();
        searchField.setPromptText("Rechercher un utilisateur...");
        searchField.setStyle("-fx-background-radius: 5; -fx-border-color: #dee2e6; -fx-border-radius: 5; -fx-padding: 8 15;");
        searchField.setPrefWidth(300);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filterUsers(newValue, roleFilter.getValue());
        });

        Button searchBtn = new Button("🔍");
        searchBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 8 15;");
        searchBtn.setOnAction(e -> filterUsers(searchField.getText(), roleFilter.getValue()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Filtres
        roleFilter = new ComboBox<>();
        roleFilter.getItems().addAll("Tous", "ADMIN", "CLIENT", "CHAUFFEUR");
        roleFilter.setValue("Tous");
        roleFilter.setStyle("-fx-background-radius: 5;");
        roleFilter.setOnAction(e -> filterUsers(searchField.getText(), roleFilter.getValue()));

        toolbar.getChildren().addAll(addBtn, spacer, searchField, searchBtn, new Label("Filtrer par rôle:"), roleFilter);
        return toolbar;
    }

    private void filterUsers(String searchText, String roleFilterValue) {
        ObservableList<Utilisateur> filteredUsers = FXCollections.observableArrayList();
        
        for (Utilisateur user : users) {
            boolean matchesSearch = searchText.isEmpty() ||
                    user.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                    user.getEmail().toLowerCase().contains(searchText.toLowerCase());
                    
            boolean matchesRole = roleFilterValue.equals("Tous") || 
                    user.getRole().equals(roleFilterValue);
            
            if (matchesSearch && matchesRole) {
                filteredUsers.add(user);
            }
        }
        
        table.setItems(filteredUsers);
        updateFooterStats(filteredUsers);
    }

    private void updateFooterStats(ObservableList<Utilisateur> userList) {
        long admins = userList.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long clients = userList.stream().filter(u -> "CLIENT".equals(u.getRole())).count();
        long chauffeurs = userList.stream().filter(u -> "CHAUFFEUR".equals(u.getRole())).count();
        
        HBox footer = (HBox) stage.getScene().getRoot().getBottom();
        if (footer != null && !footer.getChildren().isEmpty()) {
            Label statsLabel = (Label) footer.getChildren().get(0);
            statsLabel.setText("Total: " + userList.size() + " utilisateurs • " + 
                              admins + " admins • " + 
                              clients + " clients • " + 
                              chauffeurs + " chauffeurs");
        }
    }

    private TableView<Utilisateur> createUsersTable() {
        TableView<Utilisateur> tableView = new TableView<>();
        tableView.setItems(users);
        tableView.setStyle("-fx-background-color: transparent;");

        // Colonne ID
        TableColumn<Utilisateur, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(80);

        // Colonne Nom
        TableColumn<Utilisateur, String> nomCol = new TableColumn<>("Nom");
        nomCol.setCellValueFactory(new PropertyValueFactory<>("nom"));
        nomCol.setPrefWidth(150);

        // Colonne Email
        TableColumn<Utilisateur, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        // Colonne Rôle
        TableColumn<Utilisateur, String> roleCol = new TableColumn<>("Rôle");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleCol.setPrefWidth(120);
        roleCol.setCellFactory(column -> new TableCell<Utilisateur, String>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(role);
                    switch (role) {
                        case "ADMIN" -> setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 3 8;");
                        case "CLIENT" -> setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 3 8;");
                        case "CHAUFFEUR" -> setStyle("-fx-background-color: #e67e22; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 10; -fx-padding: 3 8;");
                        default -> setStyle("");
                    }
                }
            }
        });

        // Colonne Actions
        TableColumn<Utilisateur, Void> actionsCol = new TableColumn<>("Actions");
        actionsCol.setPrefWidth(200);
        actionsCol.setCellFactory(new Callback<>() {
            @Override
            public TableCell<Utilisateur, Void> call(TableColumn<Utilisateur, Void> param) {
                return new TableCell<>() {
                    private final HBox container = new HBox(5);

                    {
                    	// Remplacez les boutons comme suit :
                    	Button editBtn = new Button("✎");
                    	editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-weight: bold;");
                    	editBtn.setOnAction(e -> editUser(getTableView().getItems().get(getIndex())));

                    	Button deleteBtn = new Button("×");
                    	deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 5; -fx-padding: 5 10; -fx-font-weight: bold;");
                    	deleteBtn.setOnAction(e -> deleteUser(getTableView().getItems().get(getIndex())));
                    	
                        container.getChildren().addAll(editBtn, deleteBtn);
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

        tableView.getColumns().addAll(idCol, nomCol, emailCol, roleCol, actionsCol);
        return tableView;
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15 25; -fx-border-color: #dee2e6; -fx-border-width: 1 0 0 0;");
        footer.setAlignment(Pos.CENTER_LEFT);

        long admins = users.stream().filter(u -> "ADMIN".equals(u.getRole())).count();
        long clients = users.stream().filter(u -> "CLIENT".equals(u.getRole())).count();
        long chauffeurs = users.stream().filter(u -> "CHAUFFEUR".equals(u.getRole())).count();
        
        Label stats = new Label("Total: " + users.size() + " utilisateurs • " + 
                               admins + " admins • " + 
                               clients + " clients • " + 
                               chauffeurs + " chauffeurs");
        stats.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 13px;");

        footer.getChildren().add(stats);
        return footer;
    }

    private void showAddUserDialog() {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText("Remplissez les informations du nouvel utilisateur");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "CLIENT", "CHAUFFEUR");

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Mot de passe:"), 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(new Label("Rôle:"), 0, 3);
        grid.add(roleCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (nomField.getText().isEmpty() || emailField.getText().isEmpty() || 
                    passwordField.getText().isEmpty() || roleCombo.getValue() == null) {
                    showAlert("Erreur", "Veuillez remplir tous les champs");
                    return null;
                }
                
                if (userService.emailExists(emailField.getText())) {
                    showAlert("Erreur", "Cet email est déjà utilisé");
                    return null;
                }
                
                Utilisateur newUser = new Utilisateur(
                    0,
                    nomField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    roleCombo.getValue()
                );
                
                if (userService.addUser(newUser)) {
                    loadUsersFromDatabase();
                    table.refresh();
                    showAlert("Succès", "Utilisateur ajouté avec succès !");
                } else {
                    showAlert("Erreur", "Erreur lors de l'ajout à la base de données");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void editUser(Utilisateur user) {
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Modifier un utilisateur");
        dialog.setHeaderText("Modifier " + user.getNom());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nomField = new TextField(user.getNom());
        TextField emailField = new TextField(user.getEmail());
        ComboBox<String> roleCombo = new ComboBox<>();
        roleCombo.getItems().addAll("ADMIN", "CLIENT", "CHAUFFEUR");
        roleCombo.setValue(user.getRole());

        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nomField, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(new Label("Rôle:"), 0, 2);
        grid.add(roleCombo, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType saveButtonType = new ButtonType("Sauvegarder", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (nomField.getText().isEmpty() || emailField.getText().isEmpty() || 
                    roleCombo.getValue() == null) {
                    showAlert("Erreur", "Veuillez remplir tous les champs");
                    return null;
                }
                
                if (!user.getEmail().equals(emailField.getText()) && 
                    userService.emailExists(emailField.getText())) {
                    showAlert("Erreur", "Cet email est déjà utilisé");
                    return null;
                }
                
                user.setNom(nomField.getText());
                user.setEmail(emailField.getText());
                user.setRole(roleCombo.getValue());
                
                if (userService.updateUser(user)) {
                    loadUsersFromDatabase();
                    table.refresh();
                    showAlert("Succès", "Utilisateur modifié avec succès !");
                }
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void deleteUser(Utilisateur user) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer l'utilisateur");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer " + user.getNom() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (userService.deleteUser(user.getId())) {
                    users.remove(user);
                    table.refresh();
                    showAlert("Succès", "Utilisateur supprimé avec succès !");
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression");
                }
            }
        });
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void goBack() {
        if (userService != null) {
            userService.close();
        }
        new com.transport.ui.dashboard.AdminDashboard(stage, currentAdmin).show();
    }
}