//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package Group9;

import Group9.agent.factories.DefaultAgentFactory;
import Group9.agent.factories.IAgentFactory;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;
import java.util.List;

public class AgentsFactory {
    private static final IAgentFactory agentFactory = new DefaultAgentFactory();

    public AgentsFactory() {
    }

    public static List<Intruder> createIntruders(int number) {
        return agentFactory.createIntruders(number);
    }

    public static List<Guard> createGuards(int number) {
        return agentFactory.createGuards(number);
    }
}
