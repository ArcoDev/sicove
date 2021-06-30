package Clases;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
/**
 *
 * @author awsof
 */
public class ErroresSync {
    
    public void GuardarError(String Error, String metodo, int linea) throws IOException {
        
        int hora, minuto;
        
        // Fecha de hoy
        Calendar c = Calendar.getInstance();
        String dia = Integer.toString(c.get(Calendar.DATE));
        int mesI = c.get(Calendar.MONTH) + 1;
        String annio = Integer.toString(c.get(Calendar.YEAR));
        dia = dia.length() > 1 ? dia : "0" + dia;

        String mes;
        mes = Integer.toString(mesI);
        mes = mes.length() > 1 ? mes : "0" + mes;

        hora = c.get(Calendar.HOUR_OF_DAY);
        minuto = c.get(Calendar.MINUTE);
        
        BufferedWriter bf;
        String ruta = "/home/sicove/Error/errorSicove.txt";
        bf = new BufferedWriter(new FileWriter(ruta));
        bf.write(Error);
        bf.write(" ");
        bf.write(metodo);
        bf.write(" ");
        bf.write(String.valueOf(linea));
        bf.write(" ");
        bf.write(annio+"/"+ mes +"/"+ dia +" "+ hora +":"+ minuto + "\n");
        bf.close();
        
    }
}
