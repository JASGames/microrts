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
    private static int BlueHP = 20;
    private static int BlueDamage = 10;
    private static int BlueRange = 2;
    private static int BlueMoveTime = 8;
    private static int BlueAttackTime = 10;
    private static int RedHP = 20;
    private static int RedDamage = 10;
    private static int RedRange = 2;
    private static int RedMoveTime = 8;
    private static int RedAttackTime = 10;
    private static int SimulationCount = 1000;
    private static boolean Running = false;

    private static JLabel BlueWins;
    private static JLabel RedWins;
    private static JLabel Draws;
    private static JLabel BlueWinRate;

    @FunctionalInterface
    public interface SL extends DocumentListener {
        void update(DocumentEvent e);

        @Override
        default void insertUpdate(DocumentEvent e) {
            try {
                update(e);
            } catch(Exception e1){
                System.out.println(e1);
            }
        }
        @Override
        default void removeUpdate(DocumentEvent e) {
            try {
                update(e);
            } catch(Exception e1){
                System.out.println(e1);
            }
        }
        @Override
        default void changedUpdate(DocumentEvent e) {
            try {
                update(e);
            } catch(Exception e1){
                System.out.println(e1);
            }
        }
    }

    public static void RunSimulation(boolean display) throws Exception {
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
        rlight.moveTime = 8;
        rlight.attackTime = 10;
        rlight.isResource = false;
        rlight.isStockpile = false;
        rlight.canHarvest = false;
        rlight.canMove = true;
        rlight.canAttack = true;
        rlight.sightRadius = 2;
        utt.addUnitType(rlight);

        String map = "C:\\Users\\jasnell\\OneDrive - Edith Cowan University\\Documents\\even_map.xml";

        int bWins = 0;
        int rWins = 0;
        int draws = 0;
        int runs = SimulationCount;

        if(display)
            runs = 1;

        for(int i = 0; i < runs; i++) {
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

            if (!display) {
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
                if(result == -1)
                {
                    draws++;
                } else if(result == 0) {
                    bWins++;
                } else if(result == 1) {
                    rWins++;
                }

                if(i % 100 == 0) {
                    BlueWins.setText("Blue Wins: " + bWins);
                    RedWins.setText("Red Wins: " + rWins);
                    Draws.setText("Draws: " + draws);
                    BlueWinRate.setText("Blue Win-rate: " + ((bWins + (draws / 2.0f)) / (float) (bWins + rWins + draws) * 100) + "%");
                }
            } else {
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
            }
        }

        if(!display) {
            BlueWins.setText("Blue Wins: " + bWins);
            RedWins.setText("Red Wins: " + rWins);
            Draws.setText("Draws: " + draws);
            BlueWinRate.setText("Blue Win-rate: " + ((bWins + (draws / 2.0f)) / (float) (bWins + rWins + draws) * 100) + "%");
        }
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("microRTS Game Disruptor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel p1 = new JPanel();
        p1.setLayout(new GridLayout(12,2));
        p1.setBorder(new EmptyBorder(20, 20, 20, 20));

        p1.add( new JLabel("Blue HP: "));
        p1.add( new JLabel("Red HP: "));

        JTextField blHP = new JTextField();
        blHP.setText(String.valueOf(BlueHP));
        blHP.getDocument().addDocumentListener((SL) e -> {
            BlueHP = Integer.parseInt(blHP.getText());
        });
        p1.add(blHP);

        JTextField rdHP = new JTextField();
        rdHP.setText(String.valueOf(RedHP));
        rdHP.getDocument().addDocumentListener((SL) e -> {
            RedHP = Integer.parseInt(rdHP.getText());
        });
        p1.add(rdHP);

        p1.add( new JLabel("Blue Attack: "));
        p1.add( new JLabel("Red Attack: "));

        JTextField blAtk = new JTextField();
        blAtk.setText(String.valueOf(BlueDamage));
        blAtk.getDocument().addDocumentListener((SL) e -> {
            BlueDamage = Integer.parseInt(blAtk.getText());
        });
        p1.add(blAtk);

        JTextField rdAtk = new JTextField();
        rdAtk.setText(String.valueOf(RedDamage));
        rdAtk.getDocument().addDocumentListener((SL) e -> {
            RedDamage = Integer.parseInt(rdAtk.getText());
        });
        p1.add(rdAtk);

        p1.add( new JLabel("Blue Attack Range: "));
        p1.add( new JLabel("Red Attack Range: "));

        JTextField blAtkRange = new JTextField();
        blAtkRange.setText(String.valueOf(BlueRange));
        blAtkRange.getDocument().addDocumentListener((SL) e -> {
            BlueRange = Integer.parseInt(blAtkRange.getText());
        });
        p1.add(blAtkRange);

        JTextField rdAtkRange = new JTextField();
        rdAtkRange.setText(String.valueOf(RedRange));
        rdAtkRange.getDocument().addDocumentListener((SL) e -> {
            RedRange = Integer.parseInt(rdAtkRange.getText());
        });
        p1.add(rdAtkRange);

        p1.add( new JLabel("Blue Move Time: "));
        p1.add( new JLabel("Red Move Time: "));

        JTextField blMoveTime = new JTextField();
        blAtkRange.setText(String.valueOf(BlueMoveTime));
        blAtkRange.getDocument().addDocumentListener((SL) e -> {
            BlueMoveTime = Integer.parseInt(blMoveTime.getText());
        });
        p1.add(blAtkRange);

        JTextField rdMoveTime = new JTextField();
        rdAtkRange.setText(String.valueOf(RedMoveTime));
        rdAtkRange.getDocument().addDocumentListener((SL) e -> {
            RedMoveTime = Integer.parseInt(rdMoveTime.getText());
        });
        p1.add(rdAtkRange);

        p1.add( new JLabel("Blue Attack Time: "));
        p1.add( new JLabel("Red Attack Time: "));

        JTextField blAttackTime = new JTextField();
        blAtkRange.setText(String.valueOf(BlueAttackTime));
        blAtkRange.getDocument().addDocumentListener((SL) e -> {
            BlueAttackTime = Integer.parseInt(blAttackTime.getText());
        });
        p1.add(blAtkRange);

        JTextField rdAttackTime = new JTextField();
        rdAtkRange.setText(String.valueOf(RedRange));
        rdAtkRange.getDocument().addDocumentListener((SL) e -> {
            RedRange = Integer.parseInt(rdAttackTime.getText());
        });
        p1.add(rdAtkRange);

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
        simCount.setText("1000");
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
                        RunSimulation(false);
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
                        RunSimulation(true);
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

        frame.add(p1, BorderLayout.CENTER);
        frame.pack();
        frame.setMinimumSize(new Dimension(600, 300));
        frame.setVisible(true);
    }
}
