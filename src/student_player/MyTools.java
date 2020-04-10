package student_player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;
import Saboteur.cardClasses.SaboteurBonus;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurMap;
import Saboteur.cardClasses.SaboteurTile;
import boardgame.Move;


//NB: FOR NOW I FORGET VERIFYLEGIT AND ISLEGAL
public class MyTools {
	
    private static double deckAvail = 41; //55-7-7
    //dont put 8 to prioritize more restrictive tiles
    private static String[] tilesOkToDiscard = {"1","2","2_flip","3","3_flip","4","4_flip","11","11_flip","12","12_flip","13","14","14_flip","15"};
	private static String[] verticTiles = {"0","6","6_flip"};
	private static String[] horizTiles = {"9","9_flip","10"};
	private static String[] turnLeftTiles = {"5","5_flip","6"};
	private static String[] turnRightTiles = {"6_flip","7"};
	//private static String[] turnDownTiles = {"6","6_flip","7_flip","9","8"};
	//private static String[] turnUpTiles = {"9_flip","8"};
	
	
	public static boolean isConnectedToEntrance(SaboteurBoardState boardState, int[] targetPos) {
		ArrayList<int[]> originTargets = new ArrayList<>();
		originTargets.add(new int[]{5,5}); //entrance

		if (isTherePath(originTargets, targetPos, boardState,true)) {
			//next: checks that there is a path of ones.
			ArrayList<int[]> originTargets2 = new ArrayList<>();
			//the starting points
			originTargets2.add(new int[]{5*3+1, 5*3+1});
			originTargets2.add(new int[]{5*3+1, 5*3+2});
			originTargets2.add(new int[]{5*3+1, 5*3});
			originTargets2.add(new int[]{5*3, 5*3+1});
			originTargets2.add(new int[]{5*3+2, 5*3+1});
			//get the target position in 0-1 coordinate
			int[] targetPos2 = {targetPos[0]*3+1, targetPos[1]*3+1};
			if (isTherePath(originTargets2, targetPos2, boardState,false)) {
				return true;
			}
		}

		return false;
	}
    
    public static boolean isTherePath(ArrayList<int[]> originTargets,int[] targetPos,SaboteurBoardState boardState,Boolean usingCard) {
    	ArrayList<int[]> queue = new ArrayList<>(); //will store the current neighboring tile. Composed of position (int[]).
    	ArrayList<int[]> visited = new ArrayList<int[]>(); //will store the visited tile with an Hash table where the key is the position the board.
    	visited.add(targetPos);
    	
    	if(usingCard) addUnvisitedNeighborToQueue(targetPos,queue,visited,14,boardState,usingCard);
    	else addUnvisitedNeighborToQueue(targetPos,queue,visited,14*3,boardState,usingCard);
        while(queue.size()>0){
            int[] visitingPos = queue.remove(0);
            if(containsIntArray(originTargets,visitingPos)){
                return true;
            }
            visited.add(visitingPos);
            if(usingCard) addUnvisitedNeighborToQueue(visitingPos,queue,visited,14,boardState,usingCard);
            else addUnvisitedNeighborToQueue(visitingPos,queue,visited,14*3,boardState,usingCard);
            addUnvisitedNeighborToQueue(visitingPos,queue,visited,14,boardState,usingCard);
           
        }
    	
    	return false;
    }   
    private static void addUnvisitedNeighborToQueue(int[] pos,ArrayList<int[]> queue, ArrayList<int[]> visited,int maxSize, SaboteurBoardState boardState,Boolean usingCard){
        SaboteurTile[][] board = boardState.getHiddenBoard();
        int[][] intBoard = boardState.getHiddenIntBoard();
    	int[][] moves = {{0, -1},{0, 1},{1, 0},{-1, 0}};
        int i = pos[0];
        int j = pos[1];
        for (int m = 0; m < 4; m++) {
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) { //if the hypothetical neighbor is still inside the board
                int[] neighborPos = new int[]{i+moves[m][0],j+moves[m][1]};
                if(!containsIntArray(visited,neighborPos)){ 	
                    if(usingCard && board[neighborPos[0]][neighborPos[1]]!=null) queue.add(neighborPos);
                    else if(!usingCard && intBoard[neighborPos[0]][neighborPos[1]]==1) queue.add(neighborPos);
                }
            }
        }
    }
    private static boolean containsIntArray(ArrayList<int[]> a,int[] o){ //the .equals used in Arraylist.contains is not working between arrays..
        if (o == null) {
            for (int i = 0; i < a.size(); i++) {
                if (a.get(i) == null)
                    return true;
            }
        } else {
            for (int i = 0; i < a.size(); i++) {
                if (Arrays.equals(o, a.get(i)))
                    return true;
            }
        }
        return false;
    }
	
    public static double getSomething() {
        return Math.random();
    }
    
    //keep track number cards available --> deduce if soon tie
    public static void updateCardNumberAvailable() { 	
    	 	deckAvail--;deckAvail--; //to account for opponents turn
    }
    
    
    public static SaboteurMove chooseDrop(ArrayList<SaboteurCard> myCurrentHand,int playerid) { 
    	SaboteurMove drop = null;
    	int nbBonus=0;
    	int i = 0; 
    	for (SaboteurCard card : myCurrentHand) {
    		if(card instanceof SaboteurMap) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		if(card instanceof SaboteurTile) {
    			SaboteurTile tile = (SaboteurTile) card;
    			if(Arrays.asList(tilesOkToDiscard).contains(tile.getIdx()) ) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		}
    		if(card instanceof SaboteurMalus)  return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		if(card instanceof SaboteurBonus) {if(nbBonus>=2) { return new SaboteurMove(new SaboteurDrop(),i,0,playerid);} nbBonus++;}
    		i++;
    	}
    	
    	return drop;
    }
    
    
    public static SaboteurMove searchGold(ArrayList<SaboteurMove> moves, SaboteurTile[][] boardTiles) {
    	SaboteurMove noMove = null;
    	for (SaboteurMove move : moves) {
			if(move.getCardPlayed() instanceof SaboteurMap) {
				SaboteurMove mapMove = move;
				int[] pos = mapMove.getPosPlayed();
				if(boardTiles[pos[0]][pos[1]].getIdx() != "8") continue; 
				return mapMove;									//check 1st leftmost then middle
			}
    	}
    	return noMove;
	}
    
    //If malus on us then play bonus card 
    //or destroy or malus if opponent too close goal
    //or drop card to get bonus if out of 4 still available
    public static SaboteurMove counterMalus(ArrayList<SaboteurCard> mycurrenthand,SaboteurTile[][] boardTiles, int playerid) {
    	SaboteurMove noGoodMove = null;
    	int destroys =0; int maluses=0;
    	SaboteurMove destroyMove = null;
    	SaboteurMove malusMove= null;
    	
    	
    	for (SaboteurCard card : mycurrenthand) {
			if(card instanceof SaboteurBonus) {
				return new SaboteurMove((new SaboteurBonus()),0,0,playerid);
			}
			if(card instanceof SaboteurDestroy && destroys==0) {
					destroys++;
					destroyMove = destroyBlockingTile(boardTiles, playerid);
			}
			if(card instanceof SaboteurMalus && maluses==0) {
				maluses++;
				int opponentId = (playerid+1)%2;
				malusMove = new SaboteurMove((new SaboteurMalus()),0,0,opponentId);
			}
		}
    	if(maluses!=0) return malusMove;
    	else if(destroys!=0 && destroyMove!=null) return destroyMove;
    	
    	
    	return noGoodMove;
    }


    public static SaboteurMove destroyBlockingTile(SaboteurTile[][] boardTiles, int playerid) {
    	System.out.println("DESTROY BLOCKING");
    	for(int i=0;i<14;i++) {
    		if(boardTiles[11][i] != null ) { 
    			if(!(Arrays.asList(verticTiles).contains( boardTiles[11][i].getIdx())) && !(boardTiles[11][i].getIdx().contentEquals("8")) ) { //there is a blocking tile since not vertical
    				return new SaboteurMove(new SaboteurDestroy(),11,i,playerid);}
    		}
    		if(boardTiles[12][i] != null) {
    			if(!(Arrays.asList(horizTiles).contains( boardTiles[12][i].getIdx()))
    			&& !(boardTiles[11][i].getIdx().contentEquals("8")) )  { //there is a blocking tile since not horizontal
    				return new SaboteurMove(new SaboteurDestroy(),12,i,playerid);}
    		}
    	}
    	
    	return null;
    }
    
    
    // /!\ logically X and Y are inverted to our normal way of thinking
    public static SaboteurMove goToNugget(ArrayList<SaboteurMove> moves, int posGoldY, SaboteurBoardState board) {
    	SaboteurMove path = null; //goal find a move that is descendent (closer to nugget : {12,y} w/ y=3,5,7)
    	ArrayList<SaboteurMove> interestingMoves = new ArrayList<>();
    	int[] entrance = {5,5};
    	SaboteurTile[][] boardTiles =board.getHiddenBoard();

    	ArrayList<Integer> xs = new ArrayList<Integer>();
    	ArrayList<Integer> ys = new ArrayList<Integer>();
    	
    	//Consider only tiles, non blocking and connected to entrance
    	for (SaboteurMove move : moves) {
    		if(!(move.getCardPlayed() instanceof SaboteurTile)) continue;    								
    		SaboteurTile tile = (SaboteurTile) move.getCardPlayed();
    		if(Arrays.asList(tilesOkToDiscard).contains(tile.getIdx())) continue; //we dont want to block path    		
    		int[] coord= move.getPosPlayed();
    		int[][] around = { {coord[0],coord[1]+1} , {coord[0],coord[1]-1} , {coord[0]+1,coord[1]} ,{coord[0]-1,coord[1]} };
    		for(int i=0; i<4;i++) {
    			if (0 <= around[i][0] && around[i][0] < 14 && 0 <= around[i][1] && around[i][1] < 14) {  //check still in board

    				if(Arrays.equals(around[i],entrance)) {
    					//	System.out.println("OK WITH ENTRANCE");
    					interestingMoves.add(move); 
    					break;
    				}
    				if( boardTiles[around[i][0]][around[i][1]] != null) {
    					if( !(Arrays.asList(tilesOkToDiscard).contains( boardTiles[around[i][0]][around[i][1]].getIdx() ) )) {
    						if(isConnectedToEntrance(board, around[i])) {
    							interestingMoves.add(move); 
    							xs.add(coord[0]);
    				    		ys.add(coord[1]);
    							break;
    						}
    					}
    				}
    			}
    		}
    		
  //System.out.println("coord " + coord[0] + " " + coord[1]);
    	}
    	
    	//check there is at least 1 good tile
    	if(interestingMoves.isEmpty()) {
    		System.err.println("Better to drop");
    		return null;
    	}
    	
    	
    	double priorityVerticalTiles;
    	double priorityHorizontalTiles;
    	double priorityTurnLeft=1; 
    	double priorityTurnRight=1;    	
    	double isVertic = 1; double isHoriz = 1; double isTurn = 1;
    	double maxX=0;
    	double closestY=0;
    	
    	assert xs.isEmpty() == false;
    	maxX= Collections.max(xs);
    	closestY= ys.stream().min( (y1,y2) -> Math.abs(y1-posGoldY) -  Math.abs(y2-posGoldY)).get();
    	//closestY = (Double) closestY; //necessary for division
    	
    	//set priorities 
    	priorityVerticalTiles = Math.abs(1-maxX/12); 
 System.out.println("vertical  "+priorityVerticalTiles );
    	priorityHorizontalTiles = Math.abs(1 - closestY/posGoldY ); 
 System.out.println("horiz  "+priorityHorizontalTiles );

    	
   
    	
    	double maxHeuristic = 4* deckAvail/41 ; 	//so that if not enough a good move then do a drop it's better ==> acceptance decrease as game go on (NOT SURE GOOD)
//   System.out.println("maxHeuri "+ maxHeuristic);
   
   		//double maxHeuristic =0; //is it better than dropping even if low?
   
    	//FOR ME TESTING:
    	//int[] coordi = {0,0};
    	
    	for (SaboteurMove move : interestingMoves) {
    		SaboteurTile tile = (SaboteurTile) move.getCardPlayed();
    		
    		if(Arrays.asList(verticTiles).contains(tile.getIdx())) isVertic= 2;
    		if(Arrays.asList(horizTiles).contains(tile.getIdx())) isHoriz= 2;
    		
    		
    		//TODO if 0 or negative need to go up
    		
    //from before when wanted small: 	if(priorityVerticalTiles<0.5) priorityHorizontalTiles*=2; //should be prioTurns here
    		
    		int[] pos = move.getPosPlayed();
    		
    		if(pos[0]==11) {
       			if((posGoldY - closestY)<0 && (Arrays.asList(turnLeftTiles).contains(tile.getIdx()) || tile.getIdx().equals("8") )) priorityTurnLeft = 4;
       			else if((posGoldY - closestY)>0 && ( Arrays.asList(turnRightTiles).contains(tile.getIdx()) || tile.getIdx().equals("8")   )){priorityTurnRight= 4; }
       			else if ( isVertic==2 || tile.getIdx().equals("8")) {priorityVerticalTiles = 10;} //isvertic = 2 only if it s a vertic tile
    		}
    		if(pos[0]==12 && ( isHoriz==2 || tile.getIdx().equals("8")) ) { //ishoriz = 2 only if it s a horiz tile
    			priorityHorizontalTiles=10;
    		}
    		double x = pos[0] > 12 ? 11 : pos[0]; //of tile is past objective set prio as is line 11 
    		//int y = posGoldY-pos[1]>= -1 && posGoldY-pos[1]<= 1 ? 5 : 2; //if in horizontal interval then increase prob
    		double testNotZero = Math.abs(posGoldY - pos[1])==0? 1.5 : Math.abs(posGoldY - pos[1]);
    		double y = posGoldY - testNotZero;
    		
    		//bigger the better  MATH.ABS ??
    		double h = x *priorityVerticalTiles*isVertic   +   y *priorityHorizontalTiles*isHoriz*priorityTurnRight*priorityTurnLeft;
   
    		//can be made more efficient
    		if(tile.getIdx().equals("8")) h=4*(x+y);
    		
   System.out.println("NAME "+tile.getName()+" pos "+ pos[0]+" "+ pos[1]+" h " + h);
    		if(h> maxHeuristic ) {
    			
    			if(tile.getIdx().equals("7") &&  boardTiles[pos[0]][pos[1]+1] !=null) { System.out.println("it does catch it 7");continue;} //it would go up : adjacent right tile is connected
    			if(tile.getIdx().equals("5_flip") &&  boardTiles[pos[0]][pos[1]-1] !=null) { System.out.println("it does catch it 5_flip");continue;} //it would go up : adjacent left tile is connected

    			path = move;
    			maxHeuristic = h;
    		}
    	}
    
    	
    	//TODO check if tile 8 still played when needed
    	return path;
    }
    
    
    
}