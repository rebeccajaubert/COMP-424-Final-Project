package student_player;

import java.util.ArrayList;
import java.util.Arrays;

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
	
    private static int deckAvail = 41; //55-7-7
    
    public static void initPosHiddenC(SaboteurBoardState boardState) {
    	
    }
	
    public static double getSomething() {
        return Math.random();
    }
    
    //keep track number cards available --> deduce if soon tie
    public static void updateCardNumberAvailable(SaboteurBoardState boardState) { 	
    	 	deckAvail--;deckAvail--; //to account for opponents turn
    }
    
    public static SaboteurMove chooseDrop(ArrayList<SaboteurCard> myCurrentHand,int playerid) { 
    	SaboteurMove drop = null;
    	int i = 1;
    	String[] tilesOkToDiscard = {"1","2","3","4"};
    	int destroysOrmalus=0;
    	for (SaboteurCard card : myCurrentHand) {
    		if(card instanceof SaboteurMap) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		if(card instanceof SaboteurTile) {
    			SaboteurTile tile = (SaboteurTile) card;
    			if(Arrays.asList(tilesOkToDiscard).contains(tile.getIdx()) ) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    		}
    		if(card instanceof SaboteurDestroy) destroysOrmalus++;
    		if(card instanceof SaboteurMalus) destroysOrmalus++;
    		i++;
    	}
    	i=0;
    	if(destroysOrmalus!=0) {
    		for (SaboteurCard card : myCurrentHand) {
    			if(card instanceof SaboteurDestroy)  return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    			if(card instanceof SaboteurMalus) return new SaboteurMove(new SaboteurDrop(),i,0,playerid);
    			i++;
    		}
    	}
    	return new SaboteurMove(new SaboteurDrop(),1,0,playerid);
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
    public static SaboteurMove counterMalus(ArrayList<SaboteurMove> moves) {
    	SaboteurMove noGoodMove = null;
    	int bonuses = 0;int destroys =0; int maluses=0;
    	SaboteurMove destroyMove = null;
    	SaboteurMove malusMove= null;
    	
    	
    	for (SaboteurMove move : moves) {
			if(move.getCardPlayed() instanceof SaboteurBonus) {
				bonuses++;
				SaboteurMove bonusSaboteurMove = move;
				return bonusSaboteurMove;
			}
			if(move.getCardPlayed() instanceof SaboteurDestroy && destroyMove==null) {
				destroys++;
				destroyMove= move;
			}
			if(move.getCardPlayed() instanceof SaboteurMalus && malusMove==null) {
				maluses++;
				malusMove = move;
			}
		}
    	
    	if(destroys!=0) return destroyMove;
    	else if(maluses!=0) return malusMove;
    	
    	return noGoodMove;
    }


    public static SaboteurMove goToNugget(ArrayList<SaboteurMove> moves, int posGoldY) {
    	SaboteurMove path = null; //goal find a move that is descendent (closer to nugget : {12,y} w/ y=3,5,7)
    	
    	
    	
    	for (SaboteurMove move : moves) {
    		
    		if(!(move.getCardPlayed() instanceof SaboteurTile)) continue;
    		SaboteurTile tile = (SaboteurTile) move.getCardPlayed();
    		
    		int[] pos = move.getPosPlayed();
    		
    	}
    	return path;
    }
    
    
    
}