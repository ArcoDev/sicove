package Clases;

import Conexion.Conexion;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;

public class Cortesias {
    
    ResultSet resp;
    String cor2;

    public Cortesias() {
    }

    public ArrayList checarCortesia( String codigoVale ) {
        ArrayList mensaje = new ArrayList();
        
        try {
            
            String select = "SELECT id_cortesia, " +
                "cor_cantidad_cortesia, " +
                "cor_fecha_cortesia_vencimiento, " +
                "cor_cantidad_aplicada, " +
                "cor_libre, " +
                "id_unidad, " +
                "cor_estatus_cortesia, " +
                "CASE " +
                "    WHEN cor_estatus_cortesia = 1 THEN 'Vale Autorizado.' " +
                "    WHEN cor_estatus_cortesia = 2 THEN 'Vale Cancelado.' " +
                "    WHEN cor_estatus_cortesia = 3 THEN 'Vale Vencido.' " +
                "    WHEN cor_estatus_cortesia = 4 THEN 'Vale No Autorizado.' " +
                "    WHEN cor_estatus_cortesia = 5 THEN 'Vale Ya Aplicado.' " +
                "END AS Mensaje " +
                "FROM si_cortesias " +
                "WHERE cor_codigo_cortesia = '" + codigoVale + "'";
            
            resp = Conexion.Select(select);
            
            try {
                if( resp.next() ) {
                    mensaje.add(resp.getInt("id_cortesia"));
                    mensaje.add(resp.getDouble("cor_cantidad_cortesia"));
                    mensaje.add(resp.getString("cor_fecha_cortesia_vencimiento"));
                    mensaje.add(resp.getDouble("cor_cantidad_aplicada"));
                    mensaje.add(resp.getInt("cor_libre"));
                    mensaje.add(resp.getString("id_unidad"));
                    mensaje.add(resp.getInt("cor_estatus_cortesia"));
                    mensaje.add(resp.getString("Mensaje"));
                }
            } catch (SQLException e) {
                ErroresSync errorNop = new ErroresSync();
                errorNop.GuardarError(e.getMessage(), "Error de Consulta cortesia", 53);
            }
            
        } catch (Exception e) {
        }
        return mensaje;
    }
    
    public int actualizarCortesia( String idCortesia, double cantidadCort, double cCortOriginal ) throws IOException {
        
        try {
            
            int cortUpdate;
            
            Calendar c = Calendar.getInstance();
            String dia = Integer.toString(c.get(Calendar.DATE));
            int mesI = c.get(Calendar.MONTH) + 1;
            String annio = Integer.toString(c.get(Calendar.YEAR));
            dia = dia.length() > 1 ? dia : "0" + dia;
            
            String mes;
            mes = Integer.toString(mesI);
            mes = mes.length() > 1 ? mes : "0" + mes;
            
            String hora = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
            String minuto  = Integer.toString(c.get(Calendar.MINUTE)) ;
            String segundo = Integer.toString(c.get(Calendar.SECOND)) ;
            
            hora = hora.length() > 1 ? hora : "0" + hora;
            minuto = minuto.length() > 1 ? minuto : "0" + minuto;
            segundo = segundo.length() > 1 ? segundo : "0" + segundo;
            
            String fVenta = annio + "-" + mes + "-" + dia + "T" + hora + ":" + minuto + ":" + segundo + ".000";
            
            
            String update;
            if( cantidadCort >= cCortOriginal ) {
                cortUpdate = (int)Math.round(cantidadCort);
                update = "UPDATE si_cortesias SET cor_fecha_cortesia_aplicada = '" + fVenta + "' , cor_cantidad_aplicada = " + cortUpdate + ", cor_estatus_cortesia = 5 WHERE id_cortesia = " + idCortesia;
                
            } else {
                cortUpdate = (int)Math.round(cantidadCort);
                update = "UPDATE si_cortesias SET cor_fecha_cortesia_aplicada = '" + fVenta + "' , cor_cantidad_aplicada = " + cortUpdate + " WHERE id_cortesia = " + idCortesia;
            }
            
            cor2 = update;
            
            System.out.println("Update de la cortesia: " + update);
            
            int cort = Conexion.Insert(update);
            
            update = update.replace("'", "''");
            
            Conexion.Insert("Insert INTO querys(consulta, sincronizado) VALUES ('"+ update +"', 0)");
            
            return cort;
        } catch (Exception e) {
            // System.out.println("Ocurrio un error al actualizar la cortesia");
            ErroresSync errorNop = new ErroresSync();
            errorNop.GuardarError(e.getMessage(), "Error al actualizar cortesia " + cor2, 96);
            return 0;
        }
    }
}
