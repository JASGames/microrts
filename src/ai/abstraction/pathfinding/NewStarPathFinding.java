/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.abstraction.pathfinding;

import ai.abstraction.ChromoBot;
import rts.*;
import rts.units.Unit;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Vector;

/**
 *
 * @author santi
 * 
 * A* pathfinding. 
 * 
 * The code looks a bit weird, since this version of A* uses static data structures to avoid any
 * memory allocation penalty. It only reallocates memory when asked to path-find for first time,
 * or in a map that is bigger than the previous time. 
 * 
 */
public class NewStarPathFinding extends PathFinding {
    
    public static int iterations = 0;   // this is a debugging variable    
    public static int accumlength = 0;   // this is a debugging variable

    public static int usedPrevPath = 0;
    public static int calcPath = 0;
    
    Boolean free[][];
    int closed[];
    int open[];  // open list
    int heuristic[];     // heuristic value of the elements in 'open'
    int parents[];
    int cost[];     // cost of reaching a given position so far
    int inOpenOrClosed[];
    int openinsert = 0;

    HashMap<Integer, Unit> UnitAtLocation = new HashMap<>();
    HashMap<Integer, Integer> UnitFreeMoves = new HashMap<>();
    HashMap<Unit, ArrayList<Integer>> PastPaths = new HashMap<>();

    // This fucntion finds the shortest path from 'start' to 'targetpos' and then returns
    // a UnitAction of the type 'actionType' with the direction of the first step in the shorteet path
    public UnitAction findPath(Unit start, int targetpos, GameState gs, ResourceUsage ru) {        
        return findPathToPositionInRange(start,targetpos,0,gs,ru);
    }

    public UnitAction pastPathAction(Unit start, int startPos, int w, int targetpos, int range){
        ArrayList<Integer> prevPath = PastPaths.get(start);

        if(prevPath != null && (Math.abs(prevPath.get(0) - targetpos) <= range))
        {
            int newpos = prevPath.remove(prevPath.size()-1);

            if(newpos == startPos && prevPath.size() > 0){
                newpos = prevPath.remove(prevPath.size()-1);
            }

            if(prevPath.size() == 0)
                PastPaths.remove(start);

            if (newpos == startPos+w ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_DOWN);
            if (newpos == startPos-1 ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_LEFT);
            if (newpos == startPos-w ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_UP);
            if (newpos == startPos+1 ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_RIGHT);

            PastPaths.remove(start);
        }else if(prevPath != null) {
            PastPaths.remove(start);
        }

        return null;
    }

    public void calculateFreeMoves(PhysicalGameState pgs, GameState gs){
        UnitAtLocation.clear();
        UnitFreeMoves.clear();
        for(Unit u : gs.getUnits()) { // Loop over the units
            UnitAtLocation.put(u.getPosition(pgs), u);
        }

        for(Unit u : gs.getUnits()){
            int freeMoves = 0;
            int x = u.getX();
            int y = u.getY();

            if(u.getType().canMove) {
                if (x > 0 && UnitAtLocation.get((x - 1) + pgs.getWidth() * (y)) == null && pgs.getTerrain(x - 1, y) == PhysicalGameState.TERRAIN_NONE)
                    freeMoves++;
                if (x < pgs.getWidth() - 1 && UnitAtLocation.get((x + 1) + pgs.getWidth() * (y)) == null && pgs.getTerrain(x + 1, y) == PhysicalGameState.TERRAIN_NONE)
                    freeMoves++;
                if (y > 0 && UnitAtLocation.get((x) + pgs.getWidth() * (y - 1)) == null && pgs.getTerrain(x, y - 1) == PhysicalGameState.TERRAIN_NONE)
                    freeMoves++;
                if (y < pgs.getHeight() - 1 && UnitAtLocation.get((x) + pgs.getWidth() * (y + 1)) == null && pgs.getTerrain(x, y + 1) == PhysicalGameState.TERRAIN_NONE)
                    freeMoves++;
            }

            UnitFreeMoves.put(u.getPosition(pgs), freeMoves);
        }
    }

    public UnitAction pathFindingLoop(Unit start, int w, int h, int startPos, int targetpos, int range, ResourceUsage ru, PhysicalGameState pgs){
        if (free==null || free.length<w*h) {
            free = new Boolean[w][h];
            closed = new int[w*h];
            open = new int[w*h];
            heuristic = new int[w*h];
            parents = new int[w*h];
            inOpenOrClosed = new int[w*h];
            cost = new int[w*h];
        }
        for(int y = 0, i = 0;y<h;y++) {
            for(int x = 0;x<w;x++,i++) {
                free[x][y] = null;
                closed[i] = -1;
                inOpenOrClosed[i] = 0;
            }
        }
        if (ru!=null) {
            for(int pos:ru.getPositionsUsed()) {
                free[pos%w][pos/w] = false; // TODO quicker way using this?
            }
        }
        int targetx = targetpos%w;
        int targety = targetpos/w;
        int sq_range = range*range;

        assert(targetx>=0);
        assert(targetx<w);
        assert(targety>=0);
        assert(targety<h);
        assert(start.getX()>=0);
        assert(start.getX()<w);
        assert(start.getY()>=0);
        assert(start.getY()<h);

        openinsert = 0;
        open[openinsert] = startPos;
        heuristic[openinsert] = manhattanDistance(start.getX(), start.getY(), targetx, targety);
        parents[openinsert] = startPos;
        inOpenOrClosed[startPos] = 1;
        cost[startPos] = 0;
        openinsert++;
//        System.out.println("Looking for path from: " + start.getX() + "," + start.getY() + " to " + targetx + "," + targety);
        while(openinsert>0) {

            iterations++;
            openinsert--;
            int pos = open[openinsert];
            int parent = parents[openinsert];
            if (closed[pos]!=-1) continue;
            closed[pos] = parent;

            int x = pos%w;
            int y = pos/w;

            if (((x-targetx)*(x-targetx)+(y-targety)*(y-targety))<=sq_range) {
                // path found, backtrack:
                ArrayList<Integer> pathPos = new ArrayList<>();
                int last = pos;
                //pathPos.add(last);
//                System.out.println("- Path from " + start.getX() + "," + start.getY() + " to " + targetpos%w + "," + targetpos/w + " (range " + range + ") in " + iterations + " iterations");
                while(parent!=pos) {
                    pathPos.add(last);
                    last = pos;
                    pos = parent;
                    parent = closed[pos];
                    accumlength++;
                    //pathPos.add(pos);
//                    System.out.println("    " + pos%w + "," + pos/w);
                } // NB added the gs.free bits below

                pathPos.add(last);

                if(pathPos.size() > 0) {
                    PastPaths.put(start, pathPos);
                }

                if (last == pos+w ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_DOWN);
                if (last == pos-1 ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_LEFT);
                if (last == pos-w ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_UP);
                if (last == pos+1 ) return new UnitAction(UnitAction.TYPE_MOVE, UnitAction.DIRECTION_RIGHT);
                return null;
            }
            if (y>0 && inOpenOrClosed[pos-w] == 0) {
                if (free[x][y-1]==null) free[x][y-1]=(tfree(x, y-1, targetx, targety, pgs, start));//gs.free(x, y-1);
                assert(free[x][y-1]!=null);
                if (free[x][y-1]) {
                    addToOpen(x,y-1,pos-w,pos,manhattanDistance(x, y-1, targetx, targety)+penalty(x, y-1, pgs, start));
                }
            }
            if (x<pgs.getWidth()-1 && inOpenOrClosed[pos+1] == 0) {
                if (free[x+1][y]==null) free[x+1][y]=(tfree(x+1, y, targetx, targety, pgs, start));//gs.free(x+1, y);
                assert(free[x+1][y]!=null);
                if (free[x+1][y]) {
                    addToOpen(x+1,y,pos+1,pos,manhattanDistance(x+1, y, targetx, targety)+penalty(x+1, y, pgs, start));
                }
            }
            if (y<pgs.getHeight()-1 && inOpenOrClosed[pos+w] == 0) {
                if (free[x][y+1]==null) free[x][y+1]=(tfree(x, y+1, targetx, targety, pgs, start));//gs.free(x, y+1);
                assert(free[x][y+1]!=null);
                if (free[x][y+1]) {
                    addToOpen(x,y+1,pos+w,pos,manhattanDistance(x, y+1, targetx, targety)+penalty(x, y+1, pgs, start));
                }
            }
            if (x>0 && inOpenOrClosed[pos-1] == 0) {
                if (free[x-1][y]==null) free[x-1][y]=(tfree(x-1, y, targetx, targety, pgs, start));//gs.free(x-1, y);
                assert(free[x-1][y]!=null);
                if (free[x-1][y]) {
                    addToOpen(x-1,y,pos-1,pos,manhattanDistance(x-1, y, targetx, targety)+penalty(x-1, y, pgs, start));
                }
            }
        }
        return null;
    }
    
    /*
     * This function is like the previous one, but doesn't try to reach 'target', but just to 
     * reach a position that is at most 'range' far away from 'target'
     */
    public UnitAction findPathToPositionInRange(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        int h = pgs.getHeight();
        int startPos = start.getY()*w + start.getX();

        UnitAction pastAction = pastPathAction(start, startPos, w, targetpos, range);

        if(pastAction != null){
            usedPrevPath++;
            return pastAction;
        }

        calcPath++;

        calculateFreeMoves(pgs, gs);

        return pathFindingLoop(start, w, h, startPos, targetpos, range, ru, pgs);
    }

    private boolean tfree(int x, int y, int targetx, int targety, PhysicalGameState pgs, Unit start){
        int pos = (pgs.getWidth() * y) + x;
        Unit u = UnitAtLocation.get(pos);

        if( u != null && u.getPlayer() != start.getPlayer()){
            int d = Math.abs(targetx - u.getX()) + Math.abs(targety - u.getY());
            if(d > 25) {
                return false;
            }
        }

        return pgs.getTerrain(x, y) == PhysicalGameState.TERRAIN_NONE;
    }

    private int penalty(int x, int y, PhysicalGameState pgs, Unit start){
        int p = 0;
        int pos = (pgs.getWidth() * y) + x;
        Unit u = UnitAtLocation.get(pos);

        if(u != null)
        {
            // Add a penalty
            p = 1;

            // Count the free moves the target unit has
            int freeMoves = UnitFreeMoves.get(pos);

            // Increase the penalty by the number of available moves less than 4
            p += 4 - freeMoves;
        }

        return p;
    }
    
    /*
     * This function is like the previous one, but doesn't try to reach 'target', but just to 
     * reach a position adjacent to 'target'
     */
    public UnitAction findPathToAdjacentPosition(Unit start, int targetpos, GameState gs, ResourceUsage ru) {
        return findPathToPositionInRange(start, targetpos, 1, gs, ru);
    }      

    public boolean pathExists(Unit start, int targetpos, GameState gs, ResourceUsage ru) {
        return start.getPosition(gs.getPhysicalGameState()) == targetpos
            || findPath(start, targetpos, gs, ru) != null;
    }
    

    public boolean pathToPositionInRangeExists(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        int x = targetpos%gs.getPhysicalGameState().getWidth();
        int y = targetpos/gs.getPhysicalGameState().getWidth();
        int d = (x-start.getX())*(x-start.getX()) + (y-start.getY())*(y-start.getY());
        return d <= range * range
            || findPathToPositionInRange(start, targetpos, range, gs, ru) != null;
    }
    
    // and keep the "open" list sorted:
    public void addToOpen(int x, int y, int newPos, int oldPos, int h) {
        cost[newPos] = cost[oldPos]+1;
        
        // find the right position for the insert:
        for(int i = openinsert-1;i>=0;i--) {
            if (heuristic[i]+cost[open[i]]>=h+cost[newPos]) {
//                System.out.println("Inserting at " + (i+1) + " / " + openinsert);
                // shift all the elements:
                for(int j = openinsert;j>=i+1;j--) {
                    open[j] = open[j-1];
                    heuristic[j] = heuristic[j-1];
                    parents[j] = parents[j-1];
                }
                
                // insert at i+1:
                open[i+1] = newPos;
                heuristic[i+1] = h;
                parents[i+1] = oldPos;
                openinsert++;
                inOpenOrClosed[newPos] = 1;
                return;
            }
        }        
        // i = -1;
//        System.out.println("Inserting at " + 0 + " / " + openinsert);
        // shift all the elements:
        for(int j = openinsert;j>=1;j--) {
            open[j] = open[j-1];
            heuristic[j] = heuristic[j-1];
            parents[j] = parents[j-1];
        }

        // insert at i+1:
        open[0] = newPos;
        heuristic[0] = h;
        parents[0] = oldPos;
        openinsert++;
        inOpenOrClosed[newPos] = 1;
    }


    public int manhattanDistance(int x, int y, int x2, int y2) {
        return Math.abs(x-x2) + Math.abs(y-y2);
    }
     
    public int findDistToPositionInRange(Unit start, int targetpos, int range, GameState gs, ResourceUsage ru) {
        PhysicalGameState pgs = gs.getPhysicalGameState();
        int w = pgs.getWidth();
        int h = pgs.getHeight();
        if (free==null || free.length<w*h) {
            free = new Boolean[pgs.getWidth()][pgs.getHeight()];        
            closed = new int[pgs.getWidth()*pgs.getHeight()];
            open = new int[pgs.getWidth()*pgs.getHeight()];
            heuristic = new int[pgs.getWidth()*pgs.getHeight()];
            parents = new int[pgs.getWidth()*pgs.getHeight()];
            inOpenOrClosed = new int[pgs.getWidth()*pgs.getHeight()];
            cost = new int[pgs.getWidth()*pgs.getHeight()];
        }
        for(int y = 0, i = 0;y<pgs.getHeight();y++) {
            for(int x = 0;x<w;x++,i++) {
                free[x][y] = null;
                closed[i] = -1;           
                inOpenOrClosed[i] = 0;
            }
        }
        if (ru!=null) {
            for(int pos:ru.getPositionsUsed()) {
                free[pos%w][pos/w] = false;
            }
        }
        int targetx = targetpos%w;
        int targety = targetpos/w;
        int sq_range = range*range;
        int startPos = start.getY()*w + start.getX();
        
        assert(targetx>=0);
        assert(targetx<w);
        assert(targety>=0);
        assert(targety<h);
        assert(start.getX()>=0);
        assert(start.getX()<w);
        assert(start.getY()>=0);
        assert(start.getY()<h);
        
        openinsert = 0;
        open[openinsert] = startPos;
        heuristic[openinsert] = manhattanDistance(start.getX(), start.getY(), targetx, targety);
        parents[openinsert] = startPos;
        inOpenOrClosed[startPos] = 1;
        cost[startPos] = 0;
        openinsert++;
//        System.out.println("Looking for path from: " + start.getX() + "," + start.getY() + " to " + targetx + "," + targety);
        while(openinsert>0) {
            
            // debugging code:
            /*
            System.out.println("open: ");
            for(int i = 0;i<openinsert;i++) {
                System.out.print(" [" + (open[i]%w) + "," + (open[i]/w) + " -> "+ cost[open[i]] + "+" + heuristic[i] + "]");
            }
            System.out.println("");
            for(int i = 0;i<h;i++) {
                for(int j = 0;j<w;j++) {
                    if (j==start.getX() && i==start.getY()) {
                        System.out.print("s");
                    } else if (j==targetx && i==targety) {
                        System.out.print("t");
                    } else if (!free[j][i]) {
                        System.out.print("X");
                    } else {
                        if (inOpenOrClosed[j+i*w]==0) { 
                            System.out.print(".");
                        } else {
                            System.out.print("o");
                        }
                    }
                }
                System.out.println("");
            }
            */
            iterations++;
            openinsert--;
            int pos = open[openinsert];
            int parent = parents[openinsert];
            if (closed[pos]!=-1) continue;            
            closed[pos] = parent;

            int x = pos%w;
            int y = pos/w;

            if (((x-targetx)*(x-targetx)+(y-targety)*(y-targety))<=sq_range) {
                // path found, backtrack:
                //System.out.println("- Path from " + start.getX() + "," + start.getY() + " to " + targetpos%w + "," + targetpos/w + " (range " + range + ") in " + iterations + " iterations");
                int temp = 0;
                while(parent!=pos) {
                    pos = parent;
                    parent = closed[pos];
                    accumlength++;
                    temp++;
                    //System.out.println("    " + pos%w + "," + pos/w);
                }
                return temp;
                
            }
            if (y>0 && inOpenOrClosed[pos-w] == 0) {
                if (free[x][y-1]==null) free[x][y-1]=gs.free(x, y-1);
                assert(free[x][y-1]!=null);
                if (free[x][y-1]) {
                    addToOpen(x,y-1,pos-w,pos,manhattanDistance(x, y-1, targetx, targety));
                }
            }
            if (x<pgs.getWidth()-1 && inOpenOrClosed[pos+1] == 0) {
                if (free[x+1][y]==null) free[x+1][y]=gs.free(x+1, y);
                assert(free[x+1][y]!=null);
                if (free[x+1][y]) {
                    addToOpen(x+1,y,pos+1,pos,manhattanDistance(x+1, y, targetx, targety));
                }
            }
            if (y<pgs.getHeight()-1 && inOpenOrClosed[pos+w] == 0) {
                if (free[x][y+1]==null) free[x][y+1]=gs.free(x, y+1);
                assert(free[x][y+1]!=null);
                if (free[x][y+1]) {
                    addToOpen(x,y+1,pos+w,pos,manhattanDistance(x, y+1, targetx, targety));
                }
            }
            if (x>0 && inOpenOrClosed[pos-1] == 0) {
                if (free[x-1][y]==null) free[x-1][y]=gs.free(x-1, y);
                assert(free[x-1][y]!=null);
                if (free[x-1][y]) {
                    addToOpen(x-1,y,pos-1,pos,manhattanDistance(x-1, y, targetx, targety));
                }
            }              
        }
        return -1;
    }
}
