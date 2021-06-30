package sicove;

import Clases.Cortesias;
import Clases.ErroresSync;
import Clases.Mantenimientos;
import Clases.Ventas;
import Clases.VentasV2;
import Conexion.Conexion;
import com.fazecast.jSerialComm.*;
import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Sicove extends javax.swing.JFrame {
    
    Conexion con = new Conexion();
    ResultSet resp;
    String id_unidad = "-1";
    String id_despachador;
    String id_tipo_personal;
    int metodo = 1;
    int estacion = 0;
    int modulo = 0;
    int cortesia = 0;
    int autoconsumo = 0;
    double cCortOriginal = 0;
    int banderaVenta = 0;
    
    String gidCortesia;
    String gcantidadCortesia;
    String cAnterior;
    int banderaCorte = 0;
    
    String version = "v1";
    
    ArrayList dGaspar = new ArrayList();
    SerialPort comPort;// = SerialPort.getCommPorts()[0];
        
    Ventas v = new Ventas();
    VentasV2 v2 = new VentasV2();
    
    
    Runnable tarea = new Runnable() {
        public void run() {
            try {
                Socket s = new Socket("www.google.com", 80);

                if(s.isConnected()) {
                    lbl_internet.setVisible(false);
                } else {
                    s = new Socket("www.microsoft.com", 80);
                    if(s.isConnected()) {
                        lbl_internet.setVisible(false);
                    } else {
                        s = new Socket("www.amazon.com", 80);
                        if(s.isConnected()) {
                            lbl_internet.setVisible(false);
                        } else {
                            lbl_internet.setVisible(true);                                
                        }
                    }
                }

            } catch (Exception e) {
                lbl_internet.setVisible(true);
            }
        }
    };
    
    /*
    Runnable finaizarUsados = new Runnable() {
        @Override
        public void run() {
            try {
                
                Calendar c = Calendar.getInstance();
                String dia = Integer.toString(c.get(Calendar.DATE));
                int mesI = c.get(Calendar.MONTH) + 1;
                String annio = Integer.toString(c.get(Calendar.YEAR));
                dia = dia.length() > 1 ? dia : "0" + dia;

                String mes;
                mes = Integer.toString(mesI);
                mes = mes.length() > 1 ? mes : "0" + mes;

                
                String upd = "UPDATE si_cortesias SET " +
                    " cor_estatus_cortesia = 5 " +
                    " WHERE " +
                    " cor_cantidad_aplicada IS NOT NULL " +
                    " AND cor_estatus_cortesia = 1 " +
                    " AND cor_fecha_cortesia_aplicada < '" + annio + "-" + mes + "-" + dia + "'";
                
                
                Conexion.Insert(upd);
                Conexion.Insert("Insert INTO querys(consulta, sincronizado) VALUES ("+ upd +", 0)");
                
            } catch (Exception e) {
                System.out.println("Error al actualizar las cortesias " + e);
            }
        }
    };*/
    
    Runnable checarPuer = new Runnable() {
        @Override
        public void run() {
            try {
                comPort = SerialPort.getCommPorts()[0];
                lbl_Mensaje.setText("");
            } catch(Exception e) {
                lbl_internet.setText("Error conectando con Gas-Par. Por favor, verifique que el cable este conectado.");
            }
        }
    };

    public Sicove() {
        initComponents();
        try {
            comPort = SerialPort.getCommPorts()[0];
            txt_gafete.requestFocus();
        } catch(Exception e) {
            JOptionPane.showMessageDialog(null, "No se encuentra conectado el Gas-Par " + e.getMessage(), "Erro de conexion", JOptionPane.ERROR_MESSAGE);
            System.exit(0);
        }
        // ejecutar la consulta para verificar que haya internet
        ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(tarea, 1, 2, TimeUnit.MINUTES);
        
       /*  ejecutar la consulta para verificar que el puerto este abierto
        ScheduledExecutorService timer3 = Executors.newSingleThreadScheduledExecutor();
        timer.scheduleAtFixedRate(checarPuer, 1, 30, TimeUnit.SECONDS);*/
        
        // ejecutar la consulta para verificar que haya internet
        Calendar c = Calendar.getInstance();
        
        long delay = 24 - c.get(Calendar.HOUR_OF_DAY);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        
        /*
        ScheduledExecutorService timer2 = Executors.newSingleThreadScheduledExecutor();
        timer2.scheduleAtFixedRate(finaizarUsados, delay, 24, TimeUnit.HOURS);
        */
        
        // abarcar la pantalla completa y evitar que se pueda mover o minimizar 
        //this.setExtendedState(JFrame.MAXIMIZED_BOTH);
        //setDefaultCloseOperation(this.EXIT_ON_CLOSE);
        
        // Adaptacion de la image del logo de SICOVE
        ImageIcon imagen1 = new ImageIcon(getClass().getResource("/resources/logo.png"));
        Icon fondo = new ImageIcon(imagen1.getImage().getScaledInstance(logo.getWidth(), logo.getHeight(), Image.SCALE_DEFAULT));
        logo.setIcon(fondo);
        this.repaint();
        
        // Asigamos el foco al gafete
        txt_gafete.requestFocus();
        //Ocultamos los elementos que no son primarios
        lbl_alertOp.setVisible(false);
        lbl_internet.setVisible(false);
        pn_Vale.setVisible(false);
        lbl_alertKilo.setVisible(false);
        
        // Obtenemos siempre al iniciar el programa el modulo y estacion de la maquina
        resp = Conexion.Select("Select TOP 1 id_modulo, id_estacion, nombre from si_modulos_estacion");
        try {
            if( resp.next()) {
                estacion = resp.getInt("id_estacion");
                modulo = resp.getInt("id_modulo");
                String lblmodulo = resp.getString("nombre");
                lbl_Modulo.setText(lblmodulo);
            } else {
                txt_fecha_Ant.setText("");
                txt_Kilometraje_Ant.setText("");
                txt_volumen_Ant.setText("");
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>No se encuentra la información del gabinete. Por favor, informe al administrador inmediatamente.</p></html>");
            }
        } catch (SQLException ex) {
                // En caso de Error, nos mandaria el mensaje, pero esto seria que no se puede comunicar con la BD
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error al realizar la consulta. Notifique inmediatamente al administrador</p></html>");
                Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Listener que esta pendiente de lo que manda Gas-Par
        comPort.openPort();
        boolean addDataListener = comPort.addDataListener(new SerialPortDataListener() {
                        @Override
                        public int getListeningEvents() { return SerialPort.LISTENING_EVENT_DATA_AVAILABLE; }
                        @Override
                        public void serialEvent(SerialPortEvent event)
                        {
                            //int entrada = 1;
                            boolean entrada = false;
                            byte[] newData = new byte[comPort.bytesAvailable()];    
                            int numRead = comPort.readBytes(newData, newData.length);
                            //System.out.println("numRead: " + numRead);
                            String string = new String(newData, StandardCharsets.UTF_8);
                            System.out.println("Entro al gaspar:" + string);
                            
                            if( string.contains("CORT") ) { // Si se ejecuta un corte, ignoramos la data
                                banderaCorte = 1; // Estatus para saber que se esta ejecutando un corte
                                //System.out.println("CORTE");
                            }
                            
                            if( string.contains("EPORTE") ) { // Finalizamos el corte, con lo que dejamos libre para guardar la data
                                banderaCorte = 0;
                                //System.out.println("REPORTE");
                            }
                            
                            if( banderaCorte == 0){ // Si no es corte, procedemos a agregar la data al arreglo
                                
                                if(string.contains("J") || banderaVenta != 0 || string.contains("preferencias")) {
                                    
                                    if (!string.contains("NOTA")){
                                        version = "v2";
                                    }
                                    
                                    entrada = true;
                                    banderaVenta++;
                                    //System.out.println("VENTA");
                                }
                                dGaspar.add(string);
                            } else {
                                dGaspar.add(string);
                            }
                            
                            if( entrada ) { // Texto final cuado es una venta, lo que nos inidca que ya se finalizo la venta
                               
                                int km = txt_Kilometraje.getText().isEmpty() ? 0 : Integer.parseInt(txt_Kilometraje.getText());
                                int ret = 0;
                                //System.out.println("BANDERA 2");
                               
                                if( cortesia == 1 ) {
                                
                                    //System.out.println("CORTESIA");
                                    
                                    try {
                                        ret = v.registrarVenta(dGaspar, id_unidad, id_despachador, metodo, gidCortesia, gcantidadCortesia, estacion, km, cortesia, cAnterior, autoconsumo, modulo, cCortOriginal);
                                    } catch (IOException ex) {
                                        Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
                                        ErroresSync errorNop = new ErroresSync();
                                        try {
                                            errorNop.GuardarError(ex.getMessage(), "Error de registro cortesia", 189);
                                        } catch (IOException ex1) {
                                            Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex1);
                                        }
                                    }
                                } else {
                                    if( "6".equals(id_tipo_personal) ) {
                                        
                                        //System.out.println("TIPO PERSONAL");
                                        
                                        try {
                                            ret = v.registrarVenta(dGaspar, "0", id_despachador, metodo, null, null, estacion, km, cortesia, "", autoconsumo, modulo, 0);
                                        } catch (IOException ex) {
                                            Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
                                            ErroresSync errorNop = new ErroresSync();
                                            try {
                                                errorNop.GuardarError(ex.getMessage(), "Error de registro recirculacion", 189);
                                            } catch (IOException ex1) {
                                                Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex1);
                                            }
                                        }
                                    } else {
                                        
                                        //System.out.println("OTRO");
                                        
                                        try {
                                            
                                            if (version == "v1"){
                                                System.out.println("entro a la v1");
                                                ret = v.registrarVenta(dGaspar, id_unidad, id_despachador, metodo, null, null, estacion, km, cortesia, "", autoconsumo, modulo, 0);
                                            } else if (version == "v2") {
                                                System.out.println("entro a la v2");
                                                ret = v2.registrarVenta(dGaspar, id_unidad, id_despachador, metodo, null, null, estacion, km, cortesia, "", autoconsumo, modulo, 0);
                                            }
                                            
                                            //System.out.println(ret);
                                        } catch (IOException ex) {
                                            Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
                                            //System.out.println(ex);
                                        }
                                    }
                                }

                                if( ret > 0 ) {
                                    try {
                                        limpiar();
                                        lbl_Mensaje.setText("<html><p style='color: #009902;'>Venta realizada correctamente.</p></html>");
                                    } catch (Exception e) {
                                        // System.out.println("Error con el timer sleep");
                                        ErroresSync errorNop = new ErroresSync();
                                        try {
                                            errorNop.GuardarError(e.getMessage(), "Error con el timer sleep", 96);
                                        } catch (IOException ex) {
                                            Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                    }
                                } else if( ret > -1 ) {
                                    lbl_Mensaje.setText("");
                                } else {
                                    lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error al realizar la venta. Notifique inmediatamente al administrador</p></html>");
                                }
                                banderaVenta = 0;
                            }
                        }
                    });
        
        // CommPort
        System.out.println("Estado del puerto " + comPort.isOpen());
        System.out.println("Estado del puerto " + comPort.getDescriptivePortName());
        System.out.println("Estado del puerto " + SerialPort.getCommPorts().length);

        // Listener de los eventos de cada textBox
        // Gafete
        txt_gafete.addActionListener((ActionEvent e) -> {
            String gafete = txt_gafete.getText();
            System.out.println("Estado del puerto " + comPort.isOpen());
            resp = con.Select("Select id_persona, pe_nombre + ' ' + pe_apellido as Nombre, id_tipo_personal from si_personal where pe_codigo_barras='" + gafete + "'");
            try {
                if ( resp.next() ) {
                    lbl_Mensaje.setText("");
                    txt_nombre_Despachador.setText(resp.getString("Nombre"));
                    id_despachador = resp.getString("id_persona");
                    id_tipo_personal = resp.getString("id_tipo_personal");
                    txt_MPago.requestFocus();
                    
                } else {
                    txt_gafete.setText("");
                    txt_nombre_Despachador.setText("");
                    id_despachador = "";
                    txt_gafete.requestFocus();
                }
                dGaspar.clear();
            } catch (SQLException ex) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error al realizar la consulta. Notifique inmediatamente al administrador</p></html>");
                Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Unidad
        txt_Unidad.addActionListener((ActionEvent e) -> {
            String coBarras = txt_Unidad.getText();
            
            int guion = coBarras.indexOf('-');
            if( guion < 0 ) {
                String codB;
                Matcher encuentrador = Pattern.compile("\\d+").matcher(coBarras);
                while (encuentrador.find()) { 
                    
                    codB = encuentrador.group();
                    int ind = coBarras.indexOf(codB);
                    String nStrin = coBarras.substring(0, ind);
                    String numtring = coBarras.substring(ind, coBarras.length());
                    coBarras = nStrin + "-" + numtring;
                    break;
                }
            }
            
            
            resp = con.Select("select uni.id_unidad, uni.un_numero_economico, como.co_nombre, (select emp_autoconsumo from si_empresas WHERE id_empresa=uni.id_empresa) as autoconsumo, (select emp_nombre from si_empresas WHERE id_empresa=uni.id_empresa) as nEmpresa from si_unidades as uni LEFT JOIN si_comodatarios as como ON uni.id_comodatario = como.id_comodatario where uni.un_codigo_barra='" + coBarras + "'");
            try {
                if ( resp.next() ) {
                    txt_Num_Econ.setText(resp.getString("un_numero_economico") + " - " + resp.getString("nEmpresa"));
                    txt_Comodatario.setText(resp.getString("co_nombre"));
                    id_unidad = resp.getString("id_unidad");
                    autoconsumo = resp.getInt("autoconsumo");
                    lbl_Mensaje.setText("");
                    
                    Mantenimientos mant = new  Mantenimientos();
                    ArrayList mensaje = mant.checarMantenimiento(id_unidad);
                    if(mensaje.size() > 0 ) {
                        
                        int dias = Integer.parseInt(mensaje.get(3).toString());
                        
                        if( dias < 10  ) {
                            pn_Mensaje.setVisible(true);
                            if( dias <= 0 ) {
                                lbl_Mensaje.setText("<html><p style='color: #A70202;'> La Unidad " + mensaje.get(2).toString() + " tiene un mantenimiento programado para el día " + mensaje.get(1).toString().substring(0, 10) + ". Tiene un retraso de " + dias * -1 + " días. </p></html>");
                                
                            } else {
                                lbl_Mensaje.setText("<html><p style='color: #A70202;'> La Unidad " + mensaje.get(2).toString() + " tiene un mantenimiento programado para el día " + mensaje.get(1).toString().substring(0, 10) + ". Faltan " + dias + " días. </p></html>");
                            }
                        }
                    }
                    
                    resp = con.Select("select ve_fecha_venta, ve_kilometros, ve_litros from si_ventas where id_unidad="+ id_unidad +" AND ve_fecha_venta=(SELECT MAX(ve_fecha_venta) from si_ventas where id_unidad="+ id_unidad +")");
                    try {
                        if( resp.next()) {
                            txt_fecha_Ant.setText(resp.getString("ve_fecha_venta"));
                            txt_Kilometraje_Ant.setText(resp.getString("ve_kilometros"));
                            txt_volumen_Ant.setText(resp.getString("ve_litros"));
                            txt_Kilometraje.requestFocus();
                            txt_MPago.requestFocus();
                        } else {
                            txt_fecha_Ant.setText("");
                            txt_Kilometraje_Ant.setText("");
                            txt_volumen_Ant.setText("");
                            txt_Unidad.requestFocus();
                        }
                    } catch (SQLException ex) {
                        lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error al realizar la consulta. Notifique inmediatamente al administrador</p></html>");
                        Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    dGaspar.clear();
                    banderaCorte = 0;
                    String tempMPago = txt_MPago.getText();
                    if( "3".equals(tempMPago) ) {
                        pn_Vale.setVisible(true);
                        txt_Vale.setText("");
                        txt_Vale.requestFocus();
                        
                    } else {
                        txt_Kilometraje.requestFocus();
                    }
                    
                } else {
                    txt_Unidad.setText("");
                    txt_Num_Econ.setText("");
                    txt_Comodatario.setText("");
                    txt_Unidad.requestFocus();
                    lbl_Mensaje.setText("<html><p style='color: #A70202;'>Unidad no encontrada en el sistema</p></html>");
                    
                }
            } catch (SQLException ex) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error al realizar la consulta. Notifique inmediatamente al administrador</p></html>");
                Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // MPago 
        txt_MPago.addActionListener((e) -> {
            System.out.println("llega a metodo pago");
            String coBarras = txt_MPago.getText();
            
            resp = Conexion.Select("SELECT id_metodo, codigo_barras FROM si_metodos_pago WHERE codigo_barras = '" + coBarras + "'");
            
            try {
                System.out.println("llego a try");
                if(resp.next()) {
                    metodo = resp.getInt("id_metodo");
                    String seleccionado = resp.getString("codigo_barras");
                    
                    switch(seleccionado) {
                        case "1":
                            lbl_m1.setForeground(Color.BLUE);
                            break;
                        
                        case "2":
                            lbl_m2.setForeground(Color.BLUE);
                            break;
                        
                        case "3":
                            lbl_m3.setForeground(Color.BLUE);
                            break;
                        
                        case "4":
                            lbl_m4.setForeground(Color.BLUE);
                            break;
                        
                        case "5":
                            lbl_m5.setForeground(Color.BLUE);
                            break;
                            
                        case "6":
                            lbl_m6.setForeground(Color.BLUE);
                            break;
                            
                        case "7":
                            lbl_m7.setForeground(Color.BLUE);
                            break;
                            
                        case "8":
                            lbl_m8.setForeground(Color.BLUE);
                            break;
                    }
                    
                    
                    txt_Unidad.requestFocus();
                } else {
                    txt_MPago.setText("");
                    txt_MPago.requestFocus();
                }
            } catch (SQLException ex) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error al realizar la consulta. Notifique inmediatamente al administrador</p></html>");
                 Logger.getLogger(Sicove.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        // Kilometraje para comparacion
        txt_Kilometraje.addActionListener((e) -> {
            
            String kAct = "".equals(txt_Kilometraje.getText()) ? "0" : txt_Kilometraje.getText();
            String kAnt = "".equals(txt_Kilometraje_Ant.getText()) ? "0" : txt_Kilometraje_Ant.getText();

            int valActual = Integer.parseInt(kAct);
            int valAnt = Integer.parseInt(kAnt);

            if( valAnt >= valActual) {
                lbl_alertKilo.setVisible(true);

                if(autoconsumo == 1) {
                    txt_Kilometraje.setText("");
                }
                
            } else {
                lbl_alertKilo.setVisible(false);
            }
        });
        
        // Vale
        txt_Vale.addActionListener((e) -> {
            cortesia = 1;
            
            ArrayList mensaje;
            String coBarras = txt_Vale.getText();
            
            
            Cortesias cor = new Cortesias();
            mensaje = cor.checarCortesia(coBarras);

            if( mensaje.size() > 0 ) {
                
                pn_Mensaje.setVisible(true);
                lbl_Mensaje.setForeground(Color.BLACK);
                    
                double resta = (double)mensaje.get(1) - (double)mensaje.get(3);
                
                cCortOriginal = (double)mensaje.get(1);
                if( (int)mensaje.get(6) == 1 ) {
                    
                    
                    if( !mensaje.get(5).toString().equals(id_unidad) && (int)mensaje.get(4) == 0 ) {
                        lbl_Mensaje.setText("<html><p style='color: #A70202;'>La Unidad no coincide con el vale. Si no desea continuar, presione la tecla ESC</p></html>");
                    } else {
                        String fecha = (double)mensaje.get(3) > 0 ? " de Hoy " : mensaje.get(2).toString().substring(0, 10);
                        lbl_Mensaje.setText("<html><p>"+ mensaje.get(7) + " La cortesia es por "+ resta +" litros. Valido hasta el dia "+ fecha +"</p></html>");
                    }
                    
                    gcantidadCortesia = "" + resta;
                    
                } else {
                    lbl_Mensaje.setText("<html><p style='color: #A70202;'>"+ mensaje.get(7) +" Presione la tecla ESC para reiniciar la venta.</p></html>");
                    gcantidadCortesia = "" + resta;
                }
                cAnterior = Objects.isNull(mensaje.get(3)) ? "0" : mensaje.get(3).toString();
                gidCortesia = mensaje.get(0).toString();
                
                
            } else {
                lbl_Mensaje.setText("<html><p>Vale no registrado. Contacte con el administrador para solicitar ua aclaración. Presione ESC para reiniciar la venta.</p></html>");
            }
            
            txt_Kilometraje.requestFocus();
            pn_Vale.setVisible(false);
        });
        
    }
    
    public void limpiar() throws InterruptedException {
        
        id_unidad = "";
        id_despachador = "";
        id_tipo_personal = "";
        metodo = 1;
        cortesia = 0;
        gidCortesia = "";
        gcantidadCortesia = "";
        autoconsumo = 0;
        pn_Vale.setVisible(false);

        txt_gafete.setText("");
        txt_gafete.requestFocus();
        txt_nombre_Despachador.setText("");
        txt_Unidad.setText("");
        txt_Num_Econ.setText("");
        txt_Comodatario.setText("");
        txt_Kilometraje.setText("");
        txt_fecha_Ant.setText("");
        txt_Kilometraje_Ant.setText("");
        txt_volumen_Ant.setText("");
        txt_MPago.setText("");
        txt_Vale.setText("");
        lbl_alertKilo.setVisible(false);
        
        lbl_m1.setForeground(Color.BLACK);
        lbl_m2.setForeground(Color.BLACK);
        lbl_m3.setForeground(Color.BLACK);
        lbl_m4.setForeground(Color.BLACK);
        lbl_m5.setForeground(Color.BLACK);
        lbl_m6.setForeground(Color.BLACK);
        lbl_m7.setForeground(Color.BLACK);
        lbl_m8.setForeground(Color.BLACK);

        lbl_Mensaje.setText("");
        
        Thread.sleep(1000);
        dGaspar.clear();
    }
    
    public void checarPuerto() {
        if (comPort.isOpen() == false) {
            //System.out.println("Puerto abierto");
            lbl_Mensaje.setText("<html><p>Error conectando con Gas-Par. Por favor, verifique que el cable este conectado.</p></html>");
            comPort.openPort(1);
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        pn_Vale = new javax.swing.JPanel();
        txt_Vale = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        logo = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        txt_fecha_Ant = new javax.swing.JTextField();
        txt_volumen_Ant = new javax.swing.JTextField();
        txt_Kilometraje_Ant = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        txt_nombre_Despachador = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        txt_gafete = new javax.swing.JTextField();
        txt_Unidad = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        txt_MPago = new javax.swing.JTextField();
        lbl_m2 = new javax.swing.JLabel();
        lbl_m5 = new javax.swing.JLabel();
        lbl_m1 = new javax.swing.JLabel();
        lbl_m3 = new javax.swing.JLabel();
        lbl_m4 = new javax.swing.JLabel();
        lbl_m6 = new javax.swing.JLabel();
        lbl_m8 = new javax.swing.JLabel();
        lbl_m7 = new javax.swing.JLabel();
        txt_Num_Econ = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        lbl_alertOp = new javax.swing.JLabel();
        lbl_internet = new javax.swing.JLabel();
        txt_Kilometraje = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        txt_Comodatario = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        pn_Mensaje = new javax.swing.JPanel();
        lbl_alertKilo = new javax.swing.JLabel();
        lbl_Mensaje = new javax.swing.JLabel();
        lbl_Version = new javax.swing.JLabel();
        lbl_Modulo = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("SICOVE");
        setBackground(new java.awt.Color(255, 255, 255));
        setUndecorated(true);
        setResizable(false);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        pn_Vale.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        txt_Vale.setFont(new java.awt.Font("Ubuntu", 0, 24)); // NOI18N
        txt_Vale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txt_ValeActionPerformed(evt);
            }
        });
        txt_Vale.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_ValeKeyTyped(evt);
            }
        });

        jLabel12.setFont(new java.awt.Font("Ubuntu", 0, 24)); // NOI18N
        jLabel12.setText("Escaneé Vale de Cortesia");

        javax.swing.GroupLayout pn_ValeLayout = new javax.swing.GroupLayout(pn_Vale);
        pn_Vale.setLayout(pn_ValeLayout);
        pn_ValeLayout.setHorizontalGroup(
            pn_ValeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pn_ValeLayout.createSequentialGroup()
                .addContainerGap(199, Short.MAX_VALUE)
                .addGroup(pn_ValeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel12)
                    .addComponent(txt_Vale, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(189, 189, 189))
        );
        pn_ValeLayout.setVerticalGroup(
            pn_ValeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pn_ValeLayout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addComponent(jLabel12)
                .addGap(18, 18, 18)
                .addComponent(txt_Vale, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(47, Short.MAX_VALUE))
        );

        getContentPane().add(pn_Vale, new org.netbeans.lib.awtextra.AbsoluteConstraints(380, 220, -1, -1));

        logo.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        logo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/resources/logo.png"))); // NOI18N
        logo.setDisabledIcon(null);
        logo.setFocusable(false);
        getContentPane().add(logo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 60, 270, 190));

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 36)); // NOI18N
        jLabel7.setText("Consumo anterior");

        jLabel2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel2.setText("Fecha:");

        jLabel9.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel9.setText("Kilometraje:");

        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel10.setText("Volumen:");

        txt_fecha_Ant.setEditable(false);
        txt_fecha_Ant.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_fecha_Ant.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txt_fecha_Ant.setFocusable(false);

        txt_volumen_Ant.setEditable(false);
        txt_volumen_Ant.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_volumen_Ant.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txt_volumen_Ant.setFocusable(false);

        txt_Kilometraje_Ant.setEditable(false);
        txt_Kilometraje_Ant.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_Kilometraje_Ant.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        txt_Kilometraje_Ant.setFocusable(false);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(txt_Kilometraje_Ant, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txt_volumen_Ant, javax.swing.GroupLayout.PREFERRED_SIZE, 202, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(txt_fecha_Ant)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 29, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_fecha_Ant, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_volumen_Ant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_Kilometraje_Ant, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(530, 550, 780, 180));

        txt_nombre_Despachador.setEditable(false);
        txt_nombre_Despachador.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_nombre_Despachador.setFocusable(false);

        jLabel3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel3.setText("Despachador:");

        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel5.setText("Unidad:");

        jLabel8.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel8.setText("Nombre:");

        txt_gafete.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_gafete.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_gafeteKeyTyped(evt);
            }
        });

        txt_Unidad.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_Unidad.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_UnidadKeyTyped(evt);
            }
        });

        jLabel13.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel13.setText("Pago:");

        txt_MPago.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_MPago.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_MPagoKeyTyped(evt);
            }
        });

        lbl_m2.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m2.setText("2. Crédito");

        lbl_m5.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m5.setText("5. T. Credito/Deb");

        lbl_m1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m1.setText("1. Contado");

        lbl_m3.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m3.setText("3. Cortesia");

        lbl_m4.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m4.setText("4. ValeBillete");

        lbl_m6.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m6.setText("6. T. Accor");

        lbl_m8.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m8.setText("8. Otro");

        lbl_m7.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        lbl_m7.setText("7. Tranferencia");

        txt_Num_Econ.setEditable(false);
        txt_Num_Econ.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_Num_Econ.setFocusable(false);

        jLabel11.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel11.setText("Num. Econ:");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel13)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(40, 40, 40)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(txt_Unidad, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_MPago, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(txt_gafete, javax.swing.GroupLayout.PREFERRED_SIZE, 233, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGap(174, 174, 174)
                                .addComponent(txt_nombre_Despachador, javax.swing.GroupLayout.PREFERRED_SIZE, 339, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(99, 99, 99))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(jPanel2Layout.createSequentialGroup()
                                        .addComponent(lbl_m2, javax.swing.GroupLayout.PREFERRED_SIZE, 125, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lbl_m4, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                            .addComponent(lbl_m1, javax.swing.GroupLayout.PREFERRED_SIZE, 142, javax.swing.GroupLayout.PREFERRED_SIZE)
                                            .addGap(27, 27, 27)
                                            .addComponent(lbl_m3, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(lbl_m7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(lbl_m6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(35, 35, 35))))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(txt_Num_Econ, javax.swing.GroupLayout.PREFERRED_SIZE, 422, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(lbl_m5)
                        .addGap(32, 32, 32)
                        .addComponent(lbl_m8)
                        .addContainerGap())))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_gafete, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_nombre_Despachador, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel13)
                            .addComponent(txt_MPago, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(lbl_m1)
                            .addComponent(lbl_m3)
                            .addComponent(lbl_m6))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txt_Unidad, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lbl_m2)
                    .addComponent(lbl_m4)
                    .addComponent(lbl_m7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lbl_m8)
                    .addComponent(lbl_m5)
                    .addComponent(txt_Num_Econ, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        getContentPane().add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 20, 1070, 270));

        jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel1.setText("Mensaje del sistema");
        getContentPane().add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 410, -1, -1));

        lbl_alertOp.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        lbl_alertOp.setForeground(new java.awt.Color(255, 0, 0));
        lbl_alertOp.setText("Opcion incorrecta");
        getContentPane().add(lbl_alertOp, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 580, 160, 40));

        lbl_internet.setFont(new java.awt.Font("Ubuntu", 0, 48)); // NOI18N
        lbl_internet.setForeground(java.awt.Color.red);
        lbl_internet.setText("Sin internet");
        getContentPane().add(lbl_internet, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 620, 290, 60));

        txt_Kilometraje.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_Kilometraje.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txt_KilometrajeKeyTyped(evt);
            }
        });
        getContentPane().add(txt_Kilometraje, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 300, 360, 40));

        jLabel4.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel4.setText("Kilometraje:");
        getContentPane().add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(810, 300, 166, 40));

        txt_Comodatario.setEditable(false);
        txt_Comodatario.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        txt_Comodatario.setFocusable(false);
        getContentPane().add(txt_Comodatario, new org.netbeans.lib.awtextra.AbsoluteConstraints(210, 300, 560, 40));

        jLabel6.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
        jLabel6.setText("Comodatario:");
        getContentPane().add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 300, -1, 40));

        pn_Mensaje.setEnabled(false);
        pn_Mensaje.setFocusable(false);

        javax.swing.GroupLayout pn_MensajeLayout = new javax.swing.GroupLayout(pn_Mensaje);
        pn_Mensaje.setLayout(pn_MensajeLayout);
        pn_MensajeLayout.setHorizontalGroup(
            pn_MensajeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );
        pn_MensajeLayout.setVerticalGroup(
            pn_MensajeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 120, Short.MAX_VALUE)
        );

        getContentPane().add(pn_Mensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(1320, 600, 50, 120));

        lbl_alertKilo.setFont(new java.awt.Font("Ubuntu", 0, 18)); // NOI18N
        lbl_alertKilo.setForeground(new java.awt.Color(255, 0, 0));
        lbl_alertKilo.setText("El kilometraje no puede ser inferior al anterior");
        lbl_alertKilo.setFocusable(false);
        getContentPane().add(lbl_alertKilo, new org.netbeans.lib.awtextra.AbsoluteConstraints(980, 350, 380, 30));

        lbl_Mensaje.setFont(lbl_Mensaje.getFont().deriveFont(lbl_Mensaje.getFont().getSize()+15f));
        lbl_Mensaje.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        getContentPane().add(lbl_Mensaje, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 470, 1300, 70));

        lbl_Version.setFont(new java.awt.Font("Consolas", 1, 14)); // NOI18N
        lbl_Version.setText("v2.0");
        getContentPane().add(lbl_Version, new org.netbeans.lib.awtextra.AbsoluteConstraints(1280, 0, -1, 20));

        lbl_Modulo.setFont(new java.awt.Font("Calibri", 1, 24)); // NOI18N
        lbl_Modulo.setText("V2.0");
        getContentPane().add(lbl_Modulo, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 10, 260, 40));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void txt_KilometrajeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_KilometrajeKeyTyped

        boolean primary = evt.toString().matches("(.*)primaryLevelUnicode=27(.*)");        
        
        if( primary ) {
            try {
                limpiar();
                return;
                
            } catch (Exception e) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error con la entrada de datos. Notifique inmediatamente al administrador</p></html>");
            }
        }
        
        char caracter = evt.getKeyChar();

        // Verificar si la tecla pulsada no es un digito
        if(((caracter < '0') ||
           (caracter > '9')) &&
           (caracter != '\b' /*corresponde a BACK_SPACE*/))
        {
           evt.consume();  // ignorar el evento de teclado
        }
        
    }//GEN-LAST:event_txt_KilometrajeKeyTyped

    private void txt_gafeteKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_gafeteKeyTyped
        boolean primary = evt.toString().matches("(.*)primaryLevelUnicode=27(.*)");        
        
        if( primary ) {
            try {
                limpiar();
                
            } catch (Exception e) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error con la entrada de datos. Notifique inmediatamente al administrador</p></html>");
            }
        }
    }//GEN-LAST:event_txt_gafeteKeyTyped

    private void txt_UnidadKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_UnidadKeyTyped
        boolean primary = evt.toString().matches("(.*)primaryLevelUnicode=27(.*)");        
        
        if( primary ) {
            try {
                limpiar();
                
            } catch (Exception e) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error con la entrada de datos. Notifique inmediatamente al administrador</p></html>");
            }
        }
    }//GEN-LAST:event_txt_UnidadKeyTyped

    private void txt_MPagoKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_MPagoKeyTyped
        boolean primary = evt.toString().matches("(.*)primaryLevelUnicode=27(.*)");        
        
        if( primary ) {
            try {
                limpiar();
            } catch (Exception e) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error con la entrada de datos. Notifique inmediatamente al administrador</p></html>");
            }
        }
    }//GEN-LAST:event_txt_MPagoKeyTyped

    private void txt_ValeKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txt_ValeKeyTyped
        boolean primary = evt.toString().matches("(.*)primaryLevelUnicode=27(.*)");        
        
        if( primary ) {
            try {
                limpiar();
                pn_Vale.setVisible(false);
            } catch (Exception e) {
                lbl_Mensaje.setText("<html><p style='color: #A70202;'>Ocurrio un error con la entrada de datos. Notifique inmediatamente al administrador</p></html>");
            }
        }
    }//GEN-LAST:event_txt_ValeKeyTyped

    private void txt_ValeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txt_ValeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txt_ValeActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(Sicove.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Sicove.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Sicove.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Sicove.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Sicove().setVisible(true);
            }
        });
        
    }
        

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JLabel lbl_Mensaje;
    private javax.swing.JLabel lbl_Modulo;
    private javax.swing.JLabel lbl_Version;
    private javax.swing.JLabel lbl_alertKilo;
    private javax.swing.JLabel lbl_alertOp;
    private javax.swing.JLabel lbl_internet;
    private javax.swing.JLabel lbl_m1;
    private javax.swing.JLabel lbl_m2;
    private javax.swing.JLabel lbl_m3;
    private javax.swing.JLabel lbl_m4;
    private javax.swing.JLabel lbl_m5;
    private javax.swing.JLabel lbl_m6;
    private javax.swing.JLabel lbl_m7;
    private javax.swing.JLabel lbl_m8;
    private javax.swing.JLabel logo;
    private javax.swing.JPanel pn_Mensaje;
    private javax.swing.JPanel pn_Vale;
    private javax.swing.JTextField txt_Comodatario;
    private javax.swing.JTextField txt_Kilometraje;
    private javax.swing.JTextField txt_Kilometraje_Ant;
    private javax.swing.JTextField txt_MPago;
    private javax.swing.JTextField txt_Num_Econ;
    private javax.swing.JTextField txt_Unidad;
    private javax.swing.JTextField txt_Vale;
    private javax.swing.JTextField txt_fecha_Ant;
    private javax.swing.JTextField txt_gafete;
    private javax.swing.JTextField txt_nombre_Despachador;
    private javax.swing.JTextField txt_volumen_Ant;
    // End of variables declaration//GEN-END:variables
}
