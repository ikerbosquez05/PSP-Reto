
package com.cliente;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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

    // Área donde aparecen los mensajes
    @FXML
    private TextArea txtChat;

    // Campos para enviar mensajes
    @FXML
    private CheckBox cbPrivado;

    @FXML
    private TextField txtDestinatario;

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

        // El destinatario solo se habilita al seleccionar Privado.
        txtDestinatario.setDisable(true);

        // El chat no se puede modificar manualmente.
        txtChat.setEditable(false);

        // Estado inicial de la interfaz.
        lblEstado.setText("Desconectado");

        btnDesconectar.setDisable(true);
        btnEnviar.setDisable(true);

        // Configuramos los botones.
        btnConectar.setOnAction(event -> conectar());
        btnDesconectar.setOnAction(event -> desconectar());
        btnEnviar.setOnAction(event -> enviarMensaje());

        // Controlamos la casilla de mensajes privados.
        cbPrivado.setOnAction(
            event -> actualizarEstadoDestinatario()
        );
    }

    // Establece la conexión con el servidor
    private void conectar() {

        String usuario = txtUsuario.getText().trim();
        String ip = txtIP.getText().trim();
        String textoPuerto = txtPuerto.getText().trim();

        if (usuario.isEmpty() || ip.isEmpty() || textoPuerto.isEmpty()) {

            txtChat.appendText(
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

            txtChat.appendText(
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

            txtChat.appendText(
                "Conectado al servidor como " + usuario + ".\n"
            );

            actualizarEstadoDestinatario();

            // Iniciamos el hilo de recepción.
            iniciarRecepcion();

        } catch (IOException e) {

            cliente.desconectar();
            cliente = null;

            txtChat.appendText(
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

                        txtChat.appendText(
                            "El servidor ha cerrado la conexión.\n"
                        );

                        desconectar();
                    });
                }

            } catch (IOException e) {

                if (conectado) {

                    Platform.runLater(() -> {

                        txtChat.appendText(
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

        // Avisos de conexión y desconexión.
        if (mensaje.startsWith("SERVIDOR|")) {

            txtChat.appendText(
                mensaje.substring(9) + "\n"
            );
        }

        // Mensajes públicos.
        else if (mensaje.startsWith("PUBLIC|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {

                txtChat.appendText(
                    partes[1] + ": " + partes[2] + "\n"
                );
            }
        }

        // Mensajes privados.
        else if (mensaje.startsWith("PRIVATE|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3) {

                txtChat.appendText(
                    "[PRIVADO] " + partes[1]
                    + ": " + partes[2] + "\n"
                );
            }
        }

        // Errores enviados por el servidor.
        else if (mensaje.startsWith("ERROR|")) {

            txtChat.appendText(
                "ERROR: " + mensaje.substring(6) + "\n"
            );
        }

        // USERS, STATS y LAST son mensajes de control
        // que no necesitamos mostrar en el chat.
    }

    // Habilita o deshabilita el campo del destinatario
    private void actualizarEstadoDestinatario() {

        boolean privado = cbPrivado.isSelected();

        txtDestinatario.setDisable(
            !privado || !conectado
        );

        if (!privado) {
            txtDestinatario.clear();
        }
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

            // Si la casilla está marcada, enviamos un privado.
            if (cbPrivado.isSelected()) {

                String destinatario =
                    txtDestinatario.getText().trim();

                if (destinatario.isEmpty()) {

                    txtChat.appendText(
                        "ERROR: Introduce un destinatario.\n"
                    );
                    return;
                }

                if (destinatario.equals(txtUsuario.getText().trim())) {

                    txtChat.appendText(
                        "ERROR: No puedes enviarte un mensaje a ti mismo.\n"
                    );
                    return;
                }

                cliente.enviarPrivado(destinatario, contenido);

            } else {

                // Si no está marcada, enviamos un mensaje público.
                cliente.enviarPublico(contenido);
            }

            txtMensaje.clear();

        } catch (IllegalStateException e) {

            txtChat.appendText(
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

            cbPrivado.setSelected(false);
            txtDestinatario.clear();
            txtDestinatario.setDisable(true);

            txtChat.appendText(
                "Te has desconectado del servidor.\n"
            );
        }
    }
}