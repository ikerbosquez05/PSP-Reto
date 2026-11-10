# CHAT MULTIUSUARIO -- PSP

**C.F.G.S. Desarrollo de Aplicaciones Multiplataforma**\
**Módulo:** Programación de Servicios y Procesos\
**Proyecto:** Reto Chat Multiusuario\
**Lenguaje:** Java\
**Interfaz gráfica:** JavaFX\
**Arquitectura:** Cliente-servidor\
**Puerto de comunicación:** 5000

------------------------------------------------------------------------

## 1. Descripción del proyecto

Este proyecto consiste en desarrollar una aplicación de chat
multiusuario utilizando Java y una arquitectura cliente-servidor.

La aplicación permite que varios usuarios se conecten simultáneamente a
un servidor y puedan intercambiar mensajes públicos y privados.

El servidor funciona mediante una aplicación de consola y gestiona las
conexiones utilizando sockets y múltiples hilos de ejecución.

Los clientes disponen de una interfaz gráfica desarrollada con JavaFX
para conectarse y comunicarse con otros usuarios.

## 2. Estructura del proyecto

El proyecto está dividido en dos partes principales:

``` text
ChatMultiusuario/
│
├── Servidor/
│   ├── pom.xml
│   └── src/
│       └── main/
│           └── java/
│               └── com/
│                   └── servidor/
│                       ├── Servidor.java
│                       ├── ClienteHandler.java
│                       ├── GestorUsuarios.java
│                       └── LoggerServidor.java
│
├── Cliente/
│   ├── pom.xml
│   └── src/
│       └── main/
│           ├── java/
│           │   └── com/
│           │       └── cliente/
│           │           ├── App.java
│           │           ├── ClienteSocket.java
│           │           └── PrimaryController.java
│           │
│           └── resources/
│               └── com/
│                   └── cliente/
│                       └── primary.fxml
│
└── README.md
```

## 3. Requisitos para ejecutar la aplicación

Para ejecutar el proyecto se necesita:

-   Java JDK compatible con la versión configurada en Maven.
-   Apache NetBeans o un IDE compatible con proyectos Maven.
-   Apache Maven.
-   JavaFX y sus dependencias configuradas en el proyecto.
-   Conexión de red entre el servidor y los clientes.

## 4. Cómo ejecutar el servidor

1.  Abrir Apache NetBeans.
2.  Seleccionar **File → Open Project**.
3.  Abrir la carpeta `Servidor`.
4.  Esperar a que Maven descargue las dependencias necesarias.
5.  Ejecutar el proyecto.
6.  Comprobar que el servidor se inicia correctamente en el puerto 5000.

El servidor permanece esperando conexiones de los clientes.

## 5. Cómo ejecutar el cliente

1.  Abrir Apache NetBeans.
2.  Seleccionar **File → Open Project**.
3.  Abrir la carpeta `Cliente`.
4.  Esperar a que se carguen las dependencias.
5.  Ejecutar la aplicación JavaFX.
6.  Introducir la dirección IP del servidor.
7.  Introducir el puerto 5000.
8.  Escribir el nombre de usuario.
9.  Pulsar el botón de conexión.

Para ejecutar varios clientes, se puede iniciar la aplicación varias
veces.

Si los clientes están en diferentes ordenadores, deben utilizar la
dirección IP del ordenador donde se ejecuta el servidor.

## 6. Cómo utilizar la aplicación

### 6.1. Conectarse al servidor

El usuario debe introducir la dirección IP, el puerto y su nombre de
usuario.

Después debe pulsar el botón de conexión.

### 6.2. Enviar mensajes públicos

Los mensajes públicos son visibles para todos los usuarios conectados.

El usuario escribe el mensaje en el campo correspondiente y utiliza la
opción de envío público.

### 6.3. Enviar mensajes privados

Los mensajes privados permiten comunicarse directamente con otro
usuario.

El usuario selecciona o indica el destinatario y escribe el mensaje
privado.

Solamente el emisor y el destinatario deben recibir el contenido del
mensaje.

### 6.4. Desconectarse

Para finalizar la conexión, el usuario utiliza el botón de desconexión.

El servidor elimina al usuario de la lista de usuarios conectados y
registra su salida.

## 7. Características técnicas

### Servidor

-   Comunicación mediante sockets TCP.
-   Aceptación de múltiples conexiones.
-   Creación de un hilo independiente para cada cliente.
-   Gestión centralizada de usuarios.
-   Envío de mensajes públicos y privados.
-   Registro de actividad del servidor.
-   Límite máximo previsto de 10 clientes.
-   Monitorización periódica cada 10 segundos.

### Cliente

-   Interfaz gráfica desarrollada con JavaFX.
-   Configuración de IP y puerto.
-   Conexión y desconexión del servidor.
-   Envío y recepción de mensajes.
-   Comunicación pública y privada.
-   Recepción de mensajes mediante un hilo independiente.
-   Actualización segura de la interfaz gráfica mediante
    `Platform.runLater()`.

### Sincronización y concurrencia

Se utiliza la palabra clave `synchronized` para proteger los recursos
compartidos y evitar problemas de concurrencia cuando varios hilos
acceden simultáneamente a la información de los usuarios.

## 8. Registro y monitorización

El servidor registra las acciones importantes mediante un log global.

Se registran eventos como:

-   Inicio del servidor.
-   Entrada de usuarios.
-   Salida de usuarios.
-   Mensajes públicos.
-   Mensajes privados.
-   Errores de comunicación.

Además, cada 10 segundos se muestran en la consola:

-   Número de usuarios conectados.
-   Tiempo de actividad del servidor.
-   Último mensaje recibido.

## 9. Organización y arquitectura

La aplicación utiliza una arquitectura cliente-servidor.

El servidor centraliza la comunicación y administra las conexiones,
mientras que los clientes permiten a los usuarios interactuar mediante
una interfaz gráfica.

La utilización de hilos permite atender a varios usuarios
simultáneamente sin bloquear la comunicación.

## 10. Autores

**Equipo:** Grupo 1 .

**Módulo:** Programación de Servicios y Procesos.

**Ciclo formativo:** CFGS Desarrollo de Aplicaciones Multiplataforma.

**Curso académico:** 2026-2027.
