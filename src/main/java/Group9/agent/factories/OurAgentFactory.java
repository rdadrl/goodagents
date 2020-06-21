package Group9.agent.factories;

import Group2.Agents.GuardAgent1;
import Group2.GreedyIntruderAgent;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;

import java.util.ArrayList;
import java.util.List;

public class OurAgentFactory implements IAgentFactory{
    @Override
    public List<Intruder> createIntruders(int number) {
        List<Intruder> intruders = new ArrayList<>();
        for(int i = 0; i < number; i++)
        {
            intruders.add(new GreedyIntruderAgent(i));
        }
        return intruders;
    }

    public List<Guard> createGuards(int number) {
        List<Guard> guards = new ArrayList<>();
        for(int i = 0; i < number; i++)
        {
            guards.add(new GuardAgent1(i));
            //guards.add(new DeepSpace());
        }
        return guards;
    }
}
