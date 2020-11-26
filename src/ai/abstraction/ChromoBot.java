/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates and open the template in the editor.
 */
package ai.abstraction;

import ai.abstraction.pathfinding.AStarPathFinding;
import ai.core.AI;
import ai.abstraction.pathfinding.PathFinding;
import ai.core.ParameterSpecification;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import rts.GameState;
import rts.PhysicalGameState;
import rts.Player;
import rts.PlayerAction;
import rts.units.*;

/**
 *
 * @author ben, copied form "LightRush.java"
 */
public class ChromoBot extends AbstractionLayerAI {
    
    Random r = new Random();
    protected UnitTypeTable utt;
    UnitType workerType;
    UnitType baseType;
    UnitType barracksType;
    UnitType lightType;

    // Strategy implemented by this class:
    // If we have any "light": send it to attack to the nearest enemy unit
    // If we have a base: train worker until we have 1 workers
    // If we have a barracks: train light
    // If we have a worker: do this if needed: build base, build barracks, harvest resources

    public ChromoBot(UnitTypeTable a_utt) {
        this(a_utt, new AStarPathFinding());
    }
    
    
    public ChromoBot(UnitTypeTable a_utt, PathFinding a_pf) {
        super(a_pf);
        reset(a_utt);
    }

    public void reset() { // called once at beginning of game
        // add chromosome units here?
    	super.reset();
    }
    
    public void reset(UnitTypeTable a_utt)  
    {
        utt = a_utt;
        workerType = utt.getUnitType("Worker");
        baseType = utt.getUnitType("Base");
        barracksType = utt.getUnitType("Barracks");
        lightType = utt.getUnitType("Light");
        // or chromosome here?
    }   
    

    public AI clone() {
        return new ChromoBot(utt, pf);
    }

    /*
        This is the main function of the AI. It is called at each game cycle with the most up to date game state and
        returns which actions the AI wants to execute in this cycle.
        The input parameters are:
        - player: the player that the AI controls (0 or 1)
        - gs: the current game state
        This method returns the actions to be sent to each of the units in the gamestate controlled by the player,
        packaged as a PlayerAction.
     */
    public PlayerAction getAction(int player, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Player p = gs.getPlayer(player);
//        System.out.println("ChromoBotAI for player " + player + " (cycle " + gs.getTime() + ")");

        // behavior of bases:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == baseType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                baseBehavior(u, p, pgs);
            }
        }

        // behavior of barracks:
        for (Unit u : pgs.getUnits()) {
            if (u.getType() == barracksType
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                barracksBehavior(u, p, pgs);
            }
        }

        // behavior of melee units:
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canAttack && !u.getType().canHarvest
                    && u.getPlayer() == player
                    && gs.getActionAssignment(u) == null) {
                meleeUnitBehavior(u, p, gs);
            }
        }

        // behavior of workers:
        List<Unit> workers = new LinkedList<>();
        for (Unit u : pgs.getUnits()) {
            if (u.getType().canHarvest
                    && u.getPlayer() == player) {
                workers.add(u);
            }
        }
        workersBehavior(workers, p, gs);

        // This method simply takes all the unit actions executed so far, and packages them into a PlayerAction
        return translateActions(player, gs);
    }

    public void baseBehavior(Unit u, Player p, PhysicalGameState pgs) {
        int nworkers = 0;
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == workerType
                    && u2.getPlayer() == p.getID()) {
                nworkers++;
            }
        }
        if (nworkers < 1 && p.getResources() >= workerType.cost) {
            train(u, workerType);
        }
    }

    public void barracksBehavior(Unit u, Player p, PhysicalGameState pgs) {
        if (p.getResources() >= lightType.cost) {
            train(u, lightType);
        }
    }

    // NOTES run thru "pgs.getUnits()" once
    // collect targetted base & closest enemy
    // if defense then check closest enemy not too far
    // else is closest too close
    // else go for relevant base
    // TODO or other base if goal is gone, including defenders if no mobile enemy left
    public void meleeUnitBehavior(Unit u, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        Unit closestEnemy = null;
        Unit targettedBase = null;
        int closestDistance = 0;
        int mybase = 0;
        //Boolean mobile = false; // Any mobile enemy left?
        for (Unit u2 : pgs.getUnits()) {
            if (u2.getPlayer() ==  1) {
                /*if (u2.getType().canMove) // TODO sure?
                {
                    mobile = true;
                }*/
                int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                if (closestEnemy == null || d < closestDistance) {
                    closestEnemy = u2;
                    closestDistance = d;
                }
            } // TODO how to go for another base? it seems to when no enemy units from start
            if (u2.getID() == 10 && u.getID() >= 2000 && u.getID() <= 2999) {
                targettedBase = u2;
            } else if (u2.getID() == 30 && u.getID() >= 3000 && u.getID() <= 3999) {
                targettedBase = u2;
            } else if (u2.getID() == 40 && u.getID() >= 4000 && u.getID() <= 4999) {
                targettedBase = u2;
            }
        }
        if (u.getID() >= 1000 && u.getID() <= 1999){
            // ie we're defence so don't go too far TODO unless no mobile left?
            if ((Math.abs(closestEnemy.getX() - u.getX()) < 3) && (Math.abs(closestEnemy.getY() - u.getY()) < 3)) {
                attack(u, closestEnemy);
            } else {
                attack(u, null); // NOTE this stops it from going too far aflield
            }
        } else if ((Math.abs(closestEnemy.getX() - u.getX()) < 3) && (Math.abs(closestEnemy.getY() - u.getY()) < 3)) {
            // ie its too close, attack it
            attack(u, closestEnemy);
        } else if (targettedBase != null) {
            // otherwise attack assigned base
            attack(u, targettedBase);
        } else {
            attack(u, null);
            // TODO another base?
            //attack(u, closestEnemy);
        }
    }

    public void workersBehavior(List<Unit> workers, Player p, GameState gs) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int nbases = 0;
        int nbarracks = 0;

        int resourcesUsed = 0;
        List<Unit> freeWorkers = new LinkedList<>(workers);

        if (workers.isEmpty()) {
            return;
        }

        for (Unit u2 : pgs.getUnits()) {
            if (u2.getType() == baseType
                    && u2.getPlayer() == p.getID()) {
                nbases++;
            }
            if (u2.getType() == barracksType
                    && u2.getPlayer() == p.getID()) {
                nbarracks++;
            }
        }

        List<Integer> reservedPositions = new LinkedList<>();
        if (nbases == 0 && !freeWorkers.isEmpty()) {
            // build a base:
            if (p.getResources() >= baseType.cost + resourcesUsed) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u,baseType,u.getX(),u.getY(),reservedPositions,p,pgs);
                resourcesUsed += baseType.cost;
            }
        }

        if (nbarracks == 0) {
            // build a barracks:
            if (p.getResources() >= barracksType.cost + resourcesUsed && !freeWorkers.isEmpty()) {
                Unit u = freeWorkers.remove(0);
                buildIfNotAlreadyBuilding(u,barracksType,u.getX(),u.getY(),reservedPositions,p,pgs);
                resourcesUsed += barracksType.cost;
            }
        }


        // harvest with all the free workers:
        List<Unit> stillFreeWorkers = new LinkedList<>();
        for (Unit u : freeWorkers) {
            Unit closestBase = null;
            Unit closestResource = null;
            int closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isResource) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestResource == null || d < closestDistance) {
                        closestResource = u2;
                        closestDistance = d;
                    }
                }
            }
            closestDistance = 0;
            for (Unit u2 : pgs.getUnits()) {
                if (u2.getType().isStockpile && u2.getPlayer()==p.getID()) {
                    int d = Math.abs(u2.getX() - u.getX()) + Math.abs(u2.getY() - u.getY());
                    if (closestBase == null || d < closestDistance) {
                        closestBase = u2;
                        closestDistance = d;
                    }
                }
            }
            boolean workerStillFree = true;
            if (u.getResources() > 0) {
                if (closestBase!=null) {
                    AbstractAction aa = getAbstractAction(u);
                    if (aa instanceof Harvest) {
                        Harvest h_aa = (Harvest)aa;
                        if (h_aa.base!=closestBase) harvest(u, null, closestBase);
                    } else {
                        harvest(u, null, closestBase);
                    }
                    workerStillFree = false;
                }
            } else {            
                if (closestResource!=null && closestBase!=null) {
                    AbstractAction aa = getAbstractAction(u);
                    if (aa instanceof Harvest) {
                        Harvest h_aa = (Harvest)aa;
                        if (h_aa.target != closestResource || h_aa.base!=closestBase) harvest(u, closestResource, closestBase);
                    } else {
                        harvest(u, closestResource, closestBase);
                    }
                    workerStillFree = false;
                }
            }
            
            if (workerStillFree) stillFreeWorkers.add(u);            
        }
        
        for(Unit u:stillFreeWorkers) meleeUnitBehavior(u, p, gs);        
    }

    
    @Override
    public List<ParameterSpecification> getParameters()
    {
        List<ParameterSpecification> parameters = new ArrayList<>();
        
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }
    
}
