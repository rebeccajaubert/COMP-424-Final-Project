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


//NB: FOR NOW I FORGET VERIFYLEGIT AND ISLEGAL
public class MyTools {
	
	private static final double OG_LFTOVER_DECK_SIZE = SaboteurCard.getDeck().size() - 7 - 7; 
    private static double deckAvail = SaboteurCard.getDeck().size() - 7 - 7;
    //Categorizing the cards
    private static String[] tilesOkToDiscard = {"1","2","2_flip","3","3_flip","4","4_flip","11","11_flip","12","12_flip","13","14","14_flip","15"};
	private static String[] verticTiles = {"0","6","6_flip"};
	private static String[] horizTiles = {"9","9_flip","10"};
	private static String[] turnLeftTiles = {"5","5_flip","6"};
	private static String[] turnRightTiles = {"5","6_flip","7"};
	private static String[] turnDownTiles = {"5","6","6_flip","7_flip","9","8"};
	
	//We keep track of this global variable in order to compare the previous move to the next
	//This refers to the "Y" coordinate on the board
	private static double maxX=0;
	
	//Method that verifies that tunnel is connected to entrance
	public static boolean isConnectedToEntrance(SaboteurBoardState boardState, int[] targetPos) {
		ArrayList<int[]> originTargets = new ArrayList<>();
		//Entrance
		originTargets.add(new int[]{5,5});

		if (isTherePath(originTargets, targetPos, boardState,true)) {
			//Next: checks that there is a path of ones.
			ArrayList<int[]> originTargets2 = new ArrayList<>();
			//The starting points
			originTargets2.add(new int[]{5*3+1, 5*3+1});
			originTargets2.add(new int[]{5*3+1, 5*3+2});
			originTargets2.add(new int[]{5*3+1, 5*3});
			originTargets2.add(new int[]{5*3, 5*3+1});
			originTargets2.add(new int[]{5*3+2, 5*3+1});
			//Get the target position in 0-1 coordinate
			int[] targetPos2 = {targetPos[0]*3+1, targetPos[1]*3+1};
			if (isTherePath(originTargets2, targetPos2, boardState,false)) {
				return true;
			}
		}

		return false;
	}
    
    public static boolean isTherePath(ArrayList<int[]> originTargets,int[] targetPos,SaboteurBoardState boardState,Boolean usingCard) {
    	 //Will store the current neighboring tile. Composed of position (int[]).
    	ArrayList<int[]> queue = new ArrayList<>();
    	//Will store the visited tile with an Hash table where the key is the position the board.
    	ArrayList<int[]> visited = new ArrayList<int[]>();
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
        	 //If the hypothetical neighbor is still inside the board
            if (0 <= i+moves[m][0] && i+moves[m][0] < maxSize && 0 <= j+moves[m][1] && j+moves[m][1] < maxSize) {
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
    
    //Keep track number cards available in order deduce the game will end in a draw
    public static void updateCardNumberAvailable() { 	
    	 	//Minus two, to account for agent and opponent's move
    		deckAvail =- 2;
    }
    
    //Method that will evaluate what card is best to discard
    public static SaboteurMove chooseDrop(ArrayList<SaboteurCard> myCurrentHand,int playerid) { 
    	SaboteurMove drop = null;
    	int nbBonus=0;
    	int nbDestroy=0;
    	int i = 0; 
    	for (SaboteurCard card : myCurrentHand) {
    		if(card instanceof SaboteurMap) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		if(card instanceof SaboteurTile) {
    			SaboteurTile tile = (SaboteurTile) card;
    			if(Arrays.asList(tilesOkToDiscard).contains(tile.getIdx()) ) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		}
    		if(card instanceof SaboteurMalus)  return new SaboteurMove(new SaboteurMalus(),0,0,playerid);
    		if(card instanceof SaboteurBonus) {if(nbBonus>=1) { return new SaboteurMove(new SaboteurDrop(),i,0,playerid);} nbBonus++; System.out.println("1 bonus ???");}
    		if(card instanceof SaboteurDestroy) {if(nbDestroy>=1) {return new SaboteurMove(new SaboteurDrop(), i, 0, playerid);} }
    		i++;
    	}
    	
    	return drop;
    }
    
    //Method that returns Map move
    public static SaboteurMove searchGold(ArrayList<SaboteurMove> moves, SaboteurTile[][] boardTiles) {
    	SaboteurMove noMove = null;
    	for (SaboteurMove move : moves) {
			if(move.getCardPlayed() instanceof SaboteurMap) {
				SaboteurMove mapMove = move;
				int[] pos = mapMove.getPosPlayed();
				if(boardTiles[pos[0]][pos[1]].getIdx() != "8") continue; 
				return mapMove;									
			}
    	}
    	return noMove;
	}
    
    //Method that determines smartest move after a Malus has been played against them 
    public static SaboteurMove counterMalus(ArrayList<SaboteurCard> mycurrenthand,SaboteurTile[][] boardTiles, int playerid) {
    	SaboteurMove noGoodMove = null;
    	int destroys = 0; 
    	int maluses = 0;
    	SaboteurMove destroyMove = null;
    	SaboteurMove malusMove= null;
    	
    	
    	for (SaboteurCard card : mycurrenthand) {
			//If you have Bonus card, now is the time to play it
    		if(card instanceof SaboteurBonus) {
				return new SaboteurMove((new SaboteurBonus()),0,0,playerid);
			}
			if(card instanceof SaboteurDestroy && destroys==0) {
					destroys++;
					destroyMove = destroyBlockingTileCloseToGoal(boardTiles, playerid);
			}
			if(card instanceof SaboteurMalus && maluses==0) {
				maluses++;
				int opponentId = (playerid+1)%2;
				malusMove = new SaboteurMove((new SaboteurMalus()),0,0,opponentId);
			}
		}
    	//Try to hit them back with another Malus
    	if(maluses!=0) return malusMove;
    	//Otherwise destroy something of theirs!
    	else if(destroys!=0 && destroyMove!=null) return destroyMove;
    	
    	// :( You have nothing to fight back against this Malus
    	return noGoodMove;
    }

    //Method that will check if there is a dead-end/misleading tile near goal, destroy if it can
    public static SaboteurMove destroyBlockingTileCloseToGoal(SaboteurTile[][] boardTiles, int playerid) {
    	for(int i=3;i<=7;i=i+2) { //add condition on top of hidden card
    		if(boardTiles[11][i] != null ) {  
    			if(  !(Arrays.asList(verticTiles).contains( boardTiles[11][i].getIdx())) && !(boardTiles[11][i].getIdx().contentEquals("8")) ) { //there is a blocking tile since not vertical
    				return new SaboteurMove(new SaboteurDestroy(),11,i,playerid);}
    		}
    	}
    	//case by case easier to implement
    	if(boardTiles[12][2] != null) {
			if(!(Arrays.asList(turnRightTiles).contains( boardTiles[12][2].getIdx()))
			&& !(boardTiles[12][2].getIdx().contentEquals("8")) )  { //there is a blocking tile since not horizontal
				return new SaboteurMove(new SaboteurDestroy(),12,2,playerid);}
		}
    	if(boardTiles[12][8] != null) {
			if(!(Arrays.asList(turnLeftTiles).contains( boardTiles[12][8].getIdx()))
			&& !(boardTiles[12][8].getIdx().contentEquals("8")) )  { //there is a blocking tile since not horizontal
				return new SaboteurMove(new SaboteurDestroy(),12,8,playerid);}
		}
    	if(boardTiles[12][4] != null) {
			if(( !(Arrays.asList(turnRightTiles).contains( boardTiles[12][4].getIdx())) || !(Arrays.asList(horizTiles).contains( boardTiles[12][4].getIdx())) || !(Arrays.asList(turnLeftTiles).contains( boardTiles[12][4].getIdx())) )
			&& !(boardTiles[12][4].getIdx().contentEquals("8")) )  { //there is a blocking tile since not horizontal
				return new SaboteurMove(new SaboteurDestroy(),12,4,playerid);}
		}
    	if(boardTiles[12][6] != null) {
			if(( !(Arrays.asList(turnRightTiles).contains( boardTiles[12][6].getIdx())) || !(Arrays.asList(horizTiles).contains( boardTiles[12][6].getIdx())) || !(Arrays.asList(turnLeftTiles).contains( boardTiles[12][6].getIdx())) )
			&& !(boardTiles[12][6].getIdx().contentEquals("8")) )  { //there is a blocking tile since not horizontal
				return new SaboteurMove(new SaboteurDestroy(),12,6,playerid);}
		}
    	
    	return null;
    }
    
    //Destroy tiles that are in the middle of the board  and block a good path
    public static SaboteurMove destroyBlockingMiddleBoard(SaboteurTile[][] boardTiles, int playerid) {
    	//We would like to destroy the tile the is closest to the nugget
    	for(int x=10; x>=6; x--) {
    		for(int y=2;y<=8;y++) { //dont search too far to save computation (nb: x = rows. y= col)
    			SaboteurTile tile = boardTiles[x][y];
    			if( tile != null) {
    				if(Arrays.asList(tilesOkToDiscard).contains(tile.getIdx())) return new SaboteurMove(new SaboteurDestroy(),x, y, playerid);
    			}
    			
    		}
    	}

    	return null;
    }

    
    //**REMINDER**: logically X and Y are inverted to our normal way of thinking
    //Method that finds move that will get agent closer to the nugget
    public static SaboteurMove goToNugget(ArrayList<SaboteurMove> moves, ArrayList<SaboteurCard> myHand, int posGoldY, SaboteurBoardState board) {
    	 //if the hypothetical neighbor is still inside the board
    	SaboteurMove path = null;
    	ArrayList<SaboteurMove> interestingMoves = new ArrayList<>();
    	int[] entrance = {5,5};
    	SaboteurTile[][] boardTiles =board.getHiddenBoard();

    	ArrayList<Integer> xs = new ArrayList<Integer>();
    	ArrayList<Integer> ys = new ArrayList<Integer>();
    	
    	//Consider only tiles, non blocking and connected to entrance AND not above entrance
    	for (SaboteurMove move : moves) {
    		if(!(move.getCardPlayed() instanceof SaboteurTile)) continue;    								
    		SaboteurTile tile = (SaboteurTile) move.getCardPlayed();
    		if(Arrays.asList(tilesOkToDiscard).contains(tile.getIdx())) continue; //we dont want to block path    		
    		int[] coord= move.getPosPlayed();
    		int[][] around = { {coord[0],coord[1]+1} , {coord[0],coord[1]-1} , {coord[0]+1,coord[1]} ,{coord[0]-1,coord[1]} };
    		for(int i=0; i<4;i++) {
    			if (0 <= around[i][0] && around[i][0] < 14 && 0 <= around[i][1] && around[i][1] < 14) {  //check still in board

    				if(  coord[0]>=5 && Arrays.equals(around[i],entrance)) {
    					interestingMoves.add(move); 
    					xs.add(coord[0]);
    					ys.add(coord[1]);
    					break;
    				}
    				if(coord[0]>=5 &&  boardTiles[around[i][0]][around[i][1]] != null ) {
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

    	}

    	//Rare that you will enter this...
    	//but if no good tiles in hand, could be better to drop (i.e nothing get returned from this method)
    	if(interestingMoves.isEmpty()) {
    		return null;
    	}

    	//EVALUATING AND PRIORITZING TILES BEGINS
    	
    	double priorityVerticalTiles;
    	double priorityHorizontalTiles;
    	double priorityTurnLeft= 1; 
    	double priorityTurnRight= 1; 
    	double priorityTurnDown = 1;
    	double isVertic = 1; double isHoriz = 1;
    	double prevMaxX= 0;
    	double closestY= 0;
    	
    	prevMaxX = maxX;

    	
    	if(xs.size() == 1) maxX= xs.get(0);
    	else{maxX= Collections.max(xs);}
    	
    	//If there is a blocking tile
    	if(prevMaxX > maxX) { 
    		//Expensive search
    		if(myHand.contains(new SaboteurDestroy())) {	
    			SaboteurMove canDestroy = null;
    			if(prevMaxX>10) canDestroy = destroyBlockingTileCloseToGoal(boardTiles,board.getTurnPlayer());
    			else {
    				canDestroy = destroyBlockingMiddleBoard(boardTiles, board.getTurnPlayer());
    			}
    			if(canDestroy != null) return canDestroy;
    		}
    	}

    	//This is increases/decreases priority based on how close we are to nugget
    	closestY= ys.stream().min( (y1,y2) -> Math.abs(y1-posGoldY) -  Math.abs(y2-posGoldY)).get();

    	//Evaluating the priority of the vertical tunnels and horizontal tunnel
    	
    	//Here we multiply by 2 to emphasize that going down the board is much more important
    	priorityVerticalTiles = 1-maxX/12 == 0 ? 0.1 : Math.abs(1-maxX/12) * 2 ;
    	
    	//Very similar to vertical tile priority, except is not multiplied by two 
    	priorityHorizontalTiles = Math.abs(1 - closestY/posGoldY ) ==0 ? 0.1 :  Math.abs(1 - closestY/posGoldY ); 

    	//So that if not enough a good move is available then do a drop it's better
    	double maxHeuristic = 2.8 * deckAvail/OG_LFTOVER_DECK_SIZE; 

    	for (SaboteurMove move : interestingMoves) {
    		SaboteurTile tile = (SaboteurTile) move.getCardPlayed();

    		if(Arrays.asList(verticTiles).contains(tile.getIdx())) isVertic= 2;
    		if(Arrays.asList(horizTiles).contains(tile.getIdx())) isHoriz= 2;

    		int[] pos = move.getPosPlayed();

    	
    		if((posGoldY - closestY) < 0 && (Arrays.asList(turnLeftTiles).contains(tile.getIdx()) 
    				|| tile.getIdx().equals("8") )) 
    														priorityTurnLeft = 4;
    		else if((posGoldY - closestY) > 0 && ( Arrays.asList(turnRightTiles).contains(tile.getIdx()) 
    					|| tile.getIdx().equals("8")   ))
    														{priorityTurnRight= 4; }
    		
    		
    		if(pos[0]==11) {
    			if(Arrays.asList(turnDownTiles).contains(tile.getIdx())) priorityTurnDown =8;
    			
    			if(priorityTurnLeft==4) priorityTurnLeft*=2;
    			else if(priorityTurnRight==4) priorityTurnRight*=2;
    			else if ( isVertic==2 || tile.getIdx().equals("8")) {priorityVerticalTiles = 10;} 
    		}
    			
    		if(pos[0]==12 && ( isHoriz==2 || tile.getIdx().equals("8")) ) { //ishoriz = 2 only if it s a horiz tile
    			priorityHorizontalTiles=10;
    		}
    		
    		double x = pos[0];
    		double testNotZero = Math.abs(posGoldY - pos[1])==0? 1.5 : Math.abs(posGoldY - pos[1]);
    		double y = posGoldY - testNotZero;

    		//h refers to a heuristic threshold for a good move
    		//Multiplying each coordinate by its priority
    		double h = x *priorityVerticalTiles*isVertic   +   y*priorityHorizontalTiles*isHoriz*priorityTurnRight*priorityTurnLeft*priorityTurnDown;

    		//Can be made more efficient
    		//Since 8 is not in either category of tiles, we don't want to discard
    		//multiplying by 4 is quite high, won't be played at the beginning of the game, but more so towards the end
    		if(tile.getIdx().equals("8")) h=4*(x+y);

    		//if the heuristic of the move just played (h) is higher than max then enter loop
    		if(h > maxHeuristic) {

    			if(tile.getIdx().equals("7") &&  boardTiles[pos[0]][pos[1]+1] != null 
    					&& (pos[0]!=12  )) { continue;} //it would go up : adjacent right tile is connected
    			else if(tile.getIdx().equals("5_flip") &&  boardTiles[pos[0]][pos[1]-1] !=null
    					&& (pos[0]!=12 ) ) { continue;} //it would go up : adjacent left tile is connected
    			else if((tile.getIdx().equals("10")||tile.getIdx().equals("9_flip")
    					&& (pos[0]!=12 ) ) && pos[0]==11 && (pos[1]==3||pos[1]==5||pos[1]==7) ){continue;}

    			path = move;
    			maxHeuristic = h;
    		}
    	}

    	return path;
    }
    
    
    
}