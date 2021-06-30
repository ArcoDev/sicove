package Clases;

import Conexion.Conexion;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.ArrayList;

public class Mantenimientos {

    ResultSet resp;
    
    
    public Mantenimientos() {
    }
    
    public ArrayList checarMantenimiento( String unidad) throws IOException {
        ArrayList vuelta = new ArrayList();
                
        try {
            Calendar c = Calendar.getInstance();
            String dia = Integer.toString(c.get(Calendar.DATE));
            int mesI = c.get(Calendar.MONTH) + 1;
            String mes;
            String annio = Integer.toString(c.get(Calendar.YEAR));

            dia = dia.length() > 1 ? dia : "0" + dia;
            mes = Integer.toString(mesI);
            mes = mes.length() > 1 ? mes : "0" + mes;
            
            String select = "SELECT TOP 1 mtto.id_mantenimiento, mtto.mtto_fecha_reprogramada, si_unidades.un_codigo_barra, DATEDIFF(DAY, '" + annio + "-" + mes + "-" + dia + "', mtto.mtto_fecha_reprogramada) as Dias FROM si_mantenimineto AS mtto INNER JOIN si_unidades ON si_unidades.id_unidad = mtto.id_unidad WHERE mtto.id_unidad = " + unidad + " AND id_estatus = 3 ORDER BY mtto.mtto_fecha_reprogramada DESC;";
            
            resp = Conexion.Select(select);
            
            try {
                if( resp.next() ) {
                    vuelta.add(resp.getString("id_mantenimiento"));
                    vuelta.add(resp.getString("mtto_fecha_reprogramada"));
                    vuelta.add(resp.getString("un_codigo_barra"));
                    vuelta.add(resp.getString("Dias"));
                } else {
                    //System.out.println("Respuesta: Ninguna coincidencia encontrada");
                }
            } catch (SQLException e) {
                // System.err.println("Ocurrio un error " + e);
                ErroresSync errorNop = new ErroresSync();
                errorNop.GuardarError(e.getMessage(), "Error consultando mantenimiento", 48);
            }
            
        } catch (Exception e) {
                // System.err.println("Ocurrio un error " + e);
                ErroresSync errorNop = new ErroresSync();
                errorNop.GuardarError(e.getMessage(), "Error consultando mantenimiento", 54);
        }
        return vuelta;
    }
    
}
