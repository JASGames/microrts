 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.abstraction.Attack;
import ai.abstraction.DefendBase;
import ai.abstraction.LightRush;
import ai.abstraction.pathfinding.NewStarPathFinding;
import ai.core.AI;
import ai.*;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import gui.PhysicalGameStatePanel;

import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;

 /**
 *
 * @author santi
 */
public class GameVisualSimulationTest {
    public static void main(String[] args) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        PhysicalGameState pgs = PhysicalGameState.load("maps/BigBoi.xml", utt);
//        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

        GameState gs = new GameState(pgs, utt);
        int MAXCYCLES = 5000;
        int PERIOD = 2;
        boolean gameover = false;

        NewStarPathFinding pf = new NewStarPathFinding();
        AI ai1 = new LightRush(utt, pf);
        AI ai2 = new LightRush(utt, new NewStarPathFinding());

        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
//        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

        long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
        do{
            if (System.currentTimeMillis()>=nextTimeToUpdate) {
                PlayerAction pa1 = ai1.getAction(0, gs);
                PlayerAction pa2 = ai2.getAction(1, gs);
                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
                w.repaint();
                nextTimeToUpdate+=PERIOD;
            } else {
                try {
                    Thread.sleep(1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }while(!gameover && gs.getTime()<MAXCYCLES);
        ai1.gameOver(gs.winner());
        ai2.gameOver(gs.winner());
        
        System.out.println("Game Over");
        System.out.println(pf.iterations);
        System.out.println(pf.calcPath);
        System.out.println(pf.usedPrevPath);
        System.out.println((float)pf.usedPrevPath/(pf.calcPath+pf.usedPrevPath) * 100.0f);

    }    
}
