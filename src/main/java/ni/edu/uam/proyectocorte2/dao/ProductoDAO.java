package ni.edu.uam.proyectocorte2.dao;

import ni.edu.uam.proyectocorte2.database.DatabaseConnection;
import ni.edu.uam.proyectocorte2.model.Producto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductoDAO {

    public List<Producto> buscar(String texto, String categoria, boolean soloExistencias) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT id_producto, codigo, nombre, categoria, precio, stock "
                        + "FROM producto WHERE 1 = 1");

        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND (nombre ILIKE ? OR codigo ILIKE ?)");
            parametros.add("%" + texto.trim() + "%");
            parametros.add("%" + texto.trim() + "%");
        }

        if (categoria != null) {
            sql.append(" AND categoria = ?");
            parametros.add(categoria);
        }

        if (soloExistencias) {
            sql.append(" AND stock > 0");
        }

        sql.append(" ORDER BY codigo ASC");

        List<Producto> lista = new ArrayList<>();

        try (
              Connection conexion = DatabaseConnection.getConnection();
              PreparedStatement ps = conexion.prepareStatement(sql.toString())
        ) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(crearProductoDesdeResultSet(rs));
                }
            }
        } catch (SQLException e) {
            registrarError("SELECT producto (buscar)", e);
            throw e;
        }

        return lista;
    }

    public int insertar(Producto p) throws SQLException {

        String sql = "INSERT INTO producto (codigo, nombre, categoria, precio, stock) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (
              Connection conexion = DatabaseConnection.getConnection();
              PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getCategoria());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setInt(5, p.getStock());

            return ps.executeUpdate();
        } catch (SQLException e) {
            registrarError("INSERT producto", e);
            throw e;
        }
    }

    public int actualizar(Producto p) throws SQLException {
        String sql = "UPDATE producto SET codigo = ?, nombre = ?, categoria = ?, "
                + "precio = ?, stock = ? WHERE id_producto = ?";

        try (
              Connection conexion = DatabaseConnection.getConnection();
              PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setString(1, p.getCodigo());
            ps.setString(2, p.getNombre());
            ps.setString(3, p.getCategoria());
            ps.setBigDecimal(4, p.getPrecio());
            ps.setInt(5, p.getStock());
            ps.setInt(6, p.getIdProducto());

            return ps.executeUpdate();
        } catch (SQLException e) {
            registrarError("UPDATE producto", e);
            throw e;
        }
    }

    public int eliminar(int idProducto) throws SQLException {
        String sql = "DELETE FROM producto WHERE id_producto = ?";

        try (
              Connection conexion = DatabaseConnection.getConnection();
              PreparedStatement ps = conexion.prepareStatement(sql)
        ) {
            ps.setInt(1, idProducto);

            return ps.executeUpdate();
        } catch (SQLException e) {
            registrarError("DELETE producto", e);
            throw e;
        }
    }

    public boolean existeCodigo(String codigo, Integer idExcluir) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM producto WHERE codigo = ?");

        if (idExcluir != null) {
            sql.append(" AND id_producto <> ?");
        }

        try (
              Connection conexion = DatabaseConnection.getConnection();
              PreparedStatement ps = conexion.prepareStatement(sql.toString())
        ) {
            ps.setString(1, codigo);

            if (idExcluir != null) {
                ps.setInt(2, idExcluir);
            }

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            registrarError("SELECT existeCodigo", e);
            throw e;
        }
    }

    private Producto crearProductoDesdeResultSet(ResultSet rs) throws SQLException {
        return new Producto(
                rs.getInt("id_producto"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("categoria"),
                rs.getBigDecimal("precio"),
                rs.getInt("stock")
        );
    }

    private void registrarError(String operacion, SQLException e) {
        System.err.println("[DAO] operacion=" + operacion
                + " | SQLState=" + e.getSQLState()
                + " | detalle=" + e.getMessage());
    }
}