# Encrypted File Transfer (AES-256 + Diffie-Hellman)
This repository consists of two programs, a client and a server. The client sends a file to the server, the file is specified via java arguments.
The file transfer is encrypted using AES-256 with symmetric key negotiated via conventional Diffie-Hellman algorithm.

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
