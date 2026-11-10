package com.servidor;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GestorUsuarios {

    // Número máximo de clientes que pueden conectarse.
    private static final int MAX_CLIENTES = 10;

    // Almacena los nombres de usuario y sus canales de salida.
    private final Map<String, PrintWriter> usuarios = new HashMap<>();

    // Guarda información sobre el último mensaje recibido.
    private String ultimoMensaje = "Ningún mensaje recibido todavía.";

    // Registra un usuario si hay espacio y su nombre está disponible.
    public synchronized boolean registrarUsuario(String nombre, PrintWriter salida) {

        if (usuarios.size() >= MAX_CLIENTES) {
            return false;
        }

        if (usuarios.containsKey(nombre)) {
            return false;
        }

        usuarios.put(nombre, salida);

        System.out.println("Usuario registrado: " + nombre);

        return true;
    }

    // Elimina un usuario de la lista de conectados.
    public synchronized void eliminarUsuario(String nombre) {

        PrintWriter eliminado = usuarios.remove(nombre);

        if (eliminado != null) {
            System.out.println("Usuario eliminado: " + nombre);
        }
    }

    // Envía un mensaje público a todos los usuarios conectados.
    public void enviarMensajePublico(String mensaje) {

        Map<String, PrintWriter> copiaUsuarios;

        // Copiamos los usuarios mientras protegemos el acceso al mapa.
        synchronized (this) {
            copiaUsuarios = new HashMap<>(usuarios);
        }

        // Enviamos los mensajes fuera del bloque sincronizado.
        for (Map.Entry<String, PrintWriter> usuario : copiaUsuarios.entrySet()) {

            PrintWriter salida = usuario.getValue();

            salida.println(mensaje);

            // PrintWriter no lanza una excepción cuando falla la escritura.
            // Por eso comprobamos si se ha producido algún error.
            if (salida.checkError()) {

                System.out.println(
                        "Error al enviar mensaje a: " + usuario.getKey()
                );

                eliminarSiCoincide(usuario.getKey(), salida);
            }
        }
    }

    // Envía un mensaje privado únicamente al destinatario.
    public boolean enviarMensajePrivado(String destinatario, String mensaje) {

        PrintWriter salida;

        // Consultamos el destinatario de forma sincronizada.
        synchronized (this) {
            salida = usuarios.get(destinatario);
        }

        // Si no está conectado, no podemos enviarle el mensaje.
        if (salida == null) {
            return false;
        }

        // Realizamos el envío fuera del bloque sincronizado.
        salida.println(mensaje);

        // Comprobamos si ha fallado la escritura.
        if (salida.checkError()) {

            System.out.println(
                    "Error al enviar mensaje privado a: " + destinatario
            );

            eliminarSiCoincide(destinatario, salida);

            return false;
        }

        return true;
    }

    // Elimina un usuario solamente si sigue asociado al mismo canal de salida.
    private synchronized void eliminarSiCoincide(
            String nombre, PrintWriter salida) {

        if (usuarios.get(nombre) == salida) {
            usuarios.remove(nombre);
            System.out.println("Usuario eliminado por error de comunicación: " + nombre);
        }
    }

    // Devuelve una copia de los nombres de los usuarios conectados.
    public synchronized List<String> obtenerUsuariosConectados() {

        return new ArrayList<>(usuarios.keySet());
    }

    // Devuelve el número de usuarios conectados.
    public synchronized int obtenerNumeroUsuarios() {

        return usuarios.size();
    }

    // Actualiza la información del último mensaje recibido.
    public synchronized void actualizarUltimoMensaje(String mensaje) {

        ultimoMensaje = mensaje;
    }

    // Devuelve el último mensaje recibido.
    public synchronized String obtenerUltimoMensaje() {

        return ultimoMensaje;
    }

    // Envía la lista actual de usuarios a todos los clientes.
    public void enviarListaUsuarios() {

        List<String> lista = obtenerUsuariosConectados();

        StringBuilder mensaje = new StringBuilder("USERS");

        for (String usuario : lista) {
            mensaje.append("|").append(usuario);
        }

        enviarMensajePublico(mensaje.toString());
    }

    // Envía el número de usuarios y el tiempo activo del servidor.
    public void enviarEstadisticas(String tiempoActivo) {

        String mensaje = "STATS|"
                + obtenerNumeroUsuarios()
                + "|"
                + tiempoActivo;

        enviarMensajePublico(mensaje);
    }

    // Envía el último mensaje recibido a todos los clientes.
    public void enviarUltimoMensaje() {

        String mensaje = "LAST|" + obtenerUltimoMensaje();

        enviarMensajePublico(mensaje);
    }
}