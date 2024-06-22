package screensframework;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;  // Importación agregada
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TableColumn;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import javax.swing.JOptionPane;
import screensframework.DBConnect.DBConnection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.control.Label;

public class ProductoController implements Initializable, ControlledScreen {
    
    ScreensController controlador;
    private ControlesBasicos controlesBasicos = new ControlesBasicos();
    @FXML private Button btAddProducto;
    @FXML private Button btModificarProducto;
    @FXML private Button btEliminarProducto;
    @FXML private Button btNuevoProducto;
    
    @FXML private TextField tfNombreProducto;
    @FXML private TextField tfPrecioProducto;
    @FXML private TextField tfBuscarProducto;
    @FXML private ComboBox<String> cbCategoriaProducto; // Parametrizado
    @FXML private ComboBox<String> cbMarcaProducto; // Parametrizado
    @FXML private Label lbCodigoProducto;
    
    @FXML private TableView<ObservableList<String>> tablaProducto; // Parametrizado
    @FXML private TableColumn<ObservableList<String>, String> col; // Parametrizado
    private Connection conexion;
    
    ObservableList<ObservableList<String>> producto; // Parametrizado
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.cargarDatosTabla();
        
        btEliminarProducto.setDisable(true);
        btModificarProducto.setDisable(true);
        btEliminarProducto.setStyle("-fx-background-color:grey");
        btModificarProducto.setStyle("-fx-background-color:grey");
        
        ObservableList<Object> categoriaID = FXCollections.observableArrayList();
        ObservableList<Object> categoriaNomnre = FXCollections.observableArrayList();
        ObservableList<Object> subCategoria = FXCollections.observableArrayList();
        ObservableList<Object> marcas = FXCollections.observableArrayList();
        
        try {
            conexion = DBConnection.connect();
            
            // COMBOBOX DE CATEGORIA
            String sqlCategoria = "SELECT idcategoria, nombre_categoria FROM categoria"; // Corregido
            ResultSet resultadoCategoria = conexion.createStatement().executeQuery(sqlCategoria);
            while(resultadoCategoria.next()) {
                cbCategoriaProducto.getItems().add(resultadoCategoria.getString("nombre_categoria"));
            }
            
            // COMBOBOX DE MARCAS
            String sqlMarcas = "SELECT idmarca, nombre_marca FROM marca";
            ResultSet resultadoMarcas = conexion.createStatement().executeQuery(sqlMarcas);
            while(resultadoMarcas.next()) {
                cbMarcaProducto.getItems().add(resultadoMarcas.getString("nombre_marca"));
            }
            
            resultadoCategoria.close();
            resultadoMarcas.close();
        } catch (SQLException e) {
            System.out.println("Error " + e);
        }
    }
    
    @Override
    public void setScreenParent(ScreensController pantallaPadre) {
        controlador = pantallaPadre;
    }
    
    public void cargarDatosTabla() {
        producto = FXCollections.observableArrayList();
        
        try {
            conexion = DBConnection.connect();
            //SQL FOR SELECTING ALL OF CUSTOMER
            String sql = "SELECT p.idproducto, "
                    + " p.nombre_producto, "
                    + " p.precio, "
                    + " c.nombre_categoria AS nom_categoria, "
                    + " m.nombre_marca AS nom_marca "
                    + " FROM producto AS p, "
                    + " categoria AS c, "
                    + " marca AS m "
                    + " WHERE p.idcategoria = c.idcategoria AND "
                    + " p.idmarca = m.idmarca "
                    + " ORDER BY p.idproducto DESC";
            //ResultSet
            ResultSet rs = conexion.createStatement().executeQuery(sql);
            // Títulos de las columnas
            String[] titulos = {
                    "Codigo",
                    "Nombre",
                    "Precio",
                    "Categoria",
                    "Marca"
            };
            /**********************************
             * TABLE COLUMN ADDED DYNAMICALLY *
             **********************************/
            
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++ ) {
                final int j = i;
                col = new TableColumn<>(titulos[i]);
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>(){                   
                    public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> parametro) {                                                                                             
                        return new SimpleStringProperty(parametro.getValue().get(j));                       
                    }                   
                });
                tablaProducto.getColumns().addAll(col);
                // Asignamos un tamaño a las columnas
                col.setMinWidth(100);
                System.out.println("Column ["+i+"] ");
                // Centrar los datos de la tabla
                col.setCellFactory(new Callback<TableColumn<ObservableList<String>, String>, TableCell<ObservableList<String>, String>>(){
                    @Override
                    public TableCell<ObservableList<String>, String> call(TableColumn<ObservableList<String>, String> p) {
                        return new TableCell<ObservableList<String>, String>(){
                            @Override
                            protected void updateItem(String t, boolean bln) {
                                if(t != null){
                                    super.updateItem(t, bln);
                                    System.out.println(t);
                                    setText(t);
                                    setAlignment(Pos.CENTER); //Setting the Alignment
                                }
                            }
                        };
                    }
                });
            }
            /********************************
             * Cargamos de la base de datos *
             ********************************/
            while(rs.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i = 1 ; i <= rs.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added "+row );
                producto.add(row);
            }
            //FINALLY ADDED TO TableView
            tablaProducto.setItems(producto);
            rs.close();
        }catch(SQLException e){
            System.out.println("Error "+e);            
        }
    }
    
    public void cargarProductosText(String valor) {
        
        try {
            conexion = DBConnection.connect();
            String sql = "SELECT p.*, c.*, m.* "
                    + " FROM producto AS p, categoria AS c, marca AS m "
                    + " WHERE idproducto = "+valor+" AND "
                    + " p.idcategoria = c.idcategoria AND "
                    + " p.idmarca = m.idmarca";
            ResultSet rs = conexion.createStatement().executeQuery(sql);
            
            while (rs.next()) {
                lbCodigoProducto.setText(rs.getString("idproducto"));
                tfNombreProducto.setText(rs.getString("nombre_producto"));
                tfPrecioProducto.setText(rs.getString("precio"));
                cbCategoriaProducto.setValue(rs.getString("nombre_categoria"));
                cbMarcaProducto.setValue(rs.getString("nombre_marca"));
            }
            rs.close();
        } catch (SQLException ex) {
            System.out.println("Error "+ex);
        }
        
    }
    
    @FXML
    private void getProductoSeleccionado(MouseEvent event) {
        tablaProducto.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e) {
                if (tablaProducto != null) {
                    
                    btAddProducto.setDisable(true);
                    btEliminarProducto.setDisable(false);
                    btModificarProducto.setDisable(false);
                    btAddProducto.setStyle("-fx-background-color:grey");
                    btEliminarProducto.setStyle("-fx-background-color:#66CCCC");
                    btModificarProducto.setStyle("-fx-background-color:#66CCCC");
                    
                    String valor = tablaProducto.getSelectionModel().getSelectedItems().get(0).toString();
                    
                    String cincoDigitos = valor.substring(1, 6);
                    String cuatroDigitos = valor.substring(1, 5);
                    String tresDigitos = valor.substring(1, 4);
                    String dosDigitos = valor.substring(1, 3);
                    String unDigitos = valor.substring(1, 2);
                    
                    Pattern p = Pattern.compile("^[0-9]*$");
                    
                    Matcher m5 = p.matcher(cincoDigitos);
                    Matcher m4 = p.matcher(cuatroDigitos);
                    Matcher m3 = p.matcher(tresDigitos);
                    Matcher m2 = p.matcher(dosDigitos);
                    
                    if (m5.find()) {
                        cargarProductosText(cincoDigitos);
                    } else {
                        if (m4.find()) {
                            cargarProductosText(cuatroDigitos);
                        } else {
                            if (m3.find()) {
                                cargarProductosText(tresDigitos);
                            } else {
                                if (m2.find()) {
                                    cargarProductosText(dosDigitos);
                                } else {
                                    cargarProductosText(unDigitos);
                                }
                             }
                        }
                    }
                }
            }
        });
    }
    
    @FXML
    private void addProducto(ActionEvent event) {
        int indiceCategoria = cbCategoriaProducto.getSelectionModel().getSelectedIndex() + 1;
        int indiceMarca = cbMarcaProducto.getSelectionModel().getSelectedIndex() + 1;
        
        try {
            conexion = DBConnection.connect();
            String sql = "INSERT INTO producto (idcategoria, idmarca, nombre_producto, precio) "
                    + "VALUES (?,?,?,?)";
            PreparedStatement prest = conexion.prepareStatement(sql);
            prest.setInt(1, indiceCategoria);
            prest.setInt(2, indiceMarca);
            prest.setString(3, tfNombreProducto.getText());
            prest.setString(4, tfPrecioProducto.getText());
            prest.executeUpdate();
            prest.close();
            
            tfNombreProducto.setText("");
            tfPrecioProducto.setText("");
            cbCategoriaProducto.setValue("");
            cbMarcaProducto.setValue("");
            lbCodigoProducto.setText("...");
            
            producto.clear();
            tablaProducto.getColumns().clear();
            cargarDatosTabla();
            
            JOptionPane.showMessageDialog(null, "Producto Guardado Correctamente");
            
        } catch (SQLException ex) {
            System.out.println("Error "+ex);
        }
        
    }
    
    @FXML
    private void modificarProducto(ActionEvent event) {
        int indiceCategoria = cbCategoriaProducto.getSelectionModel().getSelectedIndex() + 1;
        int indiceMarca = cbMarcaProducto.getSelectionModel().getSelectedIndex() + 1;
        
        try {
            conexion = DBConnection.connect();
            String sql = "UPDATE producto SET "
                    + "idcategoria = ?, "
                    + "idmarca = ?, "
                    + "nombre_producto = ?, "
                    + "precio = ? "
                    + "WHERE idproducto = "+lbCodigoProducto.getText()+"";
            PreparedStatement prest = conexion.prepareStatement(sql);
            prest.setInt(1, indiceCategoria);
            prest.setInt(2, indiceMarca);
            prest.setString(3, tfNombreProducto.getText());
            prest.setString(4, tfPrecioProducto.getText());
            prest.executeUpdate();
            prest.close();
            
            tfNombreProducto.setText("");
            tfPrecioProducto.setText("");
            cbCategoriaProducto.setValue("");
            cbMarcaProducto.setValue("");
            lbCodigoProducto.setText("...");
            
            producto.clear();
            tablaProducto.getColumns().clear();
            cargarDatosTabla();
            
            JOptionPane.showMessageDialog(null, "Producto Modificado Correctamente");
            
            btAddProducto.setDisable(false);
            btEliminarProducto.setDisable(true);
            btModificarProducto.setDisable(true);
            btAddProducto.setStyle("-fx-background-color:#66CCCC");
            btEliminarProducto.setStyle("-fx-background-color:grey");
            btModificarProducto.setStyle("-fx-background-color:grey");
            
        } catch (SQLException ex) {
            System.out.println("Error "+ex);
        }
        
    }
    
    @FXML
    private void eliminarProducto(ActionEvent event) {
        
        try {
            conexion = DBConnection.connect();
            String sql = "DELETE FROM producto "
                    + "WHERE idproducto = "+lbCodigoProducto.getText()+"";
            PreparedStatement prest = conexion.prepareStatement(sql);
            prest.executeUpdate();
            prest.close();
            
            tfNombreProducto.setText("");
            tfPrecioProducto.setText("");
            cbCategoriaProducto.setValue("");
            cbMarcaProducto.setValue("");
            lbCodigoProducto.setText("...");
            
            producto.clear();
            tablaProducto.getColumns().clear();
            cargarDatosTabla();
            
            JOptionPane.showMessageDialog(null, "Producto Eliminado Correctamente");
            
            btAddProducto.setDisable(false);
            btEliminarProducto.setDisable(true);
            btModificarProducto.setDisable(true);
            btAddProducto.setStyle("-fx-background-color:#66CCCC");
            btEliminarProducto.setStyle("-fx-background-color:grey");
            btModificarProducto.setStyle("-fx-background-color:grey");
            
        } catch (SQLException ex) {
            System.out.println("Error "+ex);
        }
        
    }
    
    @FXML
    private void nuevoProducto(ActionEvent event) {
        tfNombreProducto.setText("");
        tfPrecioProducto.setText("");
        cbCategoriaProducto.setValue("");
        cbMarcaProducto.setValue("");
        lbCodigoProducto.setText("...");
        
        btAddProducto.setDisable(false);
        btEliminarProducto.setDisable(true);
        btModificarProducto.setDisable(true);
        btAddProducto.setStyle("-fx-background-color:#66CCCC");
        btEliminarProducto.setStyle("-fx-background-color:grey");
        btModificarProducto.setStyle("-fx-background-color:grey");
    }
    
    @FXML
    private void buscarProducto(ActionEvent event) {
        String buscar = tfBuscarProducto.getText();
        producto.clear();
        tablaProducto.getColumns().clear();
        try {
            conexion = DBConnection.connect();
            String sql = "SELECT p.idproducto, "
                    + " p.nombre_producto, "
                    + " p.precio, "
                    + " c.nombre_categoria AS nom_categoria, "
                    + " m.nombre_marca AS nom_marca "
                    + " FROM producto AS p, "
                    + " categoria AS c, "
                    + " marca AS m "
                    + " WHERE p.idcategoria = c.idcategoria AND "
                    + " p.idmarca = m.idmarca AND "
                    + " (p.nombre_producto LIKE '%"+buscar+"%' OR "
                    + " p.precio LIKE '%"+buscar+"%' OR "
                    + " c.nombre_categoria LIKE '%"+buscar+"%' OR "
                    + " m.nombre_marca LIKE '%"+buscar+"%')";
            //ResultSet
            ResultSet rs = conexion.createStatement().executeQuery(sql);
            // Títulos de las columnas
            String[] titulos = {
                    "Codigo",
                    "Nombre",
                    "Precio",
                    "Categoria",
                    "Marca"
            };
            /**********************************
             * TABLE COLUMN ADDED DYNAMICALLY *
             **********************************/
            
            for (int i = 0; i < rs.getMetaData().getColumnCount(); i++ ) {
                final int j = i;
                col = new TableColumn<>(titulos[i]);
                col.setCellValueFactory(new Callback<CellDataFeatures<ObservableList<String>, String>, ObservableValue<String>>(){                   
                    public ObservableValue<String> call(CellDataFeatures<ObservableList<String>, String> parametro) {                                                                                             
                        return new SimpleStringProperty(parametro.getValue().get(j));                       
                    }                   
                });
                tablaProducto.getColumns().addAll(col);
                // Asignamos un tamaño a las columnas
                col.setMinWidth(100);
                System.out.println("Column ["+i+"] ");
                // Centrar los datos de la tabla
                col.setCellFactory(new Callback<TableColumn<ObservableList<String>, String>, TableCell<ObservableList<String>, String>>(){
                    @Override
                    public TableCell<ObservableList<String>, String> call(TableColumn<ObservableList<String>, String> p) {
                        return new TableCell<ObservableList<String>, String>(){
                            @Override
                            protected void updateItem(String t, boolean bln) {
                                if(t != null){
                                    super.updateItem(t, bln);
                                    System.out.println(t);
                                    setText(t);
                                    setAlignment(Pos.CENTER); //Setting the Alignment
                                }
                            }
                        };
                    }
                });
            }
            /********************************
             * Cargamos de la base de datos *
             ********************************/
            while(rs.next()){
                //Iterate Row
                ObservableList<String> row = FXCollections.observableArrayList();
                for(int i = 1 ; i <= rs.getMetaData().getColumnCount(); i++){
                    //Iterate Column
                    row.add(rs.getString(i));
                }
                System.out.println("Row [1] added "+row );
                producto.add(row);
            }
            //FINALLY ADDED TO TableView
            tablaProducto.setItems(producto);
            rs.close();
        }catch(SQLException e){
            System.out.println("Error "+e);            
        }
    }
}
