package controllers;


import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import models.Category;
import services.CategoryServices;

public class AfficherCat {

    @FXML
    private ResourceBundle resources;

    @FXML
    private URL location;

    @FXML
    private TextField RechercherCategorie;

    @FXML
    private TableView<Category> TableViewCategory;

    @FXML
    private TableColumn<Category, Void> actionColumnCategorie;

    @FXML
    private TableColumn<Category, String> typeColumnCategorie;
    @FXML
    private ImageView imageview;

    @FXML
    private ImageView imageview2;

    private final CategoryServices cs = new CategoryServices();

    @FXML
    void initialize() {
        typeColumnCategorie.setCellValueFactory(new PropertyValueFactory<>("type"));
        setupActionColumn();
        refreshCategoryList();
        // Ajouter un ChangeListener au champ de texte Rechercher
        RechercherCategorie.textProperty().addListener((observable, oldValue, newValue) -> {
            filterCategoryList(newValue);
        });
    }
    private void filterCategoryList(String keyword) {
        try {
            List<Category> categories = cs.rechercher(keyword);
            ObservableList<Category> filteredList = FXCollections.observableArrayList(categories);
            TableViewCategory.setItems(filteredList);
        } catch (SQLException e) {
            e.printStackTrace(); // Gérer cette exception correctement
        }
    }

    private void refreshCategoryList() {
        try {
            List<Category> categories = cs.afficher();
            ObservableList<Category> categoryList = FXCollections.observableArrayList(categories);
            TableViewCategory.setItems(categoryList);
        } catch (SQLException e) {
            e.printStackTrace(); // Handle this exception properly
        }
    }
    private void setupActionColumn() {
        actionColumnCategorie.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modify");
            private final Button deleteButton = new Button("Delete");
            private final HBox pane = new HBox(editButton, deleteButton);

            {
                editButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    modifyCategory(category);
                });
                deleteButton.setOnAction(event -> {
                    Category category = getTableView().getItems().get(getIndex());
                    deleteCategory(category);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }
    private void modifyCategory(Category category) {
        TextInputDialog dialog = new TextInputDialog(category.getType());
        dialog.setTitle("Modify Category");
        dialog.setHeaderText("Modify Category Type");
        dialog.setContentText("Enter new type:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newType -> {
            category.setType(newType);
            try {
                cs.modifier(category);
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category modified successfully.");
                refreshCategoryList();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error modifying category.");
            }
        });
    }
    //***************************************************************************************************************************
    private void deleteCategory(Category category) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText("Delete Category");
        alert.setContentText("Are you sure you want to delete this category?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                cs.supprimer(category.getId()); // Supposer que getId() retourne l'identifiant de la catégorie
                showAlert(Alert.AlertType.INFORMATION, "Success", "Category deleted successfully.");
                refreshCategoryList();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Error deleting category.");
            }
        }
    }
    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
