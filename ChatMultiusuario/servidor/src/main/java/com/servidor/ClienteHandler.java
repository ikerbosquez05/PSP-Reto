package com.servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClienteHandler implements Runnable {

    private final Socket cliente;
    private final GestorUsuarios gestor;
    private String nombreUsuario;

    public ClienteHandler(Socket cliente, GestorUsuarios gestor) {
        this.cliente = cliente;
        this.gestor = gestor;
    }

    @Override
    public void run() {

        boolean registrado = false;

        try (
            BufferedReader entrada = new BufferedReader(
                new InputStreamReader(
                    cliente.getInputStream(),
                    StandardCharsets.UTF_8
                )
            );

            PrintWriter salida = new PrintWriter(
                cliente.getOutputStream(),
                true,
                StandardCharsets.UTF_8
            )
        ) {

            // Recibimos el nombre de usuario.
            nombreUsuario = entrada.readLine();

            if (nombreUsuario == null || nombreUsuario.isBlank()) {
                salida.println("ERROR|Nombre de usuario no válido.");
                LoggerServidor.registrar(
                    "Se ha rechazado una conexión por nombre no válido."
                );
                return;
            }

            nombreUsuario = nombreUsuario.trim();

            // Intentamos registrar al usuario.
            if (!gestor.registrarUsuario(nombreUsuario, salida)) {
                salida.println("ERROR|Nombre ocupado o servidor completo.");

                LoggerServidor.registrar(
                    "Conexión rechazada para el usuario "
                    + nombreUsuario + ": nombre ocupado o servidor completo."
                );
                return;
            }

            registrado = true;

            // Registramos la conexión del usuario.
            LoggerServidor.registrar(
                "Usuario " + nombreUsuario + " conectado."
            );

            // Confirmamos la conexión.
            salida.println("OK|Bienvenido " + nombreUsuario);

            if (salida.checkError()) {
                throw new IOException("No se pudo enviar la bienvenida.");
            }

            // Avisamos de la nueva conexión.
            gestor.enviarMensajePublico(
                "SERVIDOR|El usuario " + nombreUsuario
                + " se ha conectado."
            );

            // Actualizamos la lista de usuarios.
            gestor.enviarListaUsuarios();

            String mensaje;

            // Esperamos mensajes del cliente.
            while ((mensaje = entrada.readLine()) != null) {

                if (mensaje.equals("QUIT")) {
                    break;
                }

                procesarMensaje(mensaje, salida);

                if (salida.checkError()) {
                    throw new IOException(
                        "Error al enviar datos al cliente."
                    );
                }
            }

        } catch (IOException e) {

            LoggerServidor.registrar(
                "Error de comunicación con " + nombreUsuario
                + ": " + e.getMessage()
            );

        } finally {

            if (registrado) {

                // Eliminamos al usuario.
                gestor.eliminarUsuario(nombreUsuario);

                // Registramos su desconexión.
                LoggerServidor.registrar(
                    "Usuario " + nombreUsuario + " desconectado."
                );

                // Avisamos de su desconexión.
                gestor.enviarMensajePublico(
                    "SERVIDOR|El usuario " + nombreUsuario
                    + " se ha desconectado."
                );

                // Actualizamos la lista de usuarios.
                gestor.enviarListaUsuarios();
            }

            try {
                cliente.close();

                System.out.println(
                    "Conexión cerrada: " + nombreUsuario
                );

            } catch (IOException e) {

                LoggerServidor.registrar(
                    "Error al cerrar la conexión de "
                    + nombreUsuario + ": " + e.getMessage()
                );
            }
        }
    }

    // Procesa los mensajes recibidos.
    private void procesarMensaje(String mensaje, PrintWriter salida) {

        // MENSAJE PÚBLICO.
        if (mensaje.startsWith("PUBLIC|")) {

            String contenido = mensaje.substring(7);

            if (!contenido.isBlank()) {

                gestor.actualizarUltimoMensaje(
                    nombreUsuario + ": " + contenido
                );

                // Registramos el mensaje público.
                LoggerServidor.registrar(
                    "Mensaje público de " + nombreUsuario
                    + ": " + contenido
                );

                gestor.enviarMensajePublico(
                    "PUBLIC|" + nombreUsuario + "|" + contenido
                );

                gestor.enviarUltimoMensaje();
            }
        }

        // MENSAJE PRIVADO.
        else if (mensaje.startsWith("PRIVATE|")) {

            String[] partes = mensaje.split("\\|", 3);

            if (partes.length == 3
                    && !partes[1].isBlank()
                    && !partes[2].isBlank()) {

                String destinatario = partes[1];
                String contenido = partes[2];

                boolean enviado = gestor.enviarMensajePrivado(
                    destinatario,
                    "PRIVATE|" + nombreUsuario + "|" + contenido
                );

                if (enviado) {

                    // Enviamos una copia al emisor.
                    salida.println(
                        "PRIVATE|" + nombreUsuario + "|" + contenido
                    );

                    String resumen = nombreUsuario
                            + " ha enviado un mensaje privado a "
                            + destinatario;

                    gestor.actualizarUltimoMensaje(resumen);

                    // Registramos el mensaje privado.
                    LoggerServidor.registrar(
                        "Mensaje privado de " + nombreUsuario
                        + " para " + destinatario + ": " + contenido
                    );

                    gestor.enviarUltimoMensaje();

                } else {

                    salida.println(
                        "ERROR|El usuario " + destinatario
                        + " no está conectado."
                    );

                    LoggerServidor.registrar(
                        "Mensaje privado no entregado: "
                        + nombreUsuario + " intentó escribir a "
                        + destinatario + ", pero no estaba conectado."
                    );
                }

            } else {

                salida.println(
                    "ERROR|El mensaje privado tiene un formato incorrecto."
                );

                LoggerServidor.registrar(
                    "Mensaje privado con formato incorrecto de "
                    + nombreUsuario
                );
            }
        }

        // COMANDO DESCONOCIDO.
        else {

            salida.println("ERROR|Comando desconocido.");

            LoggerServidor.registrar(
                "Comando desconocido recibido de " + nombreUsuario
            );
        }
    }
}