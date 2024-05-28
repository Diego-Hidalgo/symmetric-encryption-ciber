public class Server
{
    public static void main(String[] args)
    {
        java.util.List<String> extraArgs = new java.util.ArrayList<>();
        try(com.zeroc.Ice.Communicator communicator = com.zeroc.Ice.Util.initialize(args, "server.cfg", extraArgs))
        {
            com.zeroc.Ice.ObjectAdapter adapter = communicator.createObjectAdapter("FileAdapter");
            com.zeroc.Ice.Object object = new TransferI();
            adapter.add(object, com.zeroc.Ice.Util.stringToIdentity("SimpleServer"));
            adapter.activate();
            adapter.waitForDeactivate();
        }
    }
}
