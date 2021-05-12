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
import ai.abstraction.DefendBase;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.abstraction.ChromoBot;
import ai.abstraction.HeavyDefense;
import ai.abstraction.pathfinding.NewStarPathFinding;
import gui.PhysicalGameStatePanel;
import java.util.*;
import java.io.*;
import javax.swing.JFrame;

//import javafx.scene.effect.Light;
//import jdk.nashorn.internal.ir.debug.JSONWriter;
import gui.TraceDialogWindow;
import org.w3c.dom.ranges.Range;
import rts.*;
import rts.units.Unit;
import rts.units.UnitTypeTable;
//import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import util.XMLWriter;

/**
 *
 * @author ben & robert, based on "GameVisualSimulationTest.java" by santi
 * incorporates code from Applied IT Project 2018
 * by Matthew Burr, Justin Homsi as students at Edith Cowan University
 */
public class GameEvolutionOnce { // NB exclude was "**/*.java,**/*.form"
    public static HashMap<String, ChromoBot.ChromoGoal> UnitTotals = new HashMap();

    private static PhysicalGameState QuickDeploy(PhysicalGameState pgs, UnitTypeTable utt, float[][] priorities, int light, int heavy, int ranged) {
        String[] unit_types = {"Light", "Heavy", "Ranged"};
        int x = 0; // Positions in the map
        int y = pgs.getHeight() - 2;

        int p = 0;

        int[][] units = new int[ChromoBot.ChromoGoal.values().length][unit_types.length];

        // Defend Blue -> Attack Red 1 -> Attack Red 2 -> Attack Red 3
        float lightSum = priorities[p][0] + priorities[p][3] + priorities[p][6] + priorities[p][9];
        float heavySum = priorities[p][1] + priorities[p][4] + priorities[p][7] + priorities[p][10];
        float rangedSum = priorities[p][2] + priorities[p][5] + priorities[p][8] + priorities[p][11];
        /*System.out.println("Light: "+light);
        System.out.println("Heavy: "+heavy);
        System.out.println("Ranged: "+ranged);

        System.out.println("Light Sum: "+lightSum);
        System.out.println("Heavy Sum: "+heavySum);
        System.out.println("Ranged Sum: "+rangedSum);*/

        int lblueDef = (int)Math.floor(light * priorities[p][0] / lightSum);
        int lredAtk1 = (int)Math.floor(light * priorities[p][3] / lightSum);
        int lredAtk2 = (int)Math.floor(light * priorities[p][6] / lightSum);
        int lredAtk3 = (int)Math.floor(light * priorities[p][9] / lightSum);

        int lightLeft = light - (lblueDef + lredAtk1 + lredAtk2 + lredAtk3);
        //System.out.println("Light Left: "+lightLeft);
        //System.out.println("Light Placed: "+(lblueDef + lredAtk1 + lredAtk2 + lredAtk3));
        List<Float> lightRemainders = new ArrayList();
        lightRemainders.add(((light * priorities[p][0] / lightSum) - lblueDef)+lightLeft);
        lightRemainders.add(((light * priorities[p][3] / lightSum) - lredAtk1)+lightLeft);
        lightRemainders.add(((light * priorities[p][6] / lightSum) - lredAtk2)+lightLeft);
        lightRemainders.add(((light * priorities[p][9] / lightSum) - lredAtk3)+lightLeft);


        for(int i = 0; i < lightLeft; i++){
            float highest = 0;
            int index = 0;
            for(int r = 0; r < lightRemainders.size(); r++){
                if(lightRemainders.get(r) > highest){
                    index = r;
                    highest = lightRemainders.get(r);
                }
            }

            lightRemainders.set(index,highest-1.0f);

            switch(index){
                case 0: lblueDef += 1; break;
                case 1: lredAtk1 += 1; break;
                case 2: lredAtk2 += 1; break;
                case 3: lredAtk3 += 1; break;
            }
        }

        //System.out.println("Light Placed After Remainder: "+(lblueDef + lredAtk1 + lredAtk2 + lredAtk3));

        units[0][0] = lblueDef;
        units[1][0] = lredAtk1;
        units[2][0] = lredAtk2;
        units[3][0] = lredAtk3;

        int hblueDef = (int)Math.floor(heavy * priorities[p][1] / heavySum);
        int hredAtk1 = (int)Math.floor(heavy * priorities[p][4] / heavySum);
        int hredAtk2 = (int)Math.floor(heavy * priorities[p][7] / heavySum);
        int hredAtk3 = (int)Math.floor(heavy * priorities[p][10] / heavySum);

        int heavyLeft = heavy - (hblueDef + hredAtk1 + hredAtk2 + hredAtk3);
        //System.out.println("Heavy Left: "+heavyLeft);
        //System.out.println("Heavy Placed: "+(hblueDef + hredAtk1 + hredAtk2 + hredAtk3));
        List<Float> heavyRemainders = new ArrayList();
        heavyRemainders.add(((heavy * priorities[p][1] / heavySum) - hblueDef)+heavyLeft);
        heavyRemainders.add(((heavy * priorities[p][4] / heavySum) - hredAtk1)+heavyLeft);
        heavyRemainders.add(((heavy * priorities[p][7] / heavySum) - hredAtk2)+heavyLeft);
        heavyRemainders.add(((heavy * priorities[p][10] / heavySum) - hredAtk3)+heavyLeft);


        for(int i = 0; i < heavyLeft; i++){
            float highest = 0;
            int index = 0;
            for(int r = 0; r < heavyRemainders.size(); r++){
                if(heavyRemainders.get(r) > highest){
                    index = r;
                    highest = heavyRemainders.get(r);
                }
            }

            heavyRemainders.set(index,highest-1.0f);

            switch(index){
                case 0: hblueDef += 1; break;
                case 1: hredAtk1 += 1; break;
                case 2: hredAtk2 += 1; break;
                case 3: hredAtk3 += 1; break;
            }
        }

        //System.out.println("Heavy Placed After Remainder: "+(hblueDef + hredAtk1 + hredAtk2 + hredAtk3));

        units[0][1] = hblueDef;
        units[1][1] = hredAtk1;
        units[2][1] = hredAtk2;
        units[3][1] = hredAtk3;

        int rblueDef = (int)Math.floor(ranged * priorities[p][2] / rangedSum);
        int rredAtk1 = (int)Math.floor(ranged * priorities[p][5] / rangedSum);
        int rredAtk2 = (int)Math.floor(ranged * priorities[p][8] / rangedSum);
        int rredAtk3 = (int)Math.floor(ranged * priorities[p][11] / rangedSum);

        int rangedLeft = ranged - (rblueDef + rredAtk1 + rredAtk2 + rredAtk3);
        //System.out.println("Ranged Left: "+rangedLeft);
        //System.out.println("Ranged Placed: "+(rblueDef + rredAtk1 + rredAtk2 + rredAtk3));
        List<Float> rangedRemainders = new ArrayList();
        rangedRemainders.add(((ranged * priorities[p][2] / rangedSum) - rblueDef)+rangedLeft);
        rangedRemainders.add(((ranged * priorities[p][5] / rangedSum) - rredAtk1)+rangedLeft);
        rangedRemainders.add(((ranged * priorities[p][8] / rangedSum) - rredAtk2)+rangedLeft);
        rangedRemainders.add(((ranged * priorities[p][11] / rangedSum) - rredAtk3)+rangedLeft);


        for(int i = 0; i < rangedLeft; i++){
            float highest = 0;
            int index = 0;
            for(int r = 0; r < rangedRemainders.size(); r++){
                if(rangedRemainders.get(r) > highest){
                    index = r;
                    highest = rangedRemainders.get(r);
                }
            }

            rangedRemainders.set(index,highest-1.0f);

            switch(index){
                case 0: rblueDef += 1; break;
                case 1: rredAtk1 += 1; break;
                case 2: rredAtk2 += 1; break;
                case 3: rredAtk3 += 1; break;
            }
        }

        //System.out.println("Ranged Placed After Remainder: "+(rblueDef + rredAtk1 + rredAtk2 + rredAtk3));

        units[0][2] = rblueDef;
        units[1][2] = rredAtk1;
        units[2][2] = rredAtk2;
        units[3][2] = rredAtk3;

        //System.out.println("Light: "+light+" Sum: "+lightSum+" Allocation: "+lblueDef+" "+lredAtk1+" "+lredAtk2+" "+lredAtk3);
        //System.out.println("Heavy: "+heavy+" Sum: "+heavySum+" Allocation: "+hblueDef+" "+hredAtk1+" "+hredAtk2+" "+hredAtk3);
        //System.out.println("Ranged: "+ranged+" Sum: "+rangedSum+" Allocation: "+rblueDef+" "+rredAtk1+" "+rredAtk2+" "+rredAtk3);

        int sofar = 0;
        for (int goal = 0; goal < ChromoBot.ChromoGoal.values().length; goal++)
        {
            for (int utype = 0; utype < unit_types.length; utype++)
            {
                for (int gene = 0; gene < units[goal][utype]; gene++)
                {
                    // Add the chromosome to the board (with right id for its goal)
                    pgs.addUnit(new Unit(1000+ sofar, 0, utt.getUnitType(unit_types[utype]), x, y, 5));
                    UnitTotals.put(String.valueOf(1000+sofar), ChromoBot.ChromoGoal.values()[goal]);

                    //System.out.println("Unit: "+(1000+sofar)+" "+ChromoBot.ChromoGoal.values()[goal].toString());

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
        UnitTypeTable utt = new UnitTypeTable(UnitTypeTable.VERSION_ORIGINAL, UnitTypeTable.MOVE_CONFLICT_RESOLUTION_CANCEL_ALTERNATING);

        int batch = Integer.parseInt(args[0]);
        String map = args[1];
        int MAXCYCLES = Integer.parseInt(args[2]);
        Boolean display = false; // Display battle?
        Boolean shouldTrace = false;
        String traceChromo = "";
        int lightUnits = Integer.parseInt(args[3]);
        int heavyUnits = Integer.parseInt(args[4]);
        int rangedUnits = Integer.parseInt(args[5]);
        if (args.length > 6 && "display".equals(args[6])) {
            display = true;
        }else if(args.length > 6){
            shouldTrace = true;
            traceChromo = args[6];
        }

        if(!shouldTrace) //Read chromosomes from file
        {
            // Open input file and buffer
            BufferedReader buffrd = new BufferedReader(new InputStreamReader(new FileInputStream("chromosomes"+batch+".txt")));

            // Store the results before writting them
            ArrayList results = new ArrayList();

            String chromline; // Line to read in each chromosome
            // Loop through input file, & run battle collecting results
            while ((chromline = buffrd.readLine()) != null) {
                // Parse current chromosome
                String[] chromstrs;
                chromstrs = chromline.split(" ");

                int currentPhase = 1;
                int phases = 1 +((chromstrs.length - 12) / 12 / 4);
                int light = lightUnits;
                int heavy = heavyUnits;
                int ranged = rangedUnits;

                float[][] priorities = new float[phases][48];
                System.out.println("Light: "+light+" Heavy: "+heavy+" Ranged: "+ranged+" Phases: "+phases);

                for (int p = 0; p < phases; p++) {
                    if(p == 0) {
                        for (int i = 0; i < 12; i++) {
                            priorities[p][i] = Float.parseFloat(chromstrs[i]);
                            //System.out.print(priorities[p][i] + " ");
                        }
                    } else {
                        for (int i = 0; i < 48; i++) {
                            priorities[p][i] = Float.parseFloat(chromstrs[i + 12 + ((p-1) * 48)]);
                            //System.out.print(priorities[p][i] + " ");
                        }
                    }
                    //System.out.println();
                }

                // Setup game conditions
                PhysicalGameState pgs = PhysicalGameState.load(map, utt); // TwoBasesBarracks16x16.xml", utt);
                GameState gs = new GameState(pgs, utt);

                for(Unit u: pgs.getUnits()){
                    if(u.getType().name == "Light"){
                        u.setHitPoints(8);
                    } else if(u.getType().name == "Heavy"){
                        u.setHitPoints(8);
                    } else if(u.getType().name == "Ranged"){
                        u.setHitPoints(2);
                    }
                }

                //int MAXCYCLES = 1000; // Maximum game length
                int PERIOD = 15; // Refresh rate for display (milliseconds)
                boolean gameover = false;

                QuickDeploy(pgs, utt, priorities, light, heavy, ranged);

                HashMap<ChromoBot.ChromoGoal, int[]> baseLocations = new HashMap();
                for(Unit u : pgs.getUnits()){
                    if(u.getType().name  == "Base"){
                        switch (String.valueOf(u.getID())){
                            case "10": baseLocations.put(ChromoBot.ChromoGoal.AttackRed1, new int[]{ u.getX(), u.getY() }); break;
                            case "30": baseLocations.put(ChromoBot.ChromoGoal.AttackRed2, new int[]{ u.getX(), u.getY() }); break;
                            case "40": baseLocations.put(ChromoBot.ChromoGoal.AttackRed3, new int[]{ u.getX(), u.getY() }); break;
                            case "20": baseLocations.put(ChromoBot.ChromoGoal.DefendBlue, new int[]{ u.getX(), u.getY() }); break;
                        }
                    }
                }

                // Set the AIs
                NewStarPathFinding new1 = new NewStarPathFinding();
                AI ai1 = new ChromoBot(utt,  new1, UnitTotals, baseLocations);
                AI ai2 = new DefendBase(utt, new1);

                // Create a trace for saving the game
                Trace trace = new Trace(utt);
                TraceEntry te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                trace.addEntry(te);


                if (display) { // Slower version, displays the battle
                    //new traceDialogWindow();

                    //JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 640, 640, false, PhysicalGameStatePanel.COLORSCHEME_BLACK);
                    //Thread.sleep(3000);
                    do {

                        if(gs.getTime() > currentPhase*MAXCYCLES/phases && phases > 1){
                            HashMap<ChromoBot.ChromoGoal, List<String>> lightGoals = new HashMap();
                            HashMap<ChromoBot.ChromoGoal, List<String>> heavyGoals = new HashMap();
                            HashMap<ChromoBot.ChromoGoal, List<String>> rangedGoals = new HashMap();

                            int[][] unitTotals = new int[4][3];

                            for(Unit u : gs.getUnits()){
                                ChromoBot.ChromoGoal unitGoal = UnitTotals.get(String.valueOf(u.getID()));

                                if(unitGoal != null){
                                    switch (u.getType().name){
                                        case "Light":
                                            unitTotals[unitGoal.ordinal()][0] += 1;
                                            if(lightGoals.containsKey(unitGoal)){
                                                lightGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                            } else {
                                                List l = new ArrayList();
                                                l.add(String.valueOf(u.getID()));
                                                lightGoals.put(unitGoal, l);
                                            }
                                            break;
                                        case "Heavy":
                                            unitTotals[unitGoal.ordinal()][1] += 1;
                                            if(heavyGoals.containsKey(unitGoal)){
                                                heavyGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                            } else {
                                                List l = new ArrayList();
                                                l.add(String.valueOf(u.getID()));
                                                heavyGoals.put(unitGoal, l);
                                            }
                                            break;
                                        case "Ranged":
                                            unitTotals[unitGoal.ordinal()][2] += 1;
                                            if(rangedGoals.containsKey(unitGoal)){
                                                rangedGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                            } else {
                                                List l = new ArrayList();
                                                l.add(String.valueOf(u.getID()));
                                                rangedGoals.put(unitGoal, l);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }

                            //System.out.println("End Phase: "+currentPhase);
                            /*for(int b = 0; b < 4; b++){
                                for(int t = 0; t < 3; t++){
                                    System.out.print(unitTotals[b][t] + " ");
                                }

                                System.out.println();
                            }*/

                            UnitTotals.clear();

                            //Calculate the unit priorities
                            int p = currentPhase;


                            for(int b = 0; b < 4; b++) {
                                //System.out.println("Phase: " + p + " Priority Len: " + priorities[p].length + " Phase Len:" + priorities.length);
                                light = unitTotals[b][0];
                                heavy = unitTotals[b][1];
                                ranged = unitTotals[b][2];
                                ChromoBot.ChromoGoal prevGoal = ChromoBot.ChromoGoal.values()[b];

                                // Loop over each base and work out what units need to be assigned a particular goal using the priority
                                int[][] units = new int[4][3];

                                // Defend Blue -> Attack Red 1 -> Attack Red 2 -> Attack Red 3
                                float lightSum = priorities[p][0+(b*12)] + priorities[p][3+(b*12)] + priorities[p][6+(b*12)] + priorities[p][9+(b*12)];
                                float heavySum = priorities[p][1+(b*12)] + priorities[p][4+(b*12)] + priorities[p][7+(b*12)] + priorities[p][10+(b*12)];
                                float rangedSum = priorities[p][2+(b*12)] + priorities[p][5+(b*12)] + priorities[p][8+(b*12)] + priorities[p][11+(b*12)];

                                int lblueDef = (int)Math.floor(light * priorities[p][0+(b*12)] / lightSum);
                                int lredAtk1 = (int)Math.floor(light * priorities[p][3+(b*12)] / lightSum);
                                int lredAtk2 = (int)Math.floor(light * priorities[p][6+(b*12)] / lightSum);
                                int lredAtk3 = (int)Math.floor(light * priorities[p][9+(b*12)] / lightSum);

                                int lightLeft = light - (lblueDef + lredAtk1 + lredAtk2 + lredAtk3);
                                //System.out.println("Light Left: "+lightLeft);
                                //System.out.println("Light Placed: "+(lblueDef + lredAtk1 + lredAtk2 + lredAtk3));
                                List<Float> lightRemainders = new ArrayList();
                                lightRemainders.add(((light * priorities[p][0+(b*12)] / lightSum) - lblueDef)+lightLeft);
                                lightRemainders.add(((light * priorities[p][3+(b*12)] / lightSum) - lredAtk1)+lightLeft);
                                lightRemainders.add(((light * priorities[p][6+(b*12)] / lightSum) - lredAtk2)+lightLeft);
                                lightRemainders.add(((light * priorities[p][9+(b*12)] / lightSum) - lredAtk3)+lightLeft);


                                for(int i = 0; i < lightLeft; i++){
                                    float highest = 0;
                                    int index = 0;
                                    for(int r = 0; r < lightRemainders.size(); r++){
                                        if(lightRemainders.get(r) > highest){
                                            index = r;
                                            highest = lightRemainders.get(r);
                                        }
                                    }

                                    lightRemainders.set(index,highest-1.0f);

                                    switch(index){
                                        case 0: lblueDef += 1; break;
                                        case 1: lredAtk1 += 1; break;
                                        case 2: lredAtk2 += 1; break;
                                        case 3: lredAtk3 += 1; break;
                                    }
                                }

                                units[0][0] = lblueDef;
                                units[1][0] = lredAtk1;
                                units[2][0] = lredAtk2;
                                units[3][0] = lredAtk3;

                                int hblueDef = (int)Math.floor(heavy * priorities[p][1+(b*12)] / heavySum);
                                int hredAtk1 = (int)Math.floor(heavy * priorities[p][4+(b*12)] / heavySum);
                                int hredAtk2 = (int)Math.floor(heavy * priorities[p][7+(b*12)] / heavySum);
                                int hredAtk3 = (int)Math.floor(heavy * priorities[p][10+(b*12)] / heavySum);

                                int heavyLeft = heavy - (hblueDef + hredAtk1 + hredAtk2 + hredAtk3);
                                List<Float> heavyRemainders = new ArrayList();
                                heavyRemainders.add(((heavy * priorities[p][1+(b*12)] / heavySum) - hblueDef)+heavyLeft);
                                heavyRemainders.add(((heavy * priorities[p][4+(b*12)] / heavySum) - hredAtk1)+heavyLeft);
                                heavyRemainders.add(((heavy * priorities[p][7+(b*12)] / heavySum) - hredAtk2)+heavyLeft);
                                heavyRemainders.add(((heavy * priorities[p][10+(b*12)] / heavySum) - hredAtk3)+heavyLeft);


                                for(int i = 0; i < heavyLeft; i++){
                                    float highest = 0;
                                    int index = 0;
                                    for(int r = 0; r < heavyRemainders.size(); r++){
                                        if(heavyRemainders.get(r) > highest){
                                            index = r;
                                            highest = heavyRemainders.get(r);
                                        }
                                    }

                                    heavyRemainders.set(index,highest-1.0f);

                                    switch(index){
                                        case 0: hblueDef += 1; break;
                                        case 1: hredAtk1 += 1; break;
                                        case 2: hredAtk2 += 1; break;
                                        case 3: hredAtk3 += 1; break;
                                    }
                                }

                                units[0][1] = hblueDef;
                                units[1][1] = hredAtk1;
                                units[2][1] = hredAtk2;
                                units[3][1] = hredAtk3;

                                int rblueDef = (int)Math.floor(ranged * priorities[p][2+(b*12)] / rangedSum);
                                int rredAtk1 = (int)Math.floor(ranged * priorities[p][5+(b*12)] / rangedSum);
                                int rredAtk2 = (int)Math.floor(ranged * priorities[p][8+(b*12)] / rangedSum);
                                int rredAtk3 = (int)Math.floor(ranged * priorities[p][11+(b*12)] / rangedSum);

                                int rangedLeft = ranged - (rblueDef + rredAtk1 + rredAtk2 + rredAtk3);
                                List<Float> rangedRemainders = new ArrayList();
                                rangedRemainders.add(((ranged * priorities[p][2+(b*12)] / rangedSum) - rblueDef)+rangedLeft);
                                rangedRemainders.add(((ranged * priorities[p][5+(b*12)] / rangedSum) - rredAtk1)+rangedLeft);
                                rangedRemainders.add(((ranged * priorities[p][8+(b*12)] / rangedSum) - rredAtk2)+rangedLeft);
                                rangedRemainders.add(((ranged * priorities[p][11+(b*12)] / rangedSum) - rredAtk3)+rangedLeft);


                                for(int i = 0; i < rangedLeft; i++){
                                    float highest = 0;
                                    int index = 0;
                                    for(int r = 0; r < rangedRemainders.size(); r++){
                                        if(rangedRemainders.get(r) > highest){
                                            index = r;
                                            highest = rangedRemainders.get(r);
                                        }
                                    }

                                    rangedRemainders.set(index,highest-1.0f);

                                    switch(index){
                                        case 0: rblueDef += 1; break;
                                        case 1: rredAtk1 += 1; break;
                                        case 2: rredAtk2 += 1; break;
                                        case 3: rredAtk3 += 1; break;
                                    }
                                }

                                units[0][2] = rblueDef;
                                units[1][2] = rredAtk1;
                                units[2][2] = rredAtk2;
                                units[3][2] = rredAtk3;

                                System.out.println("Light: " + light + " Sum: " + lightSum + " Allocation: " + lblueDef + " " + lredAtk1 + " " + lredAtk2 + " " + lredAtk3);
                                System.out.println("Heavy: " + heavy + " Sum: " + heavySum + " Allocation: " + hblueDef + " " + hredAtk1 + " " + hredAtk2 + " " + hredAtk3);
                                System.out.println("Ranged: " + ranged + " Sum: " + rangedSum + " Allocation: " + rblueDef + " " + rredAtk1 + " " + rredAtk2 + " " + rredAtk3);

                                //Update Light Units
                                if(lightGoals.containsKey(prevGoal)) {
                                    for (int i = 0; i < lightGoals.get(prevGoal).size(); i++) {
                                        for (int k = 0; k < 4; k++) {
                                            if (units[k][0] > 0) {
                                                units[k][0] -= 1;
                                                UnitTotals.put(lightGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                                //System.out.println("Light Unit: "+(lightGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                                break;
                                            }
                                        }
                                    }
                                }

                                //Update Heavy Units
                                if(heavyGoals.containsKey(prevGoal)){
                                    for(int i = 0; i < heavyGoals.get(prevGoal).size(); i++){
                                        for(int k = 0; k < 4; k++){
                                            if(units[k][1] > 0){
                                                units[k][1] -= 1;
                                                UnitTotals.put(heavyGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                                //System.out.println("Heavy Unit: "+(heavyGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                                break;
                                            }
                                        }
                                    }
                                }

                                //Update Ranged Units
                                if(rangedGoals.containsKey(prevGoal)) {
                                    for (int i = 0; i < rangedGoals.get(prevGoal).size(); i++) {
                                        for (int k = 0; k < 4; k++) {
                                            if (units[k][2] > 0) {
                                                units[k][2] -= 1;
                                                UnitTotals.put(rangedGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                                //System.out.println("Ranged Unit: "+(rangedGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            //System.out.println("Prev Goals of current untis!");
                            /*int prevTotal = 0;
                            for(ChromoBot.ChromoGoal g : lightGoals.keySet()){
                                for(String id : lightGoals.get(g)) {
                                    //System.out.println("Light Unit: " + (g) + " " + id);
                                    prevTotal++;
                                }
                            }

                            for(ChromoBot.ChromoGoal g : heavyGoals.keySet()){
                                for(String id : heavyGoals.get(g)) {
                                    //System.out.println("Heavy Unit: " + (g) + " " + id);
                                    prevTotal++;
                                }
                            }

                            for(ChromoBot.ChromoGoal g : rangedGoals.keySet()){
                                for(String id : rangedGoals.get(g)) {
                                    //System.out.println("Ranged Unit: " + (g) + " " + id);
                                    prevTotal++;
                                }
                            }

                            //System.out.println("Goal Map Total: "+prevTotal);
                            prevTotal = 0;

                            for(Unit u : gs.getUnits()) {
                                if(u.getPlayer() == 0 && (u.getType().name == "Light" || u.getType().name == "Heavy" || u.getType().name == "Ranged" )){
                                    System.out.println(u.getType().name+" Unit: " + (UnitTotals.get(String.valueOf(u.getID()))) + " " + u.getID());
                                    prevTotal++;
                                }
                            }*/
                            //System.out.println("Unit Game State Total: "+prevTotal);*/

                            // Progress to next phase
                            currentPhase++;
                            //Thread.sleep(1000);
                        }

                        PlayerAction pa1 = ai1.getAction(0, gs);
                        PlayerAction pa2 = ai2.getAction(1, gs);

                        // Create a new trace entry
                        te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                        te.addPlayerAction(pa1.clone());
                        te.addPlayerAction(pa2.clone());
                        trace.addEntry(te);

                        gs.issueSafe(pa1);
                        gs.issueSafe(pa2);

                        /*for(Unit u : gs.getUnits()){
                            if(u.getPlayer()== 0)
                                System.out.println(u.getID()+":"+u.getUnitActions(gs));
                        }*/

                        // simulate:
                        gameover = gs.cycle();
                    } while (!gameover && gs.getTime() < MAXCYCLES);

                    //Thread.sleep(3000);
                    //w.dispose();
                    te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
                    trace.addEntry(te);

                    //Creating a File object for directory
                    File directoryPath = new File("traces");

                    // Write the file to the newest name
                    XMLWriter xml = new XMLWriter(new FileWriter("traces/game_evolution.xml"));
                    trace.toxml(xml);
                    xml.flush();
                    xml.close();

                    JFrame frame = new JFrame();
                    TraceDialogWindow traceDialog = new TraceDialogWindow(directoryPath, frame);
                    traceDialog.run();

                } else { // Faster, headless version
                    do { // TODO move 'trace' here & have a poper save option?
                        if(gs.getTime() > currentPhase*MAXCYCLES/phases && phases > 1){
                            HashMap<ChromoBot.ChromoGoal, List<String>> lightGoals = new HashMap();
                            HashMap<ChromoBot.ChromoGoal, List<String>> heavyGoals = new HashMap();
                            HashMap<ChromoBot.ChromoGoal, List<String>> rangedGoals = new HashMap();

                            int[][] unitTotals = new int[4][3];

                            for(Unit u : gs.getUnits()){
                                ChromoBot.ChromoGoal unitGoal = UnitTotals.get(String.valueOf(u.getID()));

                                if(unitGoal != null){
                                    switch (u.getType().name){
                                        case "Light":
                                            unitTotals[unitGoal.ordinal()][0] += 1;
                                            if(lightGoals.containsKey(unitGoal)){
                                                lightGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                            } else {
                                                List l = new ArrayList();
                                                l.add(String.valueOf(u.getID()));
                                                lightGoals.put(unitGoal, l);
                                            }
                                            break;
                                        case "Heavy":
                                            unitTotals[unitGoal.ordinal()][1] += 1;
                                            if(heavyGoals.containsKey(unitGoal)){
                                                heavyGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                            } else {
                                                List l = new ArrayList();
                                                l.add(String.valueOf(u.getID()));
                                                heavyGoals.put(unitGoal, l);
                                            }
                                            break;
                                        case "Ranged":
                                            unitTotals[unitGoal.ordinal()][2] += 1;
                                            if(rangedGoals.containsKey(unitGoal)){
                                                rangedGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                            } else {
                                                List l = new ArrayList();
                                                l.add(String.valueOf(u.getID()));
                                                rangedGoals.put(unitGoal, l);
                                            }
                                            break;
                                        default:
                                            break;
                                    }
                                }
                            }

                            /*System.out.println("End Phase: "+currentPhase);
                            for(int b = 0; b < 4; b++){
                                for(int t = 0; t < 3; t++){
                                    //System.out.print(unitTotals[b][t] + " ");
                                }

                                System.out.println();
                            }*/

                            UnitTotals.clear();

                            //Calculate the unit priorities
                            int p = currentPhase;


                            for(int b = 0; b < 4; b++) {
                                //System.out.println("Phase: " + p + " Priority Len: " + priorities[p].length + " Phase Len:" + priorities.length);
                                light = unitTotals[b][0];
                                heavy = unitTotals[b][1];
                                ranged = unitTotals[b][2];
                                ChromoBot.ChromoGoal prevGoal = ChromoBot.ChromoGoal.values()[b];

                                // Loop over each base and work out what units need to be assigned a particular goal using the priority
                                int[][] units = new int[4][3];

                                // Defend Blue -> Attack Red 1 -> Attack Red 2 -> Attack Red 3
                                float lightSum = priorities[p][0+(b*12)] + priorities[p][3+(b*12)] + priorities[p][6+(b*12)] + priorities[p][9+(b*12)];
                                float heavySum = priorities[p][1+(b*12)] + priorities[p][4+(b*12)] + priorities[p][7+(b*12)] + priorities[p][10+(b*12)];
                                float rangedSum = priorities[p][2+(b*12)] + priorities[p][5+(b*12)] + priorities[p][8+(b*12)] + priorities[p][11+(b*12)];

                                int lblueDef = (int)Math.floor(light * priorities[p][0+(b*12)] / lightSum);
                                int lredAtk1 = (int)Math.floor(light * priorities[p][3+(b*12)] / lightSum);
                                int lredAtk2 = (int)Math.floor(light * priorities[p][6+(b*12)] / lightSum);
                                int lredAtk3 = (int)Math.floor(light * priorities[p][9+(b*12)] / lightSum);

                                int lightLeft = light - (lblueDef + lredAtk1 + lredAtk2 + lredAtk3);
                                //System.out.println("Light Left: "+lightLeft);
                                //System.out.println("Light Placed: "+(lblueDef + lredAtk1 + lredAtk2 + lredAtk3));
                                List<Float> lightRemainders = new ArrayList();
                                lightRemainders.add(((light * priorities[p][0+(b*12)] / lightSum) - lblueDef)+lightLeft);
                                lightRemainders.add(((light * priorities[p][3+(b*12)] / lightSum) - lredAtk1)+lightLeft);
                                lightRemainders.add(((light * priorities[p][6+(b*12)] / lightSum) - lredAtk2)+lightLeft);
                                lightRemainders.add(((light * priorities[p][9+(b*12)] / lightSum) - lredAtk3)+lightLeft);


                                for(int i = 0; i < lightLeft; i++){
                                    float highest = 0;
                                    int index = 0;
                                    for(int r = 0; r < lightRemainders.size(); r++){
                                        if(lightRemainders.get(r) > highest){
                                            index = r;
                                            highest = lightRemainders.get(r);
                                        }
                                    }

                                    lightRemainders.set(index,highest-1.0f);

                                    switch(index){
                                        case 0: lblueDef += 1; break;
                                        case 1: lredAtk1 += 1; break;
                                        case 2: lredAtk2 += 1; break;
                                        case 3: lredAtk3 += 1; break;
                                    }
                                }

                                units[0][0] = lblueDef;
                                units[1][0] = lredAtk1;
                                units[2][0] = lredAtk2;
                                units[3][0] = lredAtk3;

                                int hblueDef = (int)Math.floor(heavy * priorities[p][1+(b*12)] / heavySum);
                                int hredAtk1 = (int)Math.floor(heavy * priorities[p][4+(b*12)] / heavySum);
                                int hredAtk2 = (int)Math.floor(heavy * priorities[p][7+(b*12)] / heavySum);
                                int hredAtk3 = (int)Math.floor(heavy * priorities[p][10+(b*12)] / heavySum);

                                int heavyLeft = heavy - (hblueDef + hredAtk1 + hredAtk2 + hredAtk3);
                                List<Float> heavyRemainders = new ArrayList();
                                heavyRemainders.add(((heavy * priorities[p][1+(b*12)] / heavySum) - hblueDef)+heavyLeft);
                                heavyRemainders.add(((heavy * priorities[p][4+(b*12)] / heavySum) - hredAtk1)+heavyLeft);
                                heavyRemainders.add(((heavy * priorities[p][7+(b*12)] / heavySum) - hredAtk2)+heavyLeft);
                                heavyRemainders.add(((heavy * priorities[p][10+(b*12)] / heavySum) - hredAtk3)+heavyLeft);


                                for(int i = 0; i < heavyLeft; i++){
                                    float highest = 0;
                                    int index = 0;
                                    for(int r = 0; r < heavyRemainders.size(); r++){
                                        if(heavyRemainders.get(r) > highest){
                                            index = r;
                                            highest = heavyRemainders.get(r);
                                        }
                                    }

                                    heavyRemainders.set(index,highest-1.0f);

                                    switch(index){
                                        case 0: hblueDef += 1; break;
                                        case 1: hredAtk1 += 1; break;
                                        case 2: hredAtk2 += 1; break;
                                        case 3: hredAtk3 += 1; break;
                                    }
                                }

                                units[0][1] = hblueDef;
                                units[1][1] = hredAtk1;
                                units[2][1] = hredAtk2;
                                units[3][1] = hredAtk3;

                                int rblueDef = (int)Math.floor(ranged * priorities[p][2+(b*12)] / rangedSum);
                                int rredAtk1 = (int)Math.floor(ranged * priorities[p][5+(b*12)] / rangedSum);
                                int rredAtk2 = (int)Math.floor(ranged * priorities[p][8+(b*12)] / rangedSum);
                                int rredAtk3 = (int)Math.floor(ranged * priorities[p][11+(b*12)] / rangedSum);

                                int rangedLeft = ranged - (rblueDef + rredAtk1 + rredAtk2 + rredAtk3);
                                List<Float> rangedRemainders = new ArrayList();
                                rangedRemainders.add(((ranged * priorities[p][2+(b*12)] / rangedSum) - rblueDef)+rangedLeft);
                                rangedRemainders.add(((ranged * priorities[p][5+(b*12)] / rangedSum) - rredAtk1)+rangedLeft);
                                rangedRemainders.add(((ranged * priorities[p][8+(b*12)] / rangedSum) - rredAtk2)+rangedLeft);
                                rangedRemainders.add(((ranged * priorities[p][11+(b*12)] / rangedSum) - rredAtk3)+rangedLeft);


                                for(int i = 0; i < rangedLeft; i++){
                                    float highest = 0;
                                    int index = 0;
                                    for(int r = 0; r < rangedRemainders.size(); r++){
                                        if(rangedRemainders.get(r) > highest){
                                            index = r;
                                            highest = rangedRemainders.get(r);
                                        }
                                    }

                                    rangedRemainders.set(index,highest-1.0f);

                                    switch(index){
                                        case 0: rblueDef += 1; break;
                                        case 1: rredAtk1 += 1; break;
                                        case 2: rredAtk2 += 1; break;
                                        case 3: rredAtk3 += 1; break;
                                    }
                                }

                                units[0][2] = rblueDef;
                                units[1][2] = rredAtk1;
                                units[2][2] = rredAtk2;
                                units[3][2] = rredAtk3;

                                //System.out.println("Light: " + light + " Sum: " + lightSum + " Allocation: " + lblueDef + " " + lredAtk1 + " " + lredAtk2 + " " + lredAtk3);
                                //System.out.println("Heavy: " + heavy + " Sum: " + heavySum + " Allocation: " + hblueDef + " " + hredAtk1 + " " + hredAtk2 + " " + hredAtk3);
                                //System.out.println("Ranged: " + ranged + " Sum: " + rangedSum + " Allocation: " + rblueDef + " " + rredAtk1 + " " + rredAtk2 + " " + rredAtk3);

                                //Update Light Units
                                if(lightGoals.containsKey(prevGoal)) {
                                    for (int i = 0; i < lightGoals.get(prevGoal).size(); i++) {
                                        for (int k = 0; k < 4; k++) {
                                            if (units[k][0] > 0) {
                                                units[k][0] -= 1;
                                                UnitTotals.put(lightGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                                //System.out.println("Light Unit: "+(lightGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                                break;
                                            }
                                        }
                                    }
                                }

                                //Update Heavy Units
                                if(heavyGoals.containsKey(prevGoal)){
                                    for(int i = 0; i < heavyGoals.get(prevGoal).size(); i++){
                                        for(int k = 0; k < 4; k++){
                                            if(units[k][1] > 0){
                                                units[k][1] -= 1;
                                                UnitTotals.put(heavyGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                                //System.out.println("Heavy Unit: "+(heavyGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                                break;
                                            }
                                        }
                                    }
                                }

                                //Update Ranged Units
                                if(rangedGoals.containsKey(prevGoal)) {
                                    for (int i = 0; i < rangedGoals.get(prevGoal).size(); i++) {
                                        for (int k = 0; k < 4; k++) {
                                            if (units[k][2] > 0) {
                                                units[k][2] -= 1;
                                                UnitTotals.put(rangedGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                                //System.out.println("Ranged Unit: "+(rangedGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            //System.out.println("Prev Goals of current untis!");
                            /*/int prevTotal = 0;
                            for(ChromoBot.ChromoGoal g : lightGoals.keySet()){
                                for(String id : lightGoals.get(g)) {
                                    //System.out.println("Light Unit: " + (g) + " " + id);
                                    prevTotal++;
                                }
                            }

                            for(ChromoBot.ChromoGoal g : heavyGoals.keySet()){
                                for(String id : heavyGoals.get(g)) {
                                    //System.out.println("Heavy Unit: " + (g) + " " + id);
                                    prevTotal++;
                                }
                            }

                            for(ChromoBot.ChromoGoal g : rangedGoals.keySet()){
                                for(String id : rangedGoals.get(g)) {
                                    //System.out.println("Ranged Unit: " + (g) + " " + id);
                                    prevTotal++;
                                }
                            }

                            System.out.println("Goal Map Total: "+prevTotal);
                            prevTotal = 0;

                            for(Unit u : gs.getUnits()) {
                                if(u.getPlayer() == 0 && (u.getType().name == "Light" || u.getType().name == "Heavy" || u.getType().name == "Ranged" )){
                                    System.out.println(u.getType().name+" Unit: " + (UnitTotals.get(String.valueOf(u.getID()))) + " " + u.getID());
                                    prevTotal++;
                                }
                            }*/
                            //System.out.println("Unit Game State Total: "+prevTotal);

                            // Progress to next phase
                            currentPhase++;
                        }

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
                /*te = new TraceEntry(gs.getPhysicalGameState().clone(), gs.getTime());
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
                }*/

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

                //System.out.println("Game Over: " + counts[0] + " " + counts[1] + " " + counts[2] + " " + counts[3] + " " + counts[4]);
                /*int lightUnits = chromosome[0][0] + chromosome[1][0] + chromosome[2][0] + chromosome[3][0];
                int heavyUnits = chromosome[0][1] + chromosome[1][1] + chromosome[2][1] + chromosome[3][1];
                int rangedUnits = chromosome[0][2] + chromosome[1][2] + chromosome[2][2] + chromosome[3][2];
                int totalUnits = lightUnits + heavyUnits + rangedUnits;
                System.out.println("Light: "+lightUnits+" Heavy: "+heavyUnits+" Ranged: "+rangedUnits+" Total:"+totalUnits);*/

                results.add(counts);
            }

            // Input file finished with
            buffrd.close();

            // Open output file and write it
            BufferedWriter buffwr = new BufferedWriter(new FileWriter("scores"+batch+".txt"));

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

            int currentPhase = 1;
            int phases = 1 +((chromstrs.length - 12) / 12 / 4);
            int light = lightUnits;
            int heavy = heavyUnits;
            int ranged = rangedUnits;

            float[][] priorities = new float[phases][48];

            for (int p = 0; p < phases; p++) {
                if(p == 0) {
                    for (int i = 0; i < 12; i++) {
                        priorities[p][i] = Float.parseFloat(chromstrs[i]);
                        //System.out.print(priorities[p][i] + " ");
                    }
                } else {
                    for (int i = 0; i < 48; i++) {
                        priorities[p][i] = Float.parseFloat(chromstrs[i + 12 + ((p-1) * 48)]);
                        //System.out.print(priorities[p][i] + " ");
                    }
                }
                //System.out.println();
            }

            // Setup game conditions
            PhysicalGameState pgs = PhysicalGameState.load(map, utt); // TwoBasesBarracks16x16.xml", utt);
            GameState gs = new GameState(pgs, utt);

            ArrayList<Unit> startingUnits = new ArrayList<>();
            HashMap<Unit, StringBuilder> unitStates = new HashMap<>();
            HashMap<Unit, StringBuilder> unitGoals = new HashMap<>();

            for(Unit u: pgs.getUnits()){
                if(u.getType().name == "Light"){
                    u.setHitPoints(8);
                } else if(u.getType().name == "Heavy"){
                    u.setHitPoints(8);
                } else if(u.getType().name == "Ranged"){
                    u.setHitPoints(2);
                }
            }
            //int MAXCYCLES = 1000; // Maximum game length
            int PERIOD = 15; // Refresh rate for display (milliseconds)
            boolean gameover = false;

            QuickDeploy(pgs, utt, priorities, light, heavy, ranged);
            HashMap<ChromoBot.ChromoGoal, int[]> baseLocations = new HashMap();
            HashMap<ChromoBot.ChromoGoal, String> baseIds = new HashMap();
            for(Unit u : pgs.getUnits()){
                if(u.getType().name  == "Base"){
                    switch (String.valueOf(u.getID())){
                        case "10":
                            baseLocations.put(ChromoBot.ChromoGoal.AttackRed1, new int[]{ u.getX(), u.getY() });
                            baseIds.put(ChromoBot.ChromoGoal.AttackRed1, "10");
                            break;
                        case "30":
                            baseLocations.put(ChromoBot.ChromoGoal.AttackRed2, new int[]{ u.getX(), u.getY() });
                            baseIds.put(ChromoBot.ChromoGoal.AttackRed2, "30");
                            break;
                        case "40":
                            baseLocations.put(ChromoBot.ChromoGoal.AttackRed3, new int[]{ u.getX(), u.getY() });
                            baseIds.put(ChromoBot.ChromoGoal.AttackRed3, "40");
                            break;
                        case "20":
                            baseLocations.put(ChromoBot.ChromoGoal.DefendBlue, new int[]{ u.getX(), u.getY() });
                            baseIds.put(ChromoBot.ChromoGoal.DefendBlue, "20");
                            break;
                    }
                }


                startingUnits.add(u);
                StringBuilder usb = new StringBuilder();
                usb.append("{"
                        + "\"x\":" + u.getX() + ", "
                        + "\"y\":" + u.getY() + ", "
                        + "\"hitpoints\":" + u.getHitPoints()
                        + "}");

                unitStates.put(u, usb);
                StringBuilder ugoals = new StringBuilder();
                if(UnitTotals.get(String.valueOf(u.getID())) != null)
                    ugoals.append("\""+UnitTotals.get(String.valueOf(u.getID())).name()+"\"");
                unitGoals.put(u, ugoals);
            }

            // Set the AIs
            AI ai1 = new ChromoBot(utt, new NewStarPathFinding(), UnitTotals, baseLocations);
            AI ai2 = new DefendBase(utt, new NewStarPathFinding());

            // Create a trace for saving the game

             // Faster, headless version
            do { // TODO move 'trace' here & have a poper save option?
                if(gs.getTime() > currentPhase*MAXCYCLES/phases && phases > 1){
                    HashMap<ChromoBot.ChromoGoal, List<String>> lightGoals = new HashMap();
                    HashMap<ChromoBot.ChromoGoal, List<String>> heavyGoals = new HashMap();
                    HashMap<ChromoBot.ChromoGoal, List<String>> rangedGoals = new HashMap();

                    int[][] unitTotals = new int[4][3];

                    for(Unit u : gs.getUnits()){
                        ChromoBot.ChromoGoal unitGoal = UnitTotals.get(String.valueOf(u.getID()));

                        if(unitGoal != null){
                            switch (u.getType().name){
                                case "Light":
                                    unitTotals[unitGoal.ordinal()][0] += 1;
                                    if(lightGoals.containsKey(unitGoal)){
                                        lightGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                    } else {
                                        List l = new ArrayList();
                                        l.add(String.valueOf(u.getID()));
                                        lightGoals.put(unitGoal, l);
                                    }
                                    break;
                                case "Heavy":
                                    unitTotals[unitGoal.ordinal()][1] += 1;
                                    if(heavyGoals.containsKey(unitGoal)){
                                        heavyGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                    } else {
                                        List l = new ArrayList();
                                        l.add(String.valueOf(u.getID()));
                                        heavyGoals.put(unitGoal, l);
                                    }
                                    break;
                                case "Ranged":
                                    unitTotals[unitGoal.ordinal()][2] += 1;
                                    if(rangedGoals.containsKey(unitGoal)){
                                        rangedGoals.get(unitGoal).add(String.valueOf(u.getID()));
                                    } else {
                                        List l = new ArrayList();
                                        l.add(String.valueOf(u.getID()));
                                        rangedGoals.put(unitGoal, l);
                                    }
                                    break;
                                default:
                                    break;
                            }
                        }
                    }

                    //System.out.println("End Phase: "+currentPhase);
                                /*for(int b = 0; b < 4; b++){
                                    for(int t = 0; t < 3; t++){
                                        System.out.print(unitTotals[b][t] + " ");
                                    }

                                    System.out.println();
                                }*/

                    UnitTotals.clear();

                    //Calculate the unit priorities
                    int p = currentPhase;


                    for(int b = 0; b < 4; b++) {
                        //System.out.println("Phase: " + p + " Priority Len: " + priorities[p].length + " Phase Len:" + priorities.length);
                        light = unitTotals[b][0];
                        heavy = unitTotals[b][1];
                        ranged = unitTotals[b][2];
                        ChromoBot.ChromoGoal prevGoal = ChromoBot.ChromoGoal.values()[b];

                        // Loop over each base and work out what units need to be assigned a particular goal using the priority
                        int[][] units = new int[4][3];

                        // Defend Blue -> Attack Red 1 -> Attack Red 2 -> Attack Red 3
                        float lightSum = priorities[p][0+(b*12)] + priorities[p][3+(b*12)] + priorities[p][6+(b*12)] + priorities[p][9+(b*12)];
                        float heavySum = priorities[p][1+(b*12)] + priorities[p][4+(b*12)] + priorities[p][7+(b*12)] + priorities[p][10+(b*12)];
                        float rangedSum = priorities[p][2+(b*12)] + priorities[p][5+(b*12)] + priorities[p][8+(b*12)] + priorities[p][11+(b*12)];

                        int lblueDef = (int)Math.floor(light * priorities[p][0+(b*12)] / lightSum);
                        int lredAtk1 = (int)Math.floor(light * priorities[p][3+(b*12)] / lightSum);
                        int lredAtk2 = (int)Math.floor(light * priorities[p][6+(b*12)] / lightSum);
                        int lredAtk3 = (int)Math.floor(light * priorities[p][9+(b*12)] / lightSum);

                        int lightLeft = light - (lblueDef + lredAtk1 + lredAtk2 + lredAtk3);
                        //System.out.println("Light Left: "+lightLeft);
                        //System.out.println("Light Placed: "+(lblueDef + lredAtk1 + lredAtk2 + lredAtk3));
                        List<Float> lightRemainders = new ArrayList();
                        lightRemainders.add(((light * priorities[p][0+(b*12)] / lightSum) - lblueDef)+lightLeft);
                        lightRemainders.add(((light * priorities[p][3+(b*12)] / lightSum) - lredAtk1)+lightLeft);
                        lightRemainders.add(((light * priorities[p][6+(b*12)] / lightSum) - lredAtk2)+lightLeft);
                        lightRemainders.add(((light * priorities[p][9+(b*12)] / lightSum) - lredAtk3)+lightLeft);


                        for(int i = 0; i < lightLeft; i++){
                            float highest = 0;
                            int index = 0;
                            for(int r = 0; r < lightRemainders.size(); r++){
                                if(lightRemainders.get(r) > highest){
                                    index = r;
                                    highest = lightRemainders.get(r);
                                }
                            }

                            lightRemainders.set(index,highest-1.0f);

                            switch(index){
                                case 0: lblueDef += 1; break;
                                case 1: lredAtk1 += 1; break;
                                case 2: lredAtk2 += 1; break;
                                case 3: lredAtk3 += 1; break;
                            }
                        }

                        units[0][0] = lblueDef;
                        units[1][0] = lredAtk1;
                        units[2][0] = lredAtk2;
                        units[3][0] = lredAtk3;

                        int hblueDef = (int)Math.floor(heavy * priorities[p][1+(b*12)] / heavySum);
                        int hredAtk1 = (int)Math.floor(heavy * priorities[p][4+(b*12)] / heavySum);
                        int hredAtk2 = (int)Math.floor(heavy * priorities[p][7+(b*12)] / heavySum);
                        int hredAtk3 = (int)Math.floor(heavy * priorities[p][10+(b*12)] / heavySum);

                        int heavyLeft = heavy - (hblueDef + hredAtk1 + hredAtk2 + hredAtk3);
                        List<Float> heavyRemainders = new ArrayList();
                        heavyRemainders.add(((heavy * priorities[p][1+(b*12)] / heavySum) - hblueDef)+heavyLeft);
                        heavyRemainders.add(((heavy * priorities[p][4+(b*12)] / heavySum) - hredAtk1)+heavyLeft);
                        heavyRemainders.add(((heavy * priorities[p][7+(b*12)] / heavySum) - hredAtk2)+heavyLeft);
                        heavyRemainders.add(((heavy * priorities[p][10+(b*12)] / heavySum) - hredAtk3)+heavyLeft);


                        for(int i = 0; i < heavyLeft; i++){
                            float highest = 0;
                            int index = 0;
                            for(int r = 0; r < heavyRemainders.size(); r++){
                                if(heavyRemainders.get(r) > highest){
                                    index = r;
                                    highest = heavyRemainders.get(r);
                                }
                            }

                            heavyRemainders.set(index,highest-1.0f);

                            switch(index){
                                case 0: hblueDef += 1; break;
                                case 1: hredAtk1 += 1; break;
                                case 2: hredAtk2 += 1; break;
                                case 3: hredAtk3 += 1; break;
                            }
                        }

                        units[0][1] = hblueDef;
                        units[1][1] = hredAtk1;
                        units[2][1] = hredAtk2;
                        units[3][1] = hredAtk3;

                        int rblueDef = (int)Math.floor(ranged * priorities[p][2+(b*12)] / rangedSum);
                        int rredAtk1 = (int)Math.floor(ranged * priorities[p][5+(b*12)] / rangedSum);
                        int rredAtk2 = (int)Math.floor(ranged * priorities[p][8+(b*12)] / rangedSum);
                        int rredAtk3 = (int)Math.floor(ranged * priorities[p][11+(b*12)] / rangedSum);

                        int rangedLeft = ranged - (rblueDef + rredAtk1 + rredAtk2 + rredAtk3);
                        List<Float> rangedRemainders = new ArrayList();
                        rangedRemainders.add(((ranged * priorities[p][2+(b*12)] / rangedSum) - rblueDef)+rangedLeft);
                        rangedRemainders.add(((ranged * priorities[p][5+(b*12)] / rangedSum) - rredAtk1)+rangedLeft);
                        rangedRemainders.add(((ranged * priorities[p][8+(b*12)] / rangedSum) - rredAtk2)+rangedLeft);
                        rangedRemainders.add(((ranged * priorities[p][11+(b*12)] / rangedSum) - rredAtk3)+rangedLeft);


                        for(int i = 0; i < rangedLeft; i++){
                            float highest = 0;
                            int index = 0;
                            for(int r = 0; r < rangedRemainders.size(); r++){
                                if(rangedRemainders.get(r) > highest){
                                    index = r;
                                    highest = rangedRemainders.get(r);
                                }
                            }

                            rangedRemainders.set(index,highest-1.0f);

                            switch(index){
                                case 0: rblueDef += 1; break;
                                case 1: rredAtk1 += 1; break;
                                case 2: rredAtk2 += 1; break;
                                case 3: rredAtk3 += 1; break;
                            }
                        }

                        units[0][2] = rblueDef;
                        units[1][2] = rredAtk1;
                        units[2][2] = rredAtk2;
                        units[3][2] = rredAtk3;

                        System.out.println("Light: " + light + " Sum: " + lightSum + " Allocation: " + lblueDef + " " + lredAtk1 + " " + lredAtk2 + " " + lredAtk3);
                        System.out.println("Heavy: " + heavy + " Sum: " + heavySum + " Allocation: " + hblueDef + " " + hredAtk1 + " " + hredAtk2 + " " + hredAtk3);
                        System.out.println("Ranged: " + ranged + " Sum: " + rangedSum + " Allocation: " + rblueDef + " " + rredAtk1 + " " + rredAtk2 + " " + rredAtk3);

                        //Update Light Units
                        if(lightGoals.containsKey(prevGoal)) {
                            for (int i = 0; i < lightGoals.get(prevGoal).size(); i++) {
                                for (int k = 0; k < 4; k++) {
                                    if (units[k][0] > 0) {
                                        units[k][0] -= 1;
                                        UnitTotals.put(lightGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                        //System.out.println("Light Unit: "+(lightGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                        break;
                                    }
                                }
                            }
                        }

                        //Update Heavy Units
                        if(heavyGoals.containsKey(prevGoal)){
                            for(int i = 0; i < heavyGoals.get(prevGoal).size(); i++){
                                for(int k = 0; k < 4; k++){
                                    if(units[k][1] > 0){
                                        units[k][1] -= 1;
                                        UnitTotals.put(heavyGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                        //System.out.println("Heavy Unit: "+(heavyGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                        break;
                                    }
                                }
                            }
                        }

                        //Update Ranged Units
                        if(rangedGoals.containsKey(prevGoal)) {
                            for (int i = 0; i < rangedGoals.get(prevGoal).size(); i++) {
                                for (int k = 0; k < 4; k++) {
                                    if (units[k][2] > 0) {
                                        units[k][2] -= 1;
                                        UnitTotals.put(rangedGoals.get(prevGoal).get(i), ChromoBot.ChromoGoal.values()[k]);
                                        //System.out.println("Ranged Unit: "+(rangedGoals.get(prevGoal).get(i))+" "+ChromoBot.ChromoGoal.values()[k].toString());
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    //System.out.println("Prev Goals of current untis!");
                                /*int prevTotal = 0;
                                for(ChromoBot.ChromoGoal g : lightGoals.keySet()){
                                    for(String id : lightGoals.get(g)) {
                                        //System.out.println("Light Unit: " + (g) + " " + id);
                                        prevTotal++;
                                    }
                                }

                                for(ChromoBot.ChromoGoal g : heavyGoals.keySet()){
                                    for(String id : heavyGoals.get(g)) {
                                        //System.out.println("Heavy Unit: " + (g) + " " + id);
                                        prevTotal++;
                                    }
                                }

                                for(ChromoBot.ChromoGoal g : rangedGoals.keySet()){
                                    for(String id : rangedGoals.get(g)) {
                                        //System.out.println("Ranged Unit: " + (g) + " " + id);
                                        prevTotal++;
                                    }
                                }

                                //System.out.println("Goal Map Total: "+prevTotal);
                                prevTotal = 0;

                                for(Unit u : gs.getUnits()) {
                                    if(u.getPlayer() == 0 && (u.getType().name == "Light" || u.getType().name == "Heavy" || u.getType().name == "Ranged" )){
                                        System.out.println(u.getType().name+" Unit: " + (UnitTotals.get(String.valueOf(u.getID()))) + " " + u.getID());
                                        prevTotal++;
                                    }
                                }*/
                    //System.out.println("Unit Game State Total: "+prevTotal);*/
                    for(Unit u : startingUnits){
                        StringBuilder goals = unitGoals.get(u);
                        if(UnitTotals.get(String.valueOf(u.getID())) != null) {
                            if (goals.length() > 0)
                                goals.append(",");

                            goals.append("\"" + UnitTotals.get(String.valueOf(u.getID())).name() + "\"");
                        }
                    }
                    // Progress to next phase
                    currentPhase++;
                    //Thread.sleep(1000);
                }

                PlayerAction pa1 = ai1.getAction(0, gs);
                PlayerAction pa2 = ai2.getAction(1, gs);

                // Create a new trace entry
                /*te = new TraceEntry(gs.getPhysicalGameState().clone(),gs.getTime());
                te.addPlayerAction(pa1.clone());
                te.addPlayerAction(pa2.clone());
                trace.addEntry(te);*/

                boolean isFirst = true;
                for(Unit u : startingUnits){
                    StringBuilder states = unitStates.get(u);
                    states.append(",");

                    states.append("{"
                            + "\"x\":" + u.getX() + ", "
                            + "\"y\":" + u.getY() + ", "
                            + "\"hitpoints\":" + u.getHitPoints()
                            + "}");
                }

                gs.issueSafe(pa1);
                gs.issueSafe(pa2);

                // simulate:
                gameover = gs.cycle();
            } while (!gameover && gs.getTime() < MAXCYCLES);

            for(Unit u : startingUnits){
                StringBuilder states = unitStates.get(u);
                states.append(",");

                states.append("{"
                        + "\"x\":" + u.getX() + ", "
                        + "\"y\":" + u.getY() + ", "
                        + "\"hitpoints\":" + u.getHitPoints()
                        + "}");
            }

            ai1.gameOver(gs.winner()); // TODO what do these do?
            ai2.gameOver(gs.winner());

            long start = System.currentTimeMillis();

            FileWriter json = new FileWriter("trace.json");

            StringBuilder sb = new StringBuilder();
            sb.append("{ \"Units\": [");
            boolean first = true;
            for(Unit u : unitStates.keySet()){
                if(first) {
                    first = false;
                } else {
                    sb.append(",");
                }

                sb.append("{ "
                        + "\"type\":\"" + u.getType().name + "\""
                        + ",\"id\":" + u.getID()
                        + ",\"player\":" + u.getPlayer()
                        + ",\"states\":[");

                sb.append(unitStates.get(u).toString());

                sb.append("]" +
                        ",\"goals\":[");

                sb.append(unitGoals.get(u).toString());

                sb.append("]}");
            }

            sb.append("]}");

            long elapsedTime = System.currentTimeMillis() - start;
            System.out.println("Append JSON took: "+elapsedTime);
            start = System.currentTimeMillis();

            json.write(sb.toString());
            json.flush();
            json.close();

            elapsedTime = System.currentTimeMillis() - start;
            System.out.println("Writing JSON took: "+elapsedTime);
            // Finish up game trace & save it if 'display' is set

            //Creating a File object for directory
            //File directoryPath = new File("traces");
            //List of all files and directories
            //String contents[] = directoryPath.list();
            //int num = contents.length;

            // Write the file to the newest name
            //XMLWriter xml = new XMLWriter(new FileWriter("traces/game_" + String.valueOf(num) + ".xml"));

            //trace.toxml(xml);
            //xml.flush();
            //xml.close();
        }
    }
}
