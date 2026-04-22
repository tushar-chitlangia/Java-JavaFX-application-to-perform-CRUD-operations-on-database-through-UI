import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.sql.*;

public class RestaurantMenuApp extends Application {

    private TextField rIdField = new TextField();
    private TextField rNameField = new TextField();
    private TextField rAddressField = new TextField();

    private TextField mIdField = new TextField();
    private TextField mNameField = new TextField();
    private TextField mPriceField = new TextField();
    private TextField mResIdField = new TextField();

    private TableView<Restaurant> restaurantTable = new TableView<>();
    private TableView<MenuItem> menuItemTable = new TableView<>();

    private ObservableList<Restaurant> restaurantList = FXCollections.observableArrayList();
    private ObservableList<MenuItem> menuItemList = FXCollections.observableArrayList();

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        Tab restaurantTab = new Tab("Restaurant CRUD");
        restaurantTab.setClosable(false);
        restaurantTab.setContent(createRestaurantPane());

        Tab menuItemTab = new Tab("MenuItem CRUD");
        menuItemTab.setClosable(false);
        menuItemTab.setContent(createMenuItemPane());

        tabPane.getTabs().addAll(restaurantTab, menuItemTab);

        Scene scene = new Scene(tabPane, 900, 600);
        stage.setTitle("Restaurant and MenuItem CRUD - JavaFX + JDBC");
        stage.setScene(scene);
        stage.show();

        loadRestaurants();
        loadMenuItems();
    }

    private Pane createRestaurantPane() {
        Label title = new Label("Restaurant CRUD Operations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        rIdField.setPromptText("Enter Restaurant ID");
        rNameField.setPromptText("Enter Restaurant Name");
        rAddressField.setPromptText("Enter Address");

        Button insertBtn = new Button("Insert");
        Button selectBtn = new Button("Select All");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");

        insertBtn.setOnAction(e -> insertRestaurant());
        selectBtn.setOnAction(e -> loadRestaurants());
        updateBtn.setOnAction(e -> updateRestaurant());
        deleteBtn.setOnAction(e -> deleteRestaurant());
        clearBtn.setOnAction(e -> clearRestaurantFields());

        HBox form = new HBox(10, rIdField, rNameField, rAddressField);
        HBox buttons = new HBox(10, insertBtn, selectBtn, updateBtn, deleteBtn, clearBtn);

        TableColumn<Restaurant, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Restaurant, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<Restaurant, String> addressCol = new TableColumn<>("Address");
        addressCol.setCellValueFactory(new PropertyValueFactory<>("address"));

        restaurantTable.getColumns().clear();
        restaurantTable.getColumns().addAll(idCol, nameCol, addressCol);
        restaurantTable.setItems(restaurantList);
        restaurantTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        restaurantTable.setOnMouseClicked(e -> {
            Restaurant r = restaurantTable.getSelectionModel().getSelectedItem();
            if (r != null) {
                rIdField.setText(String.valueOf(r.getId()));
                rNameField.setText(r.getName());
                rAddressField.setText(r.getAddress());
            }
        });

        VBox layout = new VBox(15, title, form, buttons, restaurantTable);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private Pane createMenuItemPane() {
        Label title = new Label("MenuItem CRUD Operations");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        mIdField.setPromptText("Enter MenuItem ID");
        mNameField.setPromptText("Enter Item Name");
        mPriceField.setPromptText("Enter Price");
        mResIdField.setPromptText("Enter Restaurant ID");

        Button insertBtn = new Button("Insert");
        Button selectBtn = new Button("Select All");
        Button updateBtn = new Button("Update");
        Button deleteBtn = new Button("Delete");
        Button clearBtn = new Button("Clear");

        insertBtn.setOnAction(e -> insertMenuItem());
        selectBtn.setOnAction(e -> loadMenuItems());
        updateBtn.setOnAction(e -> updateMenuItem());
        deleteBtn.setOnAction(e -> deleteMenuItem());
        clearBtn.setOnAction(e -> clearMenuFields());

        HBox form = new HBox(10, mIdField, mNameField, mPriceField, mResIdField);
        HBox buttons = new HBox(10, insertBtn, selectBtn, updateBtn, deleteBtn, clearBtn);

        TableColumn<MenuItem, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<MenuItem, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<MenuItem, Double> priceCol = new TableColumn<>("Price");
        priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        TableColumn<MenuItem, Integer> resIdCol = new TableColumn<>("ResId");
        resIdCol.setCellValueFactory(new PropertyValueFactory<>("resId"));

        menuItemTable.getColumns().clear();
        menuItemTable.getColumns().addAll(idCol, nameCol, priceCol, resIdCol);
        menuItemTable.setItems(menuItemList);
        menuItemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        menuItemTable.setOnMouseClicked(e -> {
            MenuItem m = menuItemTable.getSelectionModel().getSelectedItem();
            if (m != null) {
                mIdField.setText(String.valueOf(m.getId()));
                mNameField.setText(m.getName());
                mPriceField.setText(String.valueOf(m.getPrice()));
                mResIdField.setText(String.valueOf(m.getResId()));
            }
        });

        VBox layout = new VBox(15, title, form, buttons, menuItemTable);
        layout.setPadding(new Insets(20));
        return layout;
    }

    private void insertRestaurant() {
        String sql = "INSERT INTO Restaurant (Id, Name, Address) VALUES (?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, Integer.parseInt(rIdField.getText()));
            pst.setString(2, rNameField.getText());
            pst.setString(3, rAddressField.getText());

            pst.executeUpdate();
            showAlert("Success", "Restaurant inserted successfully.");
            loadRestaurants();
            clearRestaurantFields();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void loadRestaurants() {
        restaurantList.clear();
        String sql = "SELECT * FROM Restaurant";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                restaurantList.add(new Restaurant(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getString("Address")
                ));
            }

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void updateRestaurant() {
        String sql = "UPDATE Restaurant SET Name = ?, Address = ? WHERE Id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, rNameField.getText());
            pst.setString(2, rAddressField.getText());
            pst.setInt(3, Integer.parseInt(rIdField.getText()));

            int rows = pst.executeUpdate();
            if (rows > 0) {
                showAlert("Success", "Restaurant updated successfully.");
            } else {
                showAlert("Info", "Restaurant ID not found.");
            }

            loadRestaurants();
            clearRestaurantFields();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void deleteRestaurant() {
        String sql = "DELETE FROM Restaurant WHERE Id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, Integer.parseInt(rIdField.getText()));
            int rows = pst.executeUpdate();

            if (rows > 0) {
                showAlert("Success", "Restaurant deleted successfully.");
            } else {
                showAlert("Info", "Restaurant ID not found.");
            }

            loadRestaurants();
            clearRestaurantFields();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void clearRestaurantFields() {
        rIdField.clear();
        rNameField.clear();
        rAddressField.clear();
    }

    private void insertMenuItem() {
        String sql = "INSERT INTO MenuItem (Id, Name, Price, ResId) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, Integer.parseInt(mIdField.getText()));
            pst.setString(2, mNameField.getText());
            pst.setDouble(3, Double.parseDouble(mPriceField.getText()));
            pst.setInt(4, Integer.parseInt(mResIdField.getText()));

            pst.executeUpdate();
            showAlert("Success", "MenuItem inserted successfully.");
            loadMenuItems();
            clearMenuFields();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void loadMenuItems() {
        menuItemList.clear();
        String sql = "SELECT * FROM MenuItem";

        try (Connection con = DBConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                menuItemList.add(new MenuItem(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getDouble("Price"),
                        rs.getInt("ResId")
                ));
            }

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void updateMenuItem() {
        String sql = "UPDATE MenuItem SET Name = ?, Price = ?, ResId = ? WHERE Id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setString(1, mNameField.getText());
            pst.setDouble(2, Double.parseDouble(mPriceField.getText()));
            pst.setInt(3, Integer.parseInt(mResIdField.getText()));
            pst.setInt(4, Integer.parseInt(mIdField.getText()));

            int rows = pst.executeUpdate();
            if (rows > 0) {
                showAlert("Success", "MenuItem updated successfully.");
            } else {
                showAlert("Info", "MenuItem ID not found.");
            }

            loadMenuItems();
            clearMenuFields();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void deleteMenuItem() {
        String sql = "DELETE FROM MenuItem WHERE Id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {

            pst.setInt(1, Integer.parseInt(mIdField.getText()));
            int rows = pst.executeUpdate();

            if (rows > 0) {
                showAlert("Success", "MenuItem deleted successfully.");
            } else {
                showAlert("Info", "MenuItem ID not found.");
            }

            loadMenuItems();
            clearMenuFields();

        } catch (Exception e) {
            showAlert("Error", e.getMessage());
        }
    }

    private void clearMenuFields() {
        mIdField.clear();
        mNameField.clear();
        mPriceField.clear();
        mResIdField.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}