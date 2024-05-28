public class Client {

    public static void main(String[] args) {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "client.cfg", extraArgs)) {
            FileTransfer.TransferPrx twoway = FileTransfer.TransferPrx.checkedCast(
                    communicator.propertyToProxy("Server.Proxy")
            ).ice_twoway().ice_secure(false);
            FileTransfer.TransferPrx server = twoway.ice_twoway();
            server.negotiateKey("funciona");
        }
    }

}
