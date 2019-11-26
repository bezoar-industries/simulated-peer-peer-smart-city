package cs555.chiba.registry;

import cs555.chiba.util.Utilities;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AutomatedRegistryNode extends RegistryNode {

    private static final Logger logger = Logger.getLogger(AutomatedRegistryNode.class.getName());

    public AutomatedRegistryNode(int port) {
        super(port);
    }

    public static void main(String[] args) {
        try {
            AutomatedRegistryNode node = parseArguments(args);
            int minConnections = Utilities.parsePositiveIntFromArg("min-connections", args[1]);
            int maxConnections = Utilities.parsePositiveIntFromArg("max-connections", args[2]);
            automatedStartup(node.getPort(), node, RegistryCommands.getRegistryCommands(node), minConnections, maxConnections, args[3]);
        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Startup failed", e);
        }
    }

    private static AutomatedRegistryNode parseArguments(String[] args) {
        if (!Utilities.checkArgCount(4, args)) {
            throw new IllegalArgumentException("Automated Registry Node requires 3 arguments:  port-num min-connections max-connections output-filename");
        }

        int port = Utilities.parsePort(args[0]);
        return new AutomatedRegistryNode(port);
    }

}
