package com.cliente;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class ClientePrueba {

    public static void main(String[] args) {

        final String IP = "127.0.0.1";
        final int PUERTO = 5000;

        Scanner teclado = new Scanner(System.in);

        Socket socket = null;
        PrintWriter salida = null;
        BufferedReader entrada = null;

        try {

            boolean conectado = false;

            // Repetimos el proceso hasta conseguir un nombre válido.
            while (!conectado) {

                // Cada intento utiliza una conexión nueva.
                socket = new Socket(IP, PUERTO);

                salida = new PrintWriter(
                        socket.getOutputStream(), true, StandardCharsets.UTF_8);

                entrada = new BufferedReader(
                        new InputStreamReader(
                                socket.getInputStream(), StandardCharsets.UTF_8));

                System.out.print("Introduce tu nombre de usuario: ");
                String nombre = teclado.nextLine().trim();

                if (nombre.isBlank()) {
                    System.out.println("El nombre no puede estar vacío.");
                    socket.close();
                    continue;
                }

                salida.println(nombre);

                String respuesta = entrada.readLine();

                if (respuesta == null) {
                    System.out.println("El servidor ha cerrado la conexión.");
                    socket.close();
                    return;
                }

                if (respuesta.startsWith("ERROR|")) {

                    System.out.println("Error: " + respuesta.substring(6));
                    System.out.println("Prueba con otro nombre.\n");

                    // Cerramos esta conexión rechazada antes de reintentar.
                    socket.close();

                } else if (respuesta.startsWith("OK|")) {

                    System.out.println(respuesta);
                    conectado = true;
                }
            }

            // Hilo independiente para recibir mensajes.
            BufferedReader entradaFinal = entrada;

            Thread receptor = new Thread(() -> {

                try {
                    String mensaje;

                    while ((mensaje = entradaFinal.readLine()) != null) {
                        System.out.println("\n" + mensaje);
                        System.out.print("> ");
                    }

                } catch (IOException e) {
                    System.out.println("Se ha perdido la conexión con el servidor.");
                }
            });

            receptor.start();

            System.out.println("\nComandos disponibles:");
            System.out.println("PUBLIC|mensaje");
            System.out.println("PRIVATE|usuario|mensaje");
            System.out.println("QUIT");

            // Enviamos los mensajes escritos por el usuario.
            while (teclado.hasNextLine()) {

                String mensaje = teclado.nextLine();

                if (mensaje.equalsIgnoreCase("QUIT")) {
                    salida.println("QUIT");
                    break;
                }

                salida.println(mensaje);
            }

        } catch (IOException e) {

            System.out.println("Error de conexión: " + e.getMessage());

        } finally {

            // Cerramos la conexión cuando termina el cliente.
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                System.out.println("Error al cerrar la conexión.");
            }
        }
    }
}
