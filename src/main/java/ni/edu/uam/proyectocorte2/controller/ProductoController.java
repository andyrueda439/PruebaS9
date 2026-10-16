package ni.edu.uam.proyectocorte2.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import ni.edu.uam.proyectocorte2.dao.ProductoDAO;
import ni.edu.uam.proyectocorte2.model.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class ProductoController {

    private static final String TODAS = "Todas";

    @FXML private TextField txtId;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cmbCategoria;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtStock;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbFiltroCategoria;
    @FXML private CheckBox chkSoloExistencias;
    @FXML private Label lblContador;

    @FXML private TableView<Producto> tblProductos;
    @FXML private TableColumn<Producto, Integer> colId;
    @FXML private TableColumn<Producto, String> colCodigo;
    @FXML private TableColumn<Producto, String> colNombre;
    @FXML private TableColumn<Producto, String> colCategoria;
    @FXML private TableColumn<Producto, BigDecimal> colPrecio;
    @FXML private TableColumn<Producto, Integer> colStock;

    private final ProductoDAO productoDAO = new ProductoDAO();
    private final ObservableList<Producto> productos = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        cmbCategoria.getItems().addAll("Papelería", "Tecnología", "Accesorios");

        cmbFiltroCategoria.getItems().addAll(TODAS, "Papelería", "Tecnología", "Accesorios");
        cmbFiltroCategoria.setValue(TODAS);

        colId.setCellValueFactory(new PropertyValueFactory<>("idProducto"));
        colCodigo.setCellValueFactory(new PropertyValueFactory<>("codigo"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCategoria.setCellValueFactory(new PropertyValueFactory<>("categoria"));
        colPrecio.setCellValueFactory(new PropertyValueFactory<>("precio"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));

        tblProductos.setItems(productos);

        tblProductos.getSelectionModel().selectedItemProperty().addListener(
                (obs, anterior, seleccionado) -> {
                    if (seleccionado != null) {
                        llenarFormulario(seleccionado);
                    }
                });

        cargarProductos();
    }

    private boolean cargarProductos() {
        try {
            List<Producto> resultado = productoDAO.buscar(
                    txtBuscar.getText(),
                    categoriaFiltro(),
                    chkSoloExistencias.isSelected()
            );

            productos.clear();
            productos.addAll(resultado);
            lblContador.setText("Registros visibles: " + productos.size());
            return true;

        } catch (SQLException e) {
            lblContador.setText("No se pudo consultar la base de datos");
            mostrarErrorBaseDatos("No se pudo consultar el listado de productos.");
            return false;
        }
    }

    private String categoriaFiltro() {
        String valor = cmbFiltroCategoria.getValue();

        if (valor == null || TODAS.equals(valor)) {
            return null;
        }
        return valor;
    }

    @FXML
    private void buscar() {
        if (cargarProductos() && productos.isEmpty()) {
            mostrarAlerta(Alert.AlertType.INFORMATION, "Sin coincidencias",
                    "No existen productos que coincidan con los filtros aplicados.");
        }
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        cmbFiltroCategoria.setValue(TODAS);
        chkSoloExistencias.setSelected(false);

        cargarProductos();
    }

    @FXML
    private void recargar() {
        cargarProductos();
    }

    @FXML
    private void guardar() {
        Producto nuevo = leerFormulario();

        if (nuevo == null) {
            return;
        }

        try {
            if (productoDAO.existeCodigo(nuevo.getCodigo(), null)) {
                mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado",
                        "El código " + nuevo.getCodigo() + " ya pertenece a otro producto.");
                txtCodigo.requestFocus();
                return;
            }

            int filas = productoDAO.insertar(nuevo);

            if (filas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro guardado",
                        "El producto fue registrado correctamente.");
                limpiarFormulario();
                cargarProductos();
            }

        } catch (SQLException e) {
            mostrarErrorSql("guardar el producto", e);
        }
    }

    @FXML
    private void actualizar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Producto no seleccionado",
                    "Debe seleccionar un producto de la tabla antes de actualizar.");
            return;
        }

        Producto cambios = leerFormulario();

        if (cambios == null) {
            return;
        }

        cambios.setIdProducto(seleccionado.getIdProducto());

        try {
            if (productoDAO.existeCodigo(cambios.getCodigo(), cambios.getIdProducto())) {
                mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado",
                        "El código " + cambios.getCodigo() + " pertenece a otro producto.");
                txtCodigo.requestFocus();
                return;
            }

            int filas = productoDAO.actualizar(cambios);

            if (filas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Registro actualizado",
                        "El producto fue actualizado correctamente.");
                limpiarFormulario();
                cargarProductos();
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Producto inexistente",
                        "El producto ya no existe en la base de datos. Se recargará el listado.");
                cargarProductos();
            }

        } catch (SQLException e) {
            mostrarErrorSql("actualizar el producto", e);
        }
    }

    @FXML
    private void eliminar() {
        Producto seleccionado = tblProductos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Producto no seleccionado",
                    "Debe seleccionar un producto de la tabla antes de eliminar.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Desea eliminar este producto?");
        confirmacion.setContentText(seleccionado.getCodigo() + " - " + seleccionado.getNombre());

        Optional<ButtonType> respuesta = confirmacion.showAndWait();

        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            int filas = productoDAO.eliminar(seleccionado.getIdProducto());

            if (filas > 0) {
                mostrarAlerta(Alert.AlertType.INFORMATION, "Producto eliminado",
                        "El producto fue eliminado correctamente.");
                limpiarFormulario();
                cargarProductos();
            } else {
                mostrarAlerta(Alert.AlertType.WARNING, "Producto inexistente",
                        "El producto ya no existe en la base de datos. Se recargará el listado.");
                cargarProductos();
            }

        } catch (SQLException e) {
            mostrarErrorSql("eliminar el producto", e);
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtId.clear();
        txtCodigo.clear();
        txtNombre.clear();
        cmbCategoria.setValue(null);
        txtPrecio.clear();
        txtStock.clear();

        tblProductos.getSelectionModel().clearSelection();
    }

    private void llenarFormulario(Producto p) {
        txtId.setText(String.valueOf(p.getIdProducto()));
        txtCodigo.setText(p.getCodigo());
        txtNombre.setText(p.getNombre());
        cmbCategoria.setValue(p.getCategoria());
        txtPrecio.setText(p.getPrecio().toPlainString());
        txtStock.setText(String.valueOf(p.getStock()));
    }

    private Producto leerFormulario() {
        StringBuilder errores = new StringBuilder();

        String codigo = txtCodigo.getText().trim().toUpperCase();
        if (codigo.isEmpty() || codigo.length() > 10) {
            errores.append("- El código debe tener entre 1 y 10 caracteres.\n");
        }

        String nombre = txtNombre.getText().trim();
        if (nombre.length() < 3 || nombre.length() > 80) {
            errores.append("- El nombre debe tener entre 3 y 80 caracteres.\n");
        }

        String categoria = cmbCategoria.getValue();
        if (categoria == null || !cmbCategoria.getItems().contains(categoria)) {
            errores.append("- Debe seleccionar una categoría de la lista.\n");
        }

        BigDecimal precio = null;
        String textoPrecio = txtPrecio.getText().trim();

        if (textoPrecio.isEmpty()) {
            errores.append("- El precio es obligatorio.\n");
        } else if (!textoPrecio.matches("-?\\d+(\\.\\d+)?")) {
            errores.append("- El precio debe ser numérico (use punto decimal, por ejemplo 12.50).\n");
        } else {
            precio = new BigDecimal(textoPrecio);

            if (precio.compareTo(BigDecimal.ZERO) <= 0) {
                errores.append("- El precio debe ser mayor que 0.\n");
            } else if (precio.scale() > 2) {
                errores.append("- El precio admite como máximo 2 decimales.\n");
            } else if (precio.compareTo(new BigDecimal("99999999.99")) > 0) {
                errores.append("- El precio no puede ser mayor que 99999999.99.\n");
            }
        }

        int stock = 0;
        String textoStock = txtStock.getText().trim();

        if (textoStock.isEmpty()) {
            errores.append("- El stock es obligatorio.\n");
        } else {
            try {
                stock = Integer.parseInt(textoStock);

                if (stock < 0) {
                    errores.append("- El stock no puede ser negativo.\n");
                }
            } catch (NumberFormatException e) {
                errores.append("- El stock debe ser un número entero entre 0 y 2147483647.\n");
            }
        }

        if (errores.length() > 0) {
            mostrarAlerta(Alert.AlertType.WARNING, "Validación",
                    "Revise los siguientes campos:\n" + errores);
            return null;
        }

        return new Producto(0, codigo, nombre, categoria, precio, stock);
    }

    private void mostrarErrorSql(String accion, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Código duplicado",
                    "El código ingresado ya pertenece a otro producto.");
        } else if ("23514".equals(e.getSQLState())) {
            mostrarAlerta(Alert.AlertType.WARNING, "Dato rechazado",
                    "La base de datos rechazó los datos porque no cumplen una restricción.");
        } else {
            mostrarErrorBaseDatos("No se pudo " + accion + ".");
        }
    }

    private void mostrarErrorBaseDatos(String mensaje) {
        mostrarAlerta(Alert.AlertType.ERROR, "Error de base de datos",
                mensaje + "\nVerifique la conexión con PostgreSQL e intente nuevamente.");
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
