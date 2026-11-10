package com.servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Servidor {

    private static final int PUERTO = 5000;

    private static GestorUsuarios gestor = new GestorUsuarios();

    // Momento en el que se inicia el servidor.
    private static final long tiempoInicio = System.currentTimeMillis();

    public static void main(String[] args) {

        try (ServerSocket servidor = new ServerSocket(PUERTO)) {

            // Registramos el inicio del servidor.
            LoggerServidor.registrar(
                "Servidor iniciado en el puerto " + PUERTO
            );

            // Iniciamos el hilo de monitorización.
            iniciarMonitorizacion();

            // Bucle para aceptar conexiones continuamente.
            while (true) {

                Socket socket = servidor.accept();

                // Registramos la conexión recibida.
                LoggerServidor.registrar(
                    "Nueva conexión recibida desde "
                    + socket.getInetAddress().getHostAddress()
                );

                // Creamos un gestor independiente para cada cliente.
                ClienteHandler cliente = new ClienteHandler(socket, gestor);

                // Iniciamos un hilo para atender al cliente.
                Thread hilo = new Thread(cliente);
                hilo.start();
            }

        } catch (IOException e) {

            LoggerServidor.registrar(
                "Error en el servidor: " + e.getMessage()
            );
        }
    }

    // Hilo que monitoriza el estado del servidor.
    private static void iniciarMonitorizacion() {

        Thread monitor = new Thread(() -> {

            while (true) {

                try {

                    Thread.sleep(10000);

                    // Calculamos el tiempo que lleva activo el servidor.
                    long tiempoActivo =
                        System.currentTimeMillis() - tiempoInicio;

                    long segundos = tiempoActivo / 1000;

                    long horas = segundos / 3600;
                    long minutos = (segundos % 3600) / 60;
                    long segundosRestantes = segundos % 60;

                    String tiempoFormateado = String.format(
                        "%02d:%02d:%02d",
                        horas,
                        minutos,
                        segundosRestantes
                    );

                    // Mostramos información del servidor.
                    System.out.println("--------------------------------");
                    System.out.println(
                        "Usuarios conectados: "
                        + gestor.obtenerNumeroUsuarios()
                    );
                    System.out.println(
                        "Tiempo activo: " + tiempoFormateado
                    );
                    System.out.println(
                        "Último mensaje: " + gestor.obtenerUltimoMensaje()
                    );
                    System.out.println("--------------------------------");

                    // Enviamos las estadísticas a los clientes.
                    gestor.enviarEstadisticas(tiempoFormateado);
                    gestor.enviarUltimoMensaje();

                } catch (InterruptedException e) {

                    LoggerServidor.registrar(
                        "Se ha interrumpido la monitorización del servidor."
                    );

                    Thread.currentThread().interrupt();
                    break;
                }
            }

        });

        // El monitor no impedirá cerrar el programa.
        monitor.setDaemon(true);
        monitor.start();
    }
}
