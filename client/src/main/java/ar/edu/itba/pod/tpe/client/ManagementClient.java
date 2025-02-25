package ar.edu.itba.pod.tpe.client;

import ar.edu.itba.pod.tpe.client.exceptions.ArgumentException;
import ar.edu.itba.pod.tpe.client.utils.ClientUtils;
import ar.edu.itba.pod.tpe.exceptions.IllegalElectionStateException;
import ar.edu.itba.pod.tpe.interfaces.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Optional;
import java.util.Properties;

public class ManagementClient {

    private static Logger logger = LoggerFactory.getLogger(ManagementClient.class);

    /**
     * Params values and argument error
     */
    private static final String SERVER_ADDRESS_PARAM = "serverAddress";
    private static final String ACTION_PARAM = "action";
    private static final int ERROR_STATUS = 1;

    /**
     * Properties brought from parameters
     */
    private static InetSocketAddress serverAddress;
    private static String action;


    public static void main(String[] args) {
        try {
            argumentParsing();
        } catch (ArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(ERROR_STATUS);
            return;
        }

        try {
            final Registry registry = LocateRegistry.getRegistry(serverAddress.getHostName(), serverAddress.getPort());
            final ManagementService service = (ManagementService) registry.lookup(ManagementService.class.getName());

            switch (action) {
                case "open":
                    System.out.println("Election " + service.open().getMessage());
                    break;
                case "close":
                    System.out.println("Election " + service.close().getMessage());
                    break;
                case "state":
                    System.out.println("Election is " + service.status().getMessage());
                    break;
                default:
                    System.err.println("Invalid action name, possible options: open, close, state");
            }
        } catch (IllegalElectionStateException e) {
            System.err.println("Error trying to " + action + ", " + e.getMessage());
            System.exit(ERROR_STATUS);
        } catch (RemoteException e) {
            System.err.println("Remote communication failed.");
            System.exit(ERROR_STATUS);
        } catch (NotBoundException e) {
            System.err.println("Server " + ManagementService.class.getName() + " has no associated binding.");
            System.exit(ERROR_STATUS);
        }
    }


    /**
     * Ex.
     * -DserverAddress=xx.xx.xx.xx:yyyy     --> host:port
     * -Daction=action                      --> action
     *
     * Parses arguments from terminal
     * @throws ArgumentException
     */
    private static void argumentParsing() throws ArgumentException {

        Properties properties = System.getProperties();

        try {
            serverAddress = ClientUtils.getInetAddress(properties.getProperty(SERVER_ADDRESS_PARAM));
        } catch (URISyntaxException e) {
            throw new ArgumentException("Server Address must be supplied using -DserverAddress and its format must be xx.xx.xx.xx:yyyy");
        }

        action = Optional.ofNullable(properties.getProperty(ACTION_PARAM)).orElseThrow(new ArgumentException("Action name must be supplied using -Daction"));
    }
}
