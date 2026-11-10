package com.servidor;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerServidor {

    // Nombre del archivo donde guardaremos la actividad.
    private static final String ARCHIVO_LOG = "servidor.log";

    // Formato de fecha y hora para cada registro.
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // synchronized evita que varios hilos escriban a la vez.
    public static synchronized void registrar(String mensaje) {

        // Obtenemos la fecha y hora actuales.
        String fecha = LocalDateTime.now().format(FORMATO);

        // Construimos la línea que guardaremos.
        String registro = "[" + fecha + "] " + mensaje;

        // Mostramos también el registro en la consola.
        System.out.println(registro);

        // Abrimos el archivo en modo añadir (append).
        try (PrintWriter escritor =
                new PrintWriter(new FileWriter(ARCHIVO_LOG, true))) {

            // Añadimos una nueva línea al archivo.
            escritor.println(registro);

        } catch (IOException e) {

            // Informamos si no se puede escribir en el archivo.
            System.err.println("Error al escribir el log: " + e.getMessage());
        }
    }
}
