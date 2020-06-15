package Group2;

import Group2.Agents.*;
import Group9.agent.RandomIntruderAgent;
import Group9.agent.factories.IAgentFactory;
import Interop.Agent.Guard;
import Interop.Agent.Intruder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides common way to build agents for the competition.
 *
 * Sharing knowledge between agents is NOT ALLOWED.
 *
 * For example:
 * Agents must not hold ANY references to common objects or references to each other.
 */
public class AgentsFactory implements IAgentFactory {

    public List<Intruder> createIntruders(int number) {
        ArrayList<Intruder> intruders = new ArrayList<>();
        for(int i=0;i<number;i++){
            Intruder intruder = new Morontruder();
            intruders.add(intruder);
        }
        return intruders;
    }
    public List<Guard> createGuards(int number) {
        ArrayList<Guard> guards = new ArrayList<>();
        for(int i=0;i<number;i++){
            Guard guard = new GuardAgent1(i);
            guards.add(guard);
        }
        return guards;
    }
}