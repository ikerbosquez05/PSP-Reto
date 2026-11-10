package com.cliente;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PrimaryController {

    // Campos de conexión
    @FXML
    private TextField txtUsuario;

    @FXML
    private TextField txtIP;

    @FXML
    private TextField txtPuerto;

    @FXML
    private Button btnConectar;

    @FXML
    private Button btnDesconectar;

    @FXML
    private Label lblEstado;

    // Información del servidor
    @FXML
    private Label lblUsuarios;

    @FXML
    private Label lblTiempoActivo;

    @FXML
    private Label lblUltimoMensaje;

    // Paneles de actividad y chat
    @FXML
    private TextArea txtActividad;

    @FXML
    private TextArea txtChat;

    // Campos para enviar mensajes
    @FXML
    private ComboBox<String> cbTipo;

    @FXML
    private ComboBox<String> cbDestinatario;

    @FXML
    private TextField txtMensaje;

    @FXML
    private Button btnEnviar;

    // Objeto encargado de la comunicación
    private ClienteSocket cliente;

    private volatile boolean conectado = false;

    // Se ejecuta al cargar la interfaz
    @FXML
    private void initialize() {

        cbTipo.getItems().addAll("Público", "Privado");
        cbTipo.setValue("Público");

        cbDestinatario.setDisable(true);

        txtActividad.setEditable(false);
        txtChat.setEditable(false);

        lblEstado.setText("Desconectado");
        lblUsuarios.setText("Usuarios conectados: 0");
        lblTiempoActivo.setText("Tiempo activo: 00:00:00");
        lblUltimoMensaje.setText("Todavía no hay mensajes");

        btnDesconectar.setDisable(true);
        btnEnviar.setDisable(true);

        btnConectar.setOnAction(event -> conectar());
        btnDesconectar.setOnAction(event -> desconectar());
        btnEnviar.setOnAction(event -> enviarMensaje());

        cbTipo.setOnAction(event -> actualizarEstadoDestinatario());
    }

    // Establece la conexión con el servidor
    private void conectar() {

        String usuario = txtUsuario.getText().trim();
        String ip = txtIP.getText().trim();
        String textoPuerto = txtPuerto.getText().trim();

        if (usuario.isEmpty() || ip.isEmpty() || textoPuerto.isEmpty()) {
            txtActividad.appendText(
                    "ERROR: Completa todos los campos de conexión.\n"
            );
            return;
        }

        int puerto;

        try {

            puerto = Integer.parseInt(textoPuerto);

            if (puerto < 1 || puerto > 65535) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {

            txtActividad.appendText(
                    "ERROR: El puerto debe estar entre 1 y 65535.\n"
            );
            return;
        }

        cliente = new ClienteSocket();

        try {

            cliente.conectar(ip, puerto, usuario);

            conectado = true;

            lblEstado.setText("Conectado");

            btnConectar.setDisable(true);
            btnDesconectar.setDisable(false);
            btnEnviar.setDisable(false);

            txtActividad.appendText(
                    "Conectado al servidor como " + usuario + ".\n"
            );

            iniciarRecepcion();

        } catch (IOException e) {

            cliente.desconectar();
            cliente = null;

            txtActividad.appendText(
                    "ERROR DE CONEXIÓN: " + e.getMessage() + "\n"
            );

            lblEstado.setText("Desconectado");
        }
    }

    // Inicia un hilo independiente para recibir mensajes
    private void iniciarRecepcion() {

        Thread hiloRecepcion = new Thread(() -> {

            try {

                String mensaje;

                while (conectado
                        && (mensaje = cliente.recibirMensaje()) != null) {

                    String mensajeRecibido = mensaje;

                    Platform.runLater(
                            () -> procesarMensaje(mensajeRecibido)
                    );
                }

                // El servidor ha cerrado la conexión.
                if (conectado) {
                    Platform.runLater(() -> {
                        txtActividad.appendText(
                                "El servidor ha cerrado la conexión.\n"
                        );
                        desconectar();
                    });
                }

            } catch (IOException e) {

                if (conectado) {
                    Platform.runLater(() -> {
                        txtActividad.appendText(
                                "Se ha perdido la conexión con el servidor.\n"
                        );
                        desconectar();
                    });
                }
            }
        });

        hiloRecepcion.setName("Hilo-Recepcion-Cliente");
        hiloRecepcion.setDaemon(true);
        hiloRecepcion.start();
    }

    // Interpreta los mensajes recibidos del servidor
    private void procesarMensaje(String mensaje) {

        // Avisos de conexión y desconexión
        if (mensaje.startsWith("SERVIDOR|")) {

            txtActividad.appendText(
                    mensaje.substring(9) + "\n"
            );
        }

        // Mensajes públicos
        else if (mensaje.startsWith("PUBLIC|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {

                txtChat.appendText(
                        "[PÚBLICO] " + partes[1]
                        + ": " + partes[2] + "\n"
                );
            }
        }

        // Mensajes privados
        else if (mensaje.startsWith("PRIVATE|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {

                txtChat.appendText(
                        "[PRIVADO] " + partes[1]
                        + ": " + partes[2] + "\n"
                );
            }
        }

        // Lista de usuarios conectados
        else if (mensaje.startsWith("USERS|")
                || mensaje.equals("USERS")) {

            actualizarUsuarios(mensaje);
        }

        // Estadísticas del servidor
        else if (mensaje.startsWith("STATS|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {

                lblUsuarios.setText(
                        "Usuarios conectados: " + partes[1]
                );

                lblTiempoActivo.setText(
                        "Tiempo activo: " + partes[2]
                );
            }
        }

        // Último mensaje recibido
        else if (mensaje.startsWith("LAST|")) {

            lblUltimoMensaje.setText(
                    mensaje.substring(5)
            );
        }

        // Errores del servidor
        else if (mensaje.startsWith("ERROR|")) {

            txtActividad.appendText(
                    "ERROR: " + mensaje.substring(6) + "\n"
            );
        }
    }

    // Actualiza el ComboBox de destinatarios
    private void actualizarUsuarios(String mensaje) {

        String[] usuarios = mensaje.split("\\|");

        String destinatarioAnterior = cbDestinatario.getValue();

        cbDestinatario.getItems().clear();

        // Añadimos todos los usuarios excepto el propio cliente.
        for (int i = 1; i < usuarios.length; i++) {

            if (!usuarios[i].equals(txtUsuario.getText().trim())) {
                cbDestinatario.getItems().add(usuarios[i]);
            }
        }

        // Conservamos la selección si el usuario sigue conectado.
        if (destinatarioAnterior != null
                && cbDestinatario.getItems().contains(destinatarioAnterior)) {

            cbDestinatario.setValue(destinatarioAnterior);
        }

        actualizarEstadoDestinatario();
    }

    // Controla cuándo se puede seleccionar un destinatario
    private void actualizarEstadoDestinatario() {

        boolean privado = "Privado".equals(cbTipo.getValue());

        cbDestinatario.setDisable(
                !privado || !conectado
                || cbDestinatario.getItems().isEmpty()
        );
    }

    // Envía mensajes públicos o privados
    private void enviarMensaje() {

        if (!conectado || cliente == null) {
            return;
        }

        String contenido = txtMensaje.getText().trim();

        if (contenido.isEmpty()) {
            return;
        }

        try {

            if ("Público".equals(cbTipo.getValue())) {

                cliente.enviarPublico(contenido);

            } else {

                String destinatario = cbDestinatario.getValue();

                if (destinatario == null) {

                    txtActividad.appendText(
                            "Selecciona un destinatario.\n"
                    );
                    return;
                }

                cliente.enviarPrivado(destinatario, contenido);
            }

            txtMensaje.clear();

        } catch (IllegalStateException e) {

            txtActividad.appendText(
                    "ERROR AL ENVIAR: " + e.getMessage() + "\n"
            );
        }
    }

    // Cierra la conexión
    private void desconectar() {

        if (cliente != null) {

            conectado = false;

            cliente.desconectar();
            cliente = null;

            lblEstado.setText("Desconectado");

            btnConectar.setDisable(false);
            btnDesconectar.setDisable(true);
            btnEnviar.setDisable(true);

            cbDestinatario.getItems().clear();
            cbDestinatario.setDisable(true);

            lblUsuarios.setText("Usuarios conectados: 0");

            txtActividad.appendText(
                    "Te has desconectado del servidor.\n"
            );
        }
    }
}