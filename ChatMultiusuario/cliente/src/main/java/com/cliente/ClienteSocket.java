package com.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class ClienteSocket {

    private Socket socket;
    private BufferedReader entrada;
    private PrintWriter salida;

    // Establece la conexión con el servidor.
    public void conectar(String ip, int puerto, String usuario)
            throws IOException {

        socket = new Socket(ip, puerto);

        entrada = new BufferedReader(
                new InputStreamReader(
                        socket.getInputStream(),
                        StandardCharsets.UTF_8
                )
        );

        salida = new PrintWriter(
                socket.getOutputStream(),
                true,
                StandardCharsets.UTF_8
        );

        // Enviamos el nombre de usuario al servidor.
        salida.println(usuario);

        // Esperamos la respuesta de aceptación o rechazo.
        String respuesta = entrada.readLine();

        if (respuesta == null) {
            throw new IOException("El servidor ha cerrado la conexión.");
        }

        if (respuesta.startsWith("ERROR|")) {
            throw new IOException(respuesta.substring(6));
        }

        if (!respuesta.startsWith("OK|")) {
            throw new IOException("Respuesta inesperada del servidor.");
        }
    }

    // Envía un mensaje público.
    public void enviarPublico(String mensaje) {

        salida.println("PUBLIC|" + mensaje);

        comprobarEnvio();
    }

    // Envía un mensaje privado.
    public void enviarPrivado(String destinatario, String mensaje) {

        salida.println("PRIVATE|" + destinatario + "|" + mensaje);

        comprobarEnvio();
    }

    // Comprueba si ha ocurrido un error al enviar.
    private void comprobarEnvio() {

        if (salida == null || salida.checkError()) {
            throw new IllegalStateException(
                    "No se ha podido enviar el mensaje."
            );
        }
    }

    // Recibe un mensaje del servidor.
    public String recibirMensaje() throws IOException {

        return entrada.readLine();
    }

    // Comprueba si existe una conexión activa.
    public boolean estaConectado() {

        return socket != null
                && socket.isConnected()
                && !socket.isClosed();
    }

    // Cierra la conexión con el servidor.
    public void desconectar() {

        try {

            if (salida != null && socket != null && !socket.isClosed()) {
                salida.println("QUIT");
            }

            if (socket != null) {
                socket.close();
            }

        } catch (IOException e) {

            System.out.println(
                    "Error al cerrar la conexión: " + e.getMessage()
            );

        } finally {

            socket = null;
            entrada = null;
            salida = null;
        }
    }
}
