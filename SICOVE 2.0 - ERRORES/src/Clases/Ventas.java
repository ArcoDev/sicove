package Clases;

import Conexion.Conexion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.nashorn.internal.runtime.JSType;

public class Ventas {

    String ins2;
    public Ventas() {
    }
    
    public Integer registrarVenta( ArrayList array, String id_unidad, String id_despachador, int metodo, String cortesia, String cant_cortesia, int estacion , int km, int aplica, String corAnt, int autoconsumo, int modulo, double cCortOriginal ) throws IOException {
        try {
            
            Calendar c = Calendar.getInstance();
            String dia = Integer.toString(c.get(Calendar.DATE));
            int mesI = c.get(Calendar.MONTH) + 1;
            String mes;
            String annio = Integer.toString(c.get(Calendar.YEAR));

            dia = dia.length() > 1 ? dia : "0" + dia;
            mes = Integer.toString(mesI);
            mes = mes.length() > 1 ? mes : "0" + mes;
            
            String hora = Integer.toString(c.get(Calendar.HOUR_OF_DAY));
            String minuto  = Integer.toString(c.get(Calendar.MINUTE)) ;
            String segundo = Integer.toString(c.get(Calendar.SECOND)) ;
            
            hora = hora.length() > 1 ? hora : "0" + hora;
            minuto = minuto.length() > 1 ? minuto : "0" + minuto;
            segundo = segundo.length() > 1 ? segundo : "0" + segundo;
            
            String pU;
            String fVenta = "";
            String servicio = "";
            double precioU = 0.0;
            String litros_temp;
            double litros = 0;
            double total = 0.0;
            String medidor = "";
            String[] temp;
            String total_temp = "";
            
            //System.out.println(array);
            String [] datos_extr= array.get(1).toString().split("\r\n");
            
            //for (int i = 0; i < array.size(); i++) {
            for (int i = 0; i < datos_extr.length; i++) {
  
                //System.out.println(array.get(i));
                //System.out.println(datos_extr[i].toString());
                
                //PRECIO
                if( datos_extr[i].toString().trim().contains("U.") || datos_extr[i].toString().trim().contains("U:") ) {
//                    if (Float.parseFloat(datos_extr[i+1].toString().trim().substring(0, 5)) > 0 ){
//                        pU = datos_extr[i+1].toString().trim().substring(0, 5);
//                    } else {
//                        pU = datos_extr[i+2].toString().trim().substring(0, 5);
//                    }
                    pU = datos_extr[i].toString().trim().split(":")[1];
                    Matcher encuentrador1 = Pattern.compile("\\d+\\.\\d+").matcher(pU);
                    while (encuentrador1.find()) { 
                        pU = encuentrador1.group();
                        break;
                    }
                    precioU = Double.parseDouble(pU);
                }

                /*
                if(array.get(i).toString().trim().contains("FECHA..")){
                    fVenta = array.get(i + 1).toString();
                    Matcher encuentrador = Pattern.compile("\\d{1,2}\\/\\d{1,2}/\\d{1,2}").matcher(fVenta);
                    while (encuentrador.find()) { 
                        fVenta = encuentrador.group();
                        break;
                    }

                    String hVenta = array.get(i + 2).toString();
                    Matcher encuentrador1 = Pattern.compile("\\d{2}\\:\\d{2}:\\d{2}").matcher(hVenta);
                    while (encuentrador1.find()) { 
                        hVenta = encuentrador1.group();
                        break;
                    }
                    
                    Calendar cal= Calendar.getInstance();
                    //int year = cal.get(Calendar.YEAR);
                    fVenta = annio + "-" + mes + "-" + dia + "T" + hora + ":" + minuto + ":" + segundo + ".000";
                }*/

                
                // SERVICIO
                if(datos_extr[i].toString().trim().contains("SERVI") || datos_extr[i].toString().trim().contains("CIO:")) {
//                    if (datos_extr[i+1].toString().trim().length() > 0){
//                        servicio = datos_extr[i+1].toString().trim();
//                    } else {
//                        servicio = datos_extr[i+2].toString().trim();
//                    }
                    servicio = datos_extr[i].toString().trim().split(":")[1];
                    
                    Matcher encuentradorServicio = Pattern.compile("\\d+").matcher(servicio);
                    while (encuentradorServicio.find()) { 
                        servicio = encuentradorServicio.group();
                        break;
                    }
                } 
                    
                // LITROS                
                if(datos_extr[i].toString().trim().contains("LITR") || datos_extr[i].toString().trim().contains("S..")) {
                    
                    //String litros_strng;
                    
//                    if (datos_extr[i+1].toString().trim().length() > 0 && JSType.isNumber(datos_extr[i+1].toString().trim())){
//                        litros = Double.parseDouble(datos_extr[i+1].toString().trim());
//                    } else {
//                        litros = Double.parseDouble(datos_extr[i+2].toString().trim());
//                    }
                    if (!datos_extr[i].toString().trim().contains(":")) {
                        litros_temp = datos_extr[i].toString().trim();
                        String litro_letras = "";
                        char[] numeros = {'0','1','2','3','4','5','6','7','8','9','.'};
                        for( int l=3; l<litros_temp.length(); l++) {
                            char caracter = litros_temp.charAt(l);
                            for (int cc=0; cc<numeros.length; cc++) {
                                if(numeros[cc] == caracter) {
                                    litro_letras += caracter;
                                }
                            }
                        }
                        litros = Double.parseDouble(litro_letras);
                    } else {
                        litros = Double.parseDouble(datos_extr[i].toString().trim().split(":")[1]);
                    }
                    
                    Matcher encuentradorLitros = Pattern.compile("\\d+").matcher(String.valueOf(litros));
                    while (encuentradorLitros.find()) { 
                        litros = Double.parseDouble(encuentradorLitros.group());    
                        break;
                    }
                }
                
                // TOTAL
                if(datos_extr[i].toString().trim().contains("TOTAL") || datos_extr[i].toString().trim().contains("$")) {
                    temp = datos_extr[i].toString().trim().split(" ");
                    //System.out.println(temp);
                    int t = 1;
                    while(temp[t].toString().trim().length() == 0) {
                        //System.out.println("contenido: "+temp[t]);
                        t++;
                    }
                    if(temp[t].toString().trim().length() > 0) {
                        total_temp = temp[t];
                        //System.out.println("contenido: "+temp[t]);
                    }
                    
                    Matcher encuentrador = Pattern.compile("\\d+\\.\\d+").matcher(total_temp.trim());
                    while (encuentrador.find()) { 
                        total_temp = encuentrador.group();
                        break;
                    }
                    total = Double.parseDouble(total_temp.trim());
                }

/* FINALIZA */
                
                
                /*
                if(array.get(i).toString().trim().contains("MEDIDOR")) {
                    medidor = array.get(i + 1).toString().trim();
//                    
                    Matcher encuentrador = Pattern.compile("\\d+").matcher(medidor);
                    while (encuentrador.find()) { 
                        medidor = encuentrador.group();
                        break;
                    }
                }*/
            }
            fVenta = annio + "-" + mes + "-" + dia + "T" + hora + ":" + minuto + ":" + segundo + ".000";
            
            String insert;
            medidor = String.valueOf(modulo);
            
            if( aplica == 1 ) {
                double NcCortesia;
                if( litros > Double.parseDouble(cant_cortesia) ) {
                    NcCortesia = Double.parseDouble(cant_cortesia);
                } else {
                    NcCortesia = litros;
                }
                
                double ncorAnt = Double.parseDouble(corAnt) + NcCortesia;
                
                String tipo = "VENTA";
                if( autoconsumo == 1 ) {
                    tipo = "AUTOCONSUMO";
                }
                
                insert = "INSERT INTO si_ventas(id_unidad, id_despachador, ve_fecha_venta, ve_precio_gas, id_metodo, ve_cortesia, ve_cantidad_cortesia, ve_kilometros, "
                        + "ve_servicio, ve_litros, ve_total, id_estacion, ve_observacion, ve_medidor) "
                        + "VALUES(" + id_unidad + ", "
                        + id_despachador + ", '"
                        + fVenta +"', "
                        + precioU + ", "
                        + metodo + ", "
                        + Integer.parseInt(cortesia) + ", "
                        + NcCortesia + ", "
                        + km + ", "
                        + servicio + ", "
                        + litros + ", "
                        + total + ", "
                        + estacion + ", '"
                        + "VENTA" + "', '"
                        + medidor + "'"
                        + ");";
                
                try {
                    Cortesias cort = new Cortesias();
                    int i = cort.actualizarCortesia( cortesia, ncorAnt, cCortOriginal );
                } catch (Exception e) {
                    System.out.println("Error al actualizar la cortesia");
                }
                 
            } else {
                String tipo = "VENTA";
                
                if( autoconsumo == 1 ) {
                    tipo = "AUTOCONSUMO";
                }
                
                if( "".equals(id_despachador) && "".equals(id_unidad) ) {
                    id_despachador = "0";
                    id_unidad = "-1";
                    tipo = "DOMESTICO";
                }
                
                if("".equals(id_unidad) ) {
                    id_unidad = "-1";
                }
                
                if( "0".equals(id_unidad) )
                    tipo = "RECIRCULACION";
                               
                insert = "INSERT INTO si_ventas(id_unidad, id_despachador, ve_fecha_venta, ve_precio_gas, id_metodo, ve_kilometros, "
                        + "ve_servicio, ve_litros, ve_total, id_estacion, ve_observacion, ve_medidor) "
                        + "VALUES(" + id_unidad + ", "
                        + id_despachador + ", '"
                        + fVenta + "', "
                        + precioU + ", "
                        + metodo + ", "
                        + km + ", "
                        + servicio + ", "
                        + litros + ", "
                        + total + ", "
                        + estacion + ", '"
                        + tipo + "', '"
                        + medidor + "'"
                        + ");";
            }
            
            System.out.println("Consulta insert de la venta: " + insert);
            // System.out.println("Fecha de la venta: " + fVenta);
            
            
            ins2 = insert;
            int col = Conexion.Insert(insert);
            
            insert = insert.replace("'", "''");
            
             Conexion.Insert("Insert INTO querys(consulta, sincronizado) VALUES ('"+ insert +"', 0)");
            return col;
        } catch (NumberFormatException e) {
            ErroresSync errorNop = new ErroresSync();
            errorNop.GuardarError(e.getMessage(), "Error de venta" + ins2, 189);
            return 0;
        }   
    }
    
}
