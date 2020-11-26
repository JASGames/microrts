/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

/* command lines for compile and run:
javac -cp "lib/*;src" -d bin src/rts/MicroRTS.java
javac -cp "lib/*;src" -d bin src/tests/GameEvolutionOnce.java
java -cp "lib/*;bin" tests.GameEvolutionOnce
NB uses task_allocator.py
 */
import ai.core.AI;
import ai.abstraction.ChromoBot;
import ai.abstraction.HeavyDefense;
import ai.abstraction.pathfinding.NewStarPathFinding;
import gui.PhysicalGameStatePanel;
import java.util.*;
import java.io.*;
import javax.swing.JFrame;

import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.Trace;
import rts.TraceEntry;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author ben & robert, based on "GameVisualSimulationTest.java" by santi
 * incorporates code from Applied IT Project 2018
 * by Matthew Burr, Justin Homsi as students at Edith Cowan University
 */
public class GameEvolutionOnce { // NB exclude was "**/*.java,**/*.form"

    private static PhysicalGameState QuickDeploy(PhysicalGameState pgs, UnitTypeTable utt, int[][] chromo) {
        String[] unit_types = {"Light", "Heavy", "Ranged"};
        int x = 0; // Positions in the map
        int y = pgs.getHeight() - 2;

        for (int goal = 0; goal < 4; goal++)
        {
            int sofar = 0; // Offset of unit id within goal
            for (int utype = 0; utype < 3; utype++)
            {
                for (int gene = 0; gene < chromo[goal][utype]; gene++)
                {
                    // Add the chromosome to the board (with right id for its goal)
                    pgs.addUnit(new Unit(1000 * (1 + goal) + sofar, 0, utt.getUnitType(unit_types[utype]), x, y, 5));
                    sofar++;

                    // Set next x & y coords
                    if (y == (pgs.getHeight() - 1))
                    {
                        y = pgs.getHeight() - x - 2;
                        x = 0;
                    }
                    else
                    {
                        x++;
                        y++;
                    }
                }
            }
        }
        return pgs;
    }

    public static void main(String[] args) throws Exception {
        UnitTypeTable utt = new UnitTypeTable();
        String map = args[0];
        int MAXCYCLES = Integer.parseInt(args[1]);
        Boolean display = false; // Display battle?
        Boolean shouldTrace = false;
        String traceChromo = "";
        if (args.length > 2 && "display".equals(args[2])) {
            display = true;
        }else if(args.length > 2){
            shouldTrace = true;
            traceChromo = args[2];
        }

        if(!shouldTrace) //Read chromosomes from file
        {
            // Open input file and buffer
            BufferedReader buffrd = new BufferedReader(new InputStreamReader(new FileInputStream("chromosomes.txt")));

            // Store the results before writting them
            ArrayList results = new ArrayList();

            String chromline; // Line to read in each chromosome
            // Loop through input file, & run battle collecting results
            while ((chromline = buffrd.readLine()) != null) {
                // Parse current chromosome
                int[][] chromosome = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
                String[] chromstrs;
                chromstrs = chromline.split(" ");
                for (int ii = 0; ii < 4; ii++) {
                    for (int jj = 0; jj < 3; jj++) {
                        chromosome[ii][jj] = Integer.parseInt(chromstrs[ii * 3 + jj]);
                    }
                }

                // Setup game conditions
                PhysicalGameState pgs = PhysicalGameState.load(map, utt); // TwoBasesBarracks16x16.xml", utt);
                GameState gs = new GameState(pgs, utt);
                //int MAXCYCLES = 1000; // Maximum game length
                int PERIOD = 15; // Refresh rate for display (milliseconds)
                boolean gameover = false;

                // Set the AIs
                AI ai1 = new ChromoBot(utt, new NewStarPathFinding());
                AI ai2 = new HeavyDefense(utt, new NewStarPathFinding());

                QuickDeploy(pgs, utt, chromosome);

                // Create a trace for saving the game
                Trace trace = new Trace(utt);
                TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                trace.addEntry(te);

                if (display) { // Slower version, displays the battle
                    JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);

                    long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
                    do {
                        if (System.currentTimeMillis() >= nextTimeToUpdate) {
                            PlayerAction pa1 = ai1.getAction(0, gs);
                            PlayerAction pa2 = ai2.getAction(1, gs);

                            // Create a new trace entry
                            te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                            te.addPlayerAction(pa1.clone());
                            te.addPlayerAction(pa2.clone());
                            trace.addEntry(te);

                            gs.issueSafe(pa1);
                            gs.issueSafe(pa2);

                            // simulate:
                            gameover = gs.cycle();
                            w.repaint();
                            nextTimeToUpdate += PERIOD;
                        } else {
                            try {
                                Thread.sleep(1);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } while (!gameover && gs.getTime() < MAXCYCLES);
                    w.dispose();
                } else { // Faster, headless version
                    do { // TODO move 'trace' here & have a poper save option?
                        PlayerAction pa1 = ai1.getAction(0, gs);
                        PlayerAction pa2 = ai2.getAction(1, gs);
                        gs.issueSafe(pa1);
                        gs.issueSafe(pa2);

                        // simulate:
                        gameover = gs.cycle();
                    } while (!gameover && gs.getTime() < MAXCYCLES);
                }

                ai1.gameOver(gs.winner()); // TODO what do these do?
                ai2.gameOver(gs.winner());

                // Finish up game trace & save it if 'display' is set
                te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                trace.addEntry(te);

                if (display) // Save the trace file
                {
                    //Creating a File object for directory
                    File directoryPath = new File("traces");
                    //List of all files and directories
                    String contents[] = directoryPath.list();
                    int num = contents.length;

                    // Write the file to the newest name
                    XMLWriter xml = new XMLWriter(new FileWriter("traces/game_" + String.valueOf(num) + ".xml"));
                    trace.toxml(xml);
                    xml.flush();
                    xml.close();
                }

                // Resuts of game: blue units, red units, blue bases, red bases & time
                int[] counts = {0, 0, 0, 0, 0};
                for (Unit u : gs.getUnits()) { // TODO needs testing
                    if (u.getType() == utt.getUnitType("Light")
                            || u.getType() == utt.getUnitType("Heavy")
                            || u.getType() == utt.getUnitType("Ranged")) {
                        counts[u.getPlayer()]++;
                    } else if (u.getType() == utt.getUnitType("Base")) {
                        counts[u.getPlayer() + 2]++;
                    }
                }
                counts[4] = gs.getTime();

                System.out.println("Game Over: " + counts[0] + " " + counts[1] + " " + counts[2] + " " + counts[3] + " " + counts[4]);
                int lightUnits = chromosome[0][0] + chromosome[1][0] + chromosome[2][0] + chromosome[3][0];
                int heavyUnits = chromosome[0][1] + chromosome[1][1] + chromosome[2][1] + chromosome[3][1];
                int rangedUnits = chromosome[0][2] + chromosome[1][2] + chromosome[2][2] + chromosome[3][2];
                int totalUnits = lightUnits + heavyUnits + rangedUnits;
                System.out.println("Light: "+lightUnits+" Heavy: "+heavyUnits+" Ranged: "+rangedUnits+" Total:"+totalUnits);

                results.add(counts);
            }

            // Input file finished with
            buffrd.close();

            // Open output file and write it
            BufferedWriter buffwr = new BufferedWriter(new FileWriter("scores.txt"));

            Iterator iter = results.iterator();
            while (iter.hasNext()) {
                String count_str = "";
                for (int count : (int[]) iter.next()) {
                    count_str += String.valueOf(count) + " ";
                }
                count_str += "\n";
                buffwr.write(count_str);
            }

            // Output file finished with
            buffwr.close();
        } 
        else // Trace the passed in chromosomes
        {
            int[][] chromosome = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
            String[] chromstrs;
            chromstrs = traceChromo.split(" ");
            for (int ii = 0; ii < 4; ii++) {
                for (int jj = 0; jj < 3; jj++) {
                    chromosome[ii][jj] = Integer.parseInt(chromstrs[ii * 3 + jj]);
                }
            }

            // Setup game conditions
            PhysicalGameState pgs = PhysicalGameState.load(map, utt); // TwoBasesBarracks16x16.xml", utt);
            GameState gs = new GameState(pgs, utt);
            //int MAXCYCLES = 1000; // Maximum game length
            int PERIOD = 15; // Refresh rate for display (milliseconds)
            boolean gameover = false;

            // Set the AIs
            AI ai1 = new ChromoBot(utt, new NewStarPathFinding());
            AI ai2 = new HeavyDefense(utt, new NewStarPathFinding());

            QuickDeploy(pgs, utt, chromosome);

            // Create a trace for saving the game
            Trace trace = new Trace(utt);
            TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
            trace.addEntry(te);

             // Faster, headless version
            do { // TODO move 'trace' here & have a poper save option?
                PlayerAction pa1 = ai1.getAction(0, gs);
                PlayerAction pa2 = ai2.getAction(1, gs);

                // Create a new trace entry
                te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                te.addPlayerAction(pa1.clone());
                te.addPlayerAction(pa2.clone());
                trace.addEntry(te);

                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
            } while (!gameover && gs.getTime() < MAXCYCLES);

            ai1.gameOver(gs.winner()); // TODO what do these do?
            ai2.gameOver(gs.winner());

            // Finish up game trace & save it if 'display' is set
            te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
            trace.addEntry(te);

            //Creating a File object for directory
            File directoryPath = new File("traces");
            //List of all files and directories
            String contents[] = directoryPath.list();
            int num = contents.length;

            // Write the file to the newest name
            XMLWriter xml = new XMLWriter(new FileWriter("traces/game_" + String.valueOf(num) + ".xml"));
            trace.toxml(xml);
            xml.flush();
            xml.close();
        }
    }
}
