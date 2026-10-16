package ni.edu.uam.proyectocorte2.pruebas;

import ni.edu.uam.proyectocorte2.dao.ProductoDAO;
import ni.edu.uam.proyectocorte2.model.Producto;

import java.math.BigDecimal;
import java.sql.SQLException;

public class PruebaE2 {

    public static void main(String[] args) {
        ProductoDAO dao = new ProductoDAO();

        probar(dao, new Producto(0, "P001", "Producto repetido", "Papelería", new BigDecimal("10.00"), 1),
                "UNIQUE (codigo duplicado)");

        probar(dao, new Producto(0, "E2X1", "Precio invalido", "Papelería", new BigDecimal("0.00"), 1),
                "CHECK (precio)");
    }

    private static void probar(ProductoDAO dao, Producto p, String descripcion) {
        try {
            int filas = dao.insertar(p);
            System.out.println("[" + descripcion + "] ERROR: se insertaron " + filas + " fila(s).");
        } catch (SQLException e) {
            System.out.println("[" + descripcion + "] rechazado. SQLState=" + e.getSQLState());
        }
    }
}
