package Group9;

import Group2.GreedyIntruderAgent;
import Group9.agent.factories.DefaultAgentFactory;
import Group9.agent.factories.OurAgentFactory;
import Group9.map.parser.Parser;


public class Main {
    public static boolean SUPPRESS_OUTPUT = true;

    public static void main(String[] args) {
        double x = 0;
        double y = 0;
        double sum = 0;
        final double NUMBER_OF_RUNS = 10;
        for(int i=1; i <= NUMBER_OF_RUNS; i++) {
            Game game = new Game(Parser.parseFile("./src/main/java/Group9/map/maps/configSecond.map"), new OurAgentFactory(), false);
            game.run();
            System.out.printf("The winner is: %s\n", game.getWinner());
            if(game.getWinner() == Game.Team.TIMEOUT){
                i--;
                continue;
            }
            if(game.getWinner() == Game.Team.GUARDS){
                x++;
            }
            if(game.getWinner() == Game.Team.INTRUDERS){
                y++;
            }
            sum = sum + game.getCounter();


        }
        System.out.printf("guard wins : %s\n", (x*10));
        System.out.printf("intruder wins : %s\n", (y*10));
        System.out.printf("average is : %s\n", sum/NUMBER_OF_RUNS);

    }


}
