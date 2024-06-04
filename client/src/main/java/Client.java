import java.util.ArrayList;
import java.util.List;

public class Client {

    public static void main(String[] args) {
        List<String> extraArgs = new ArrayList<>();
        try (com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "client.cfg", extraArgs)) {
            FileTransfer.SymmetricKeyFileTransferPrx proxy = FileTransfer.SymmetricKeyFileTransferPrx.checkedCast(
                            communicator.propertyToProxy("Server.Proxy"))
                    .ice_twoway().ice_secure(false);

            DHFileSender fileSender = new DHFileSender(proxy);
            fileSender.negotiateKey();
            fileSender.sendFile(args[0]);
        }
    }
}
