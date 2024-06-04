# Encrypted File Transfer (AES-256 + Diffie-Hellman)
This repository consists of two programs, a client and a server. The client sends a file to the server, the file is specified via java arguments.
The file transfer is encrypted using AES-256 with symmetric key negotiated via conventional Diffie-Hellman algorithm.

## Requirements
- **Java 17**
- **Gradle 7.2** 
- **ZeroC Ice 3.7.10** Can be downloaded from their web page here: [Download Ice](https://zeroc.com/ice/downloads/3.7)

## Execution
After making sure that you installed the previours requirements,
you can run the programs locally by executing the following instructions on the project root.

1. Build the project
`./gradlew build`
2. Run the server
`java -jar ./server/build/libs/server.jar`
4. Run the client with the file you want to send as argument
`java -jar ./client/build/libs/client.jar enviar.txt`
