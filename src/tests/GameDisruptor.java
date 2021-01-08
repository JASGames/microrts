/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tests;

import ai.abstraction.LightRush;
import ai.core.AI;
import ai.abstraction.pathfinding.NewStarPathFinding;
import gui.PhysicalGameStatePanel;

import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.Unit;
import rts.units.UnitType;
import rts.units.UnitTypeTable;

import static rts.units.UnitTypeTable.*;

/**
 *
 * @author jasnell
 */
public class GameDisruptor extends JPanel {
    private static final int HP = 20;
    private static final int DMG = 10;
    private static final int RNG = 2;
    private static final int MT = 8;
    private static final int AT = 6;

    private static int BlueHP = HP;
    private static int BlueDamage = DMG;
    private static int BlueRange = RNG;
    private static int BlueMoveTime = MT;
    private static int BlueAttackTime = AT;
    private static int RedHP = HP;
    private static int RedDamage = DMG;
    private static int RedRange = RNG;
    private static int RedMoveTime = MT;
    private static int RedAttackTime = AT;
    private static int SimulationCount = 1000;
    private static boolean Running = false;

    private static JLabel BlueWins;
    private static JLabel RedWins;
    private static JLabel Draws;
    private static JLabel BlueWinRate;
    private static float WinRate;
    private static float TargetWinRate = 100f;

    private static float WinWeight = 1.0f;
    private static float ChangeWeight = 0.0f;

    private static int CurrentDraws = 0;
    private static int CurrentBWins = 0;
    private static int CurrentRWins = 0;

    @FunctionalInterface
    public interface SL extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            try {
                update(e);
            } catch(Exception e1){
                //System.out.println(e1);
            }
        }
        @Override
        default void removeUpdate(DocumentEvent e) {
            try {
                update(e);
            } catch(Exception e1){
                //System.out.println(e1);
            }
        }
        @Override
        default void changedUpdate(DocumentEvent e) {
            try {
                update(e);
            } catch(Exception e1){
                //System.out.println(e1 );
            }
        }
    }

    public static void RunSimulation(boolean display, int threadsToUse) throws Exception {
        UnitTypeTable utt = new UnitTypeTable(VERSION_ORIGINAL, MOVE_CONFLICT_RESOLUTION_CANCEL_BOTH);

        //BlueLight
        UnitType blight = new UnitType();
        blight.name = "BlueLight";
        blight.cost = 2;
        blight.hp = BlueHP;
        blight.minDamage = 0;
        blight.maxDamage = BlueDamage;
        blight.attackRange = BlueRange;
        blight.produceTime = 80;
        blight.moveTime = BlueMoveTime;
        blight.attackTime = BlueAttackTime;
        blight.isResource = false;
        blight.isStockpile = false;
        blight.canHarvest = false;
        blight.canMove = true;
        blight.canAttack = true;
        blight.sightRadius = 2;
        utt.addUnitType(blight);

        //RedLight
        UnitType rlight = new UnitType();
        rlight.name = "RedLight";
        rlight.cost = 2;
        rlight.hp = RedHP;
        rlight.minDamage = 0;
        rlight.maxDamage = RedDamage;
        rlight.attackRange = RedRange;
        rlight.produceTime = 80;
        rlight.moveTime = RedMoveTime;
        rlight.attackTime = RedAttackTime;
        rlight.isResource = false;
        rlight.isStockpile = false;
        rlight.canHarvest = false;
        rlight.canMove = true;
        rlight.canAttack = true;
        rlight.sightRadius = 2;
        utt.addUnitType(rlight);

        String map = "C:\\Users\\jasnell\\OneDrive - Edith Cowan University\\Documents\\even_map.xml";

        CurrentBWins = 0;
        CurrentRWins = 0;
        CurrentDraws = 0;

        if(display){
            // Setup game conditions
            PhysicalGameState pgs = PhysicalGameState.load(map, utt); // even_map.xml", utt);
            //Update unit health to match stats
            for(Unit unit : pgs.getUnits()){
                if(unit.getType() == blight){
                    unit.setHitPoints(blight.hp);
                } else if(unit.getType() == rlight){
                    unit.setHitPoints(rlight.hp);
                }
            }
            GameState gs = new GameState(pgs, utt);

            int MAXCYCLES = 350; // Maximum game length
            int PERIOD = 25; // Refresh rate for display (milliseconds)

            boolean gameover = false;

            // Set the AIs
            AI ai1 = new LightRush(utt, new NewStarPathFinding());
            AI ai2 = new LightRush(utt, new NewStarPathFinding());

            JFrame w = PhysicalGameStatePanel.newVisualizer(gs, 768, 768, true, PhysicalGameStatePanel.COLORSCHEME_BLACK);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do {
                if (System.currentTimeMillis() >= nextTimeToUpdate) {
                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);

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

                if(w.isVisible() == false) {
                    gameover = true;
                    Running = false;
                }
            } while (!gameover && gs.getTime() < MAXCYCLES);
            w.dispose();
        } else if(threadsToUse == 1){
            int runs = SimulationCount;
            int bWins = 0;
            int rWins = 0;
            int draws = 0;

            for(int i = 0; i < runs; i++) {
                // Setup game conditions
                PhysicalGameState pgs = PhysicalGameState.load(map, utt);

                //Update unit health to match stats
                for (Unit unit : pgs.getUnits()) {
                    if (unit.getType() == blight) {
                        unit.setHitPoints(blight.hp);
                    } else if (unit.getType() == rlight) {
                        unit.setHitPoints(rlight.hp);
                    }
                }
                GameState gs = new GameState(pgs, utt);

                int MAXCYCLES = 350; // Maximum game length
                int PERIOD = 25; // Refresh rate for display (milliseconds)

                boolean gameover = false;

                // Set the AIs
                AI ai1 = new LightRush(utt, new NewStarPathFinding());
                AI ai2 = new LightRush(utt, new NewStarPathFinding());

                do {
                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                } while (!gameover && gs.getTime() < MAXCYCLES);

                ai1.gameOver(gs.winner()); // TODO what do these do?
                ai2.gameOver(gs.winner());

                int result = gs.winner();
                if (result == -1) {
                    draws++;
                } else if (result == 0) {
                    bWins++;
                } else if (result == 1) {
                    rWins++;
                }
            }

            CurrentDraws += draws;
            CurrentBWins += bWins;
            CurrentRWins += rWins;
        } else {
            ArrayList<Thread> threadList = new ArrayList<Thread>();

            // Spin up multiple simulations across multiple threads
            for(int t = 0; t < threadsToUse; t++) {
                Thread thread = new Thread(() -> {
                    int runs = (int)Math.ceil(SimulationCount/threadsToUse);
                    int bWins = 0;
                    int rWins = 0;
                    int draws = 0;

                    for(int i = 0; i < runs; i++){
                        try {
                            // Setup game conditions
                            PhysicalGameState pgs = PhysicalGameState.load(map, utt);

                            //Update unit health to match stats
                            for (Unit unit : pgs.getUnits()) {
                                if (unit.getType() == blight) {
                                    unit.setHitPoints(blight.hp);
                                } else if (unit.getType() == rlight) {
                                    unit.setHitPoints(rlight.hp);
                                }
                            }
                            GameState gs = new GameState(pgs, utt);

                            int MAXCYCLES = 350; // Maximum game length
                            int PERIOD = 25; // Refresh rate for display (milliseconds)

                            boolean gameover = false;

                            // Set the AIs
                            AI ai1 = new LightRush(utt, new NewStarPathFinding());
                            AI ai2 = new LightRush(utt, new NewStarPathFinding());

                            do {
                                PlayerAction pa1 = ai1.getAction(0, gs);
                                PlayerAction pa2 = ai2.getAction(1, gs);
                                gs.issueSafe(pa1);
                                gs.issueSafe(pa2);

                                // simulate:
                                gameover = gs.cycle();
                            } while (!gameover && gs.getTime() < MAXCYCLES);

                            ai1.gameOver(gs.winner()); // TODO what do these do?
                            ai2.gameOver(gs.winner());

                            int result = gs.winner();
                            if (result == -1) {
                                draws++;
                            } else if (result == 0) {
                                bWins++;
                            } else if (result == 1) {
                                rWins++;
                            }

                            /*if (i % 100 == 0) {
                                BlueWins.setText("Blue Wins: " + bWins);
                                RedWins.setText("Red Wins: " + rWins);
                                Draws.setText("Draws: " + draws);
                                WinRate = ((bWins + (draws / 2.0f)) / (float) (bWins + rWins + draws) * 100);
                                BlueWinRate.setText("Blue Win-rate: " + WinRate + "%");
                            }*/
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    CurrentDraws += draws;
                    CurrentBWins += bWins;
                    CurrentRWins += rWins;
                });

                //Keep track of the thread and start it
                threadList.add(thread);
                thread.start();
            }

            while(threadList.size() > 0){
                for(int i = 0; i < threadList.size(); i++){
                    if(threadList.get(i).getState() == Thread.State.TERMINATED){
                        threadList.remove(threadList.get(i));
                    }
                }

                Thread.sleep(5);
            }
        }



        if(!display) {
            BlueWins.setText("Blue Wins: " + CurrentBWins);
            RedWins.setText("Red Wins: " + CurrentRWins);
            Draws.setText("Draws: " + CurrentDraws);
            WinRate = ((CurrentBWins + (CurrentDraws / 2.0f)) / (float) (CurrentBWins + CurrentRWins + CurrentDraws) * 100);
            BlueWinRate.setText("Blue Win-rate: " + WinRate + "%");
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("microRTS Game Disruptor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(10,4));
        p1.setBorder(new EmptyBorder(20, 20, 20, 20));

        p1.add( new JLabel("Blue HP: "));

        JTextField blHP = new JTextField();
        blHP.setText(String.valueOf(BlueHP));
        blHP.getDocument().addDocumentListener((SL) e -> {
            BlueHP = Integer.parseInt(blHP.getText());
        });
        p1.add(blHP);

        p1.add( new JLabel("Red HP: "));

        JTextField rdHP = new JTextField();
        rdHP.setText(String.valueOf(RedHP));
        rdHP.getDocument().addDocumentListener((SL) e -> {
            RedHP = Integer.parseInt(rdHP.getText());
        });
        p1.add(rdHP);

        p1.add( new JLabel("Blue Attack: "));

        JTextField blAtk = new JTextField();
        blAtk.setText(String.valueOf(BlueDamage));
        blAtk.getDocument().addDocumentListener((SL) e -> {
            BlueDamage = Integer.parseInt(blAtk.getText());
        });
        p1.add(blAtk);

        p1.add( new JLabel("Red Attack: "));

        JTextField rdAtk = new JTextField();
        rdAtk.setText(String.valueOf(RedDamage));
        rdAtk.getDocument().addDocumentListener((SL) e -> {
            RedDamage = Integer.parseInt(rdAtk.getText());
        });
        p1.add(rdAtk);

        p1.add( new JLabel("Blue Attack Range: "));

        JTextField blAtkRange = new JTextField();
        blAtkRange.setText(String.valueOf(BlueRange));
        blAtkRange.getDocument().addDocumentListener((SL) e -> {
            BlueRange = Integer.parseInt(blAtkRange.getText());
        });
        p1.add(blAtkRange);

        p1.add( new JLabel("Red Attack Range: "));

        JTextField rdAtkRange = new JTextField();
        rdAtkRange.setText(String.valueOf(RedRange));
        rdAtkRange.getDocument().addDocumentListener((SL) e -> {
            RedRange = Integer.parseInt(rdAtkRange.getText());
        });
        p1.add(rdAtkRange);

        p1.add( new JLabel("Blue Move Time: "));

        JTextField blMoveTime = new JTextField();
        blMoveTime.setText(String.valueOf(BlueMoveTime));
        blMoveTime.getDocument().addDocumentListener((SL) e -> {
            BlueMoveTime = Integer.parseInt(blMoveTime.getText());
        });
        p1.add(blMoveTime);

        p1.add( new JLabel("Red Move Time: "));

        JTextField rdMoveTime = new JTextField();
        rdMoveTime.setText(String.valueOf(RedMoveTime));
        rdMoveTime.getDocument().addDocumentListener((SL) e -> {
            RedMoveTime = Integer.parseInt(rdMoveTime.getText());
        });
        p1.add(rdMoveTime);

        p1.add( new JLabel("Blue Attack Time: "));

        JTextField blAttackTime = new JTextField();
        blAttackTime.setText(String.valueOf(BlueAttackTime));
        blAttackTime.getDocument().addDocumentListener((SL) e -> {
            BlueAttackTime = Integer.parseInt(blAttackTime.getText());
        });
        p1.add(blAttackTime);

        p1.add( new JLabel("Red Attack Time: "));

        JTextField rdAttackTime = new JTextField();
        rdAttackTime.setText(String.valueOf(RedAttackTime));
        rdAttackTime.getDocument().addDocumentListener((SL) e -> {
            RedAttackTime = Integer.parseInt(rdAttackTime.getText());
        });
        p1.add(rdAttackTime);

        BlueWins = new JLabel("Blue Wins: ");
        RedWins = new JLabel("Red Wins: ");
        BlueWinRate = new JLabel("Blue Win-rate: ");
        Draws = new JLabel("Draws: ");

        p1.add(BlueWins);
        p1.add(RedWins);
        p1.add(BlueWinRate);
        p1.add(Draws);

        p1.add( new JLabel("Simulation Count: "));

        JTextField simCount = new JTextField();
        simCount.setText(String.valueOf(SimulationCount));
        simCount.getDocument().addDocumentListener((SL) e -> {
            SimulationCount = Integer.parseInt(simCount.getText());
        });
        p1.add(simCount);

        JButton runButton = new JButton("Run Simulation");
        runButton.addActionListener(e -> {
            if(Running == false){
                Running = true;
                new Thread(() -> {
                    try {
                        RunSimulation(false, Runtime.getRuntime().availableProcessors()+2);
                    } catch (Exception e1){
                        System.out.println(e1);
                    }

                    Running = false;
                }).start();
            }
        });

        JPanel runPanel = new JPanel();
        runPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        runPanel.add(runButton);

        JButton showButton = new JButton("Show Simulation");
        showButton.addActionListener(e -> {
            if(Running == false){
                Running = true;
                new Thread(() -> {
                    try {
                        RunSimulation(true, 1);
                    } catch (Exception e1){
                        System.out.println(e1);
                    }

                    Running = false;
                }).start();
            }
        });

        JPanel showPanel = new JPanel();
        showPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        showPanel.add(showButton);

        p1.add(runPanel);
        p1.add(showPanel);

        p1.add( new JLabel("Target Win-rate: "));

        JTextField targetWinRate = new JTextField();
        targetWinRate.setText(String.valueOf(TargetWinRate));
        targetWinRate.getDocument().addDocumentListener((SL) e -> {
            TargetWinRate = Float.parseFloat(targetWinRate.getText());
        });
        p1.add(targetWinRate);

        JButton evolveButton = new JButton("Evolve");
        evolveButton.addActionListener(e -> {
            if(Running == false){
                Running = true;
                new Thread(() -> {
                    try {
                        //RunSimulation(false);
                        BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
                        System.out.println("Evolve");


                        while(true) {
                            String s = bufferRead.readLine();

                            if(s.contains("END")){
                                System.out.println("FOUND END!");
                                break;
                            } else if(!s.isEmpty()) {
                                String[] individuals = s.split(" : ");
                                int[][] chromosome = new int[individuals.length][5];
                                for (int i = 0; i < individuals.length; i++) {
                                    String[] geneString = individuals[i].split(", ");
                                    for (int k = 0; k < 5; k++) {
                                        chromosome[i][k] = Integer.parseInt(geneString[k]);
                                    }
                                }

                                String fitness = "";
                                for (int i = 0; i < individuals.length; i++) {
                                    BlueHP = chromosome[i][0];
                                    BlueDamage = chromosome[i][1];
                                    BlueRange = chromosome[i][2];
                                    BlueMoveTime = chromosome[i][3];
                                    BlueAttackTime = chromosome[i][4];

                                    RunSimulation(false, Runtime.getRuntime().availableProcessors()+2);

                                    float changed = (5.0f - (Math.abs(1.0f - ((float)chromosome[i][0]/HP)) + Math.abs(1.0f - ((float)chromosome[i][1]/DMG)) + Math.abs(1.0f - ((float)chromosome[i][2]/RNG)) + Math.abs(1.0f - ((float)chromosome[i][3]/MT)) + Math.abs(1.0f - ((float)chromosome[i][4]/AT)))) / 5.0f;
                                    float tWinRate = (100-Math.abs(WinRate-TargetWinRate)) / 100f;
                                    fitness += ((tWinRate*WinWeight) + (changed*ChangeWeight)) + " ";
                                }

                                System.out.println(fitness.trim());
                                System.out.flush();
                            }
                        }

                    } catch (Exception e1){
                        System.out.print(e1);
                        System.out.print(" ");
                        for(StackTraceElement el : e1.getStackTrace()){
                            System.out.print(el.toString());
                            System.out.print(" ");
                        }
                        System.out.println();
                    }

                    Running = false;
                }).start();
            }
        });

        JPanel evolvePanel = new JPanel();
        evolvePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        evolvePanel.add(evolveButton);

        p1.add(evolvePanel);

        p1.add(new Panel());

        p1.add( new JLabel("Win-rate Weight: "));

        JTextField winRateWeight = new JTextField();
        winRateWeight.setText(String.valueOf(WinWeight));
        winRateWeight.getDocument().addDocumentListener((SL) e -> {
            WinWeight = Float.parseFloat(winRateWeight.getText());
        });
        p1.add(winRateWeight);

        p1.add( new JLabel("Changed Weight: "));

        JTextField changedWeight = new JTextField();
        changedWeight.setText(String.valueOf(ChangeWeight));
        changedWeight.getDocument().addDocumentListener((SL) e -> {
            ChangeWeight = Float.parseFloat(changedWeight.getText());
        });
        p1.add(changedWeight);

        frame.add(p1, BorderLayout.CENTER);
        frame.pack();
        frame.setMinimumSize(new Dimension(600, 300));
        frame.setVisible(true);
    }
}
