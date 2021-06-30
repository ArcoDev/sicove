package Conexion;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import java.sql.Statement;

public class Conexion {
    
    static Connection contacto = null;
    private static String user = "sa";
    private static String password = "Sicove2020";
    
    public static Connection getConnection() {
        
        //String url = "jdbc:sqlserver://softlag.sytes.net:1433;databaseName=sicove_gas";
        String url = "jdbc:sqlserver://localhost:1433;databaseName=sicove_gas";
        //String url = "jdbc:sqlserver://localhost:1433;databaseName=sicove_gas";
        
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null, "No se pudo establecer la conexion" + e.getMessage(), "Erro de conexion", JOptionPane.ERROR_MESSAGE);
        }
        
        try {
            contacto = DriverManager.getConnection(url, Conexion.user, Conexion.password);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "Error" + e.getMessage(), "Error de conexion", JOptionPane.ERROR_MESSAGE);
        }
        return contacto;
    }
    
    public static ResultSet Select( String consulta) {
        Connection con = getConnection();
        
        Statement declara;
        
        try {
            declara = con.createStatement();
            ResultSet respuesta = declara.executeQuery(consulta);
            return respuesta;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "No se pudo establecer la conexion" + e.getMessage(), "Error de conexion", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
    public static Integer Insert( String consulta) {
        Connection con = getConnection();
        
        Statement declara;
        
        try {
            declara = con.createStatement();
            Integer respuesta = declara.executeUpdate(consulta);
            return respuesta;
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "No se pudo establecer la conexion" + e.getMessage(), "Error de conexion", JOptionPane.ERROR_MESSAGE);
        }
        return null;
    }
    
}
