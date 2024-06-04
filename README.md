## Informe del Proyecto: Transferencia Segura de Archivos con Diffie-Hellman y AES-256

**Integrantes del Equipo:**

* [Juan Fernando Martínez]
* [Diego Hidalgo]
* [Brian Romero]

**Descripción del Proyecto**

Este proyecto implementa un sistema de transferencia segura de archivos entre un cliente y un servidor, utilizando el framework Ice de ZeroC para la comunicación. Se aplican las siguientes tecnologías y algoritmos para garantizar la seguridad y la integridad de los datos:

* **Intercambio de claves Diffie-Hellman (DH):** Este algoritmo nos permite establecer una clave secreta compartida entre el cliente y el servidor a través de un canal inseguro.
* **Cifrado AES-256:** Este cifrado nos permite utilizar la clave secreta negociada con DH para cifrar el archivo antes de la transmisión, asegurando la confidencialidad de los datos.
* **Hash SHA-256:** Se calcula el hash del archivo original y se envía al servidor para verificar que el archivo recibido no ha sido alterado durante la transferencia.

**Implementación**

El proyecto consta de cuatro clases principales:

* **`Client.java`:** Clase principal para el uso del cliente que inicia la conexión con el servidor, negocia la clave DH y envía el archivo cifrado.
* **`DHFileSender.java`:** Maneja la negociación de claves DH en el lado del cliente, el cifrado AES y el envío del archivo.
* **`DHFileTransfer.java`:** Una implementación de la interfaz `SymmetricKeyFileTransfer` de Ice, que nos permite definir las operaciones para la negociación de claves, recepción del archivo y verificación del hash en el lado del servidor.
* **`Server.java`:** Clase principal del para el servidor que inicia el servicio Ice y espera conexiones de los clientes.

**Flujo del Programa**

1. El servidor inicia y escucha por el puerto definido en el archivo de configuración.
2. El cliente inicia la conexión con el servidor por el puerto definido.
2. El cliente y el servidor negocian una clave secreta compartida utilizando DH.
3. El cliente cifra el archivo con AES-256 utilizando la clave secreta.
4. El cliente envía el archivo cifrado.
5. El servidor recibe el archivo, lo descifa y notifica al cliente si lo recibió exitosamente o hubo algun error.
6. El cliente envía el hash SHA-256 del archivo enviado al servidor.
7. El servidor calcula el hash SHA-256 del archivo recibido y lo compara con el hash enviado por el cliente.
8. El servidor informa al cliente si los hashes coinciden, confirmando la transferencia exitosa y finalizando la comunicación.

**Dificultades y Soluciones**

* **Configuración de Ice:** Tuvimos algunos problemas iniciales para configurar correctamente el entorno Ice y los archivos de configuración del cliente y servidor. Investigamos en la documentación de ZeroC y en línea para resolverlos.
* **Implementación de DH:** La implementación del intercambio de claves DH requirió estudiar nuevamente los conceptos de criptografía para comprender de manera más profunda lo que necesitabamos para utilizar correctamente las clases de Java para generar e intercambiar claves.

**Conclusiones**

En este proyecto logramos implementar un sistema de transferencia de archivos seguro que utiliza técnicas criptográficas robustas para proteger la confidencialidad y la integridad de los datos. Con esta implementación conseguimos un reforzar nuestros conocimientos en criptográfia, además nos permitio practicar y aprender nuevas cosas sobre el framework Ice de ZeroC, el intercambio de claves Diffie-Hellman y el cifrado AES-256.


## How to run the program

## Requirements
- **Java 17**
- **Gradle 7.2** 
- **ZeroC Ice 3.7.10** Can be downloaded from their web page here: [Download Ice](https://zeroc.com/ice/downloads/3.7)

## Execution

You can run the programs by following the next instructions.

### Local
After making sure that you installed the previous requirements,
do the following on the project root:

1. Build the project
`./gradlew build`
2. Run the server
`java -jar ./server/build/libs/server.jar`
3. Run the client with the file you want to send as argument
`java -jar ./client/build/libs/client.jar /path/to/file.txt`

### Remote

1. Install the requirements on client and server machines.
2. Configure the client and server.

   a. Modify the file `client/src/main/resources/client.cfg` replacing according to your server machine:
     ```
        Server.Proxy=SimpleServer:tcp -p SERVER_AVAILABLE_PORT
        Ice.Default.Host=YOUR_SERVER_IP_ADDRESS
     ```
   b. Modify the file `server/src/main/resources/server.cfg` replacing according to your server machine and previous step
     ```
        FileAdapter.Endpoints=tcp -p SERVER_AVAILABLE_PORT
        Ice.Default.Host=YOUR_SERVER_IP_ADDRESS
     ``` 
3. Copy the project to your client and server machines.

On the server at the project root:

4. Build the project
`./gradlew build`

5. Run the server
`java -jar ./server/build/libs/server.jar`

On the client at the project root:

6. Build the project
`./gradlew build`

7. Run the client with the file you want to send as argument
`java -jar ./client/build/libs/client.jar /path/to/file.txt`
