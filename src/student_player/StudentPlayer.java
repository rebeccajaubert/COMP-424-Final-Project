package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurTile;
import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

import java.util.ArrayList;


/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {
	private boolean goldFound = false;
	
	/**
	 * You must modify this constructor to return your student number. This is
	 * important, because this is what the code that runs the competition uses to
	 * associate you with your agent. The constructor should do nothing else.
	 */
	public StudentPlayer() {
		super("260729564");
	}

	/**
	 * This is the primary method that you need to implement. The ``boardState``
	 * object contains the current state of the game, which your agent must use to
	 * make decisions.
	 */
	public Move chooseMove(SaboteurBoardState boardState) {
		
		//GATHERING INFO
		
		ArrayList<SaboteurCard> myCurrentHand =  boardState.getCurrentPlayerCards();
		ArrayList<SaboteurMove> moves = boardState.getAllLegalMoves();
		int playerid = boardState.getTurnPlayer();
		SaboteurTile[][] boardTiles =boardState.getHiddenBoard(); //board 14x14  
		
		SaboteurMove goodmove = null;
		//Reminder: originPos = 5. hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
		int posGoldY = 5;
		boolean isThereMalus = false;
		boolean isThereDestroy = false;
		
		//Checking if any of the hidden cards have been revealed to us AND were the gold nugget
		if(boardTiles[12][3].getIdx().equals("nugget")) {goldFound=true; posGoldY = 3; } 
		else if(boardTiles[12][5].getIdx().equals("nugget")) { goldFound=true; posGoldY = 5; }  
		else if( boardTiles[12][7].getIdx().equals("nugget")) { goldFound=true; posGoldY=7; } //should never get here
		else if(!(boardTiles[12][3].getIdx().equals("8")) && !(boardTiles[12][5].getIdx().equals("8"))) { goldFound=true; posGoldY=7; }
		
		//Search if there is a Malus or Destroy in our hand
		for(SaboteurCard card : myCurrentHand) {
			if(card instanceof SaboteurMalus)  { isThereMalus=true; }
			if(card instanceof SaboteurDestroy)  { isThereDestroy=true; }
		}


		//If a Malus is played against us, then we play best we can
		if(boardState.getNbMalus(boardState.getTurnPlayer()) > 0) {								
			SaboteurMove repost = MyTools.counterMalus(myCurrentHand, boardTiles, playerid);
			
			if(repost != null) { goodmove = repost; }
			else { goodmove = MyTools.chooseDrop(myCurrentHand, playerid); }
			
			if(goodmove!=null) {
				MyTools.updateCardNumberAvailable();
				return goodmove;
			}
			else {
				//Room for improvement: less strict condition for dropping a card
				return new SaboteurMove(new SaboteurDrop(),0,0,playerid);
			}
		}
		
		//END OF GATHERING INFO

		//If position of gold is still unknown, try to find it!
		if(!goldFound) {
			
			SaboteurMove mapMove = MyTools.searchGold(moves,boardTiles);					
			//First prioritize playing Map, if you have one
			if(mapMove != null) {
				goodmove = mapMove;
			}
			else {																			
				//Second prioritize Malus, if you have one
				if(isThereMalus) {												
					MyTools.updateCardNumberAvailable();	
					return new SaboteurMove(new SaboteurMalus(), 0, 0, playerid);
				}
				
				//If no Malus card, simply make your way down to the hidden cards, to get closer to the nugget
				//Here, we guess the nugget is at (5,12)
				SaboteurMove moveTowardsNug = MyTools.goToNugget(moves, myCurrentHand, posGoldY,boardState); 
				if(moveTowardsNug != null) {
					goodmove = moveTowardsNug;
				}
				//No good move towards gold nugget was found...
				else {																			
					if(goodmove == null) goodmove = MyTools.chooseDrop(myCurrentHand,playerid);
					if(isThereDestroy) {
		    			SaboteurMove canDestroy = MyTools.destroyBlockingTileCloseToGoal(boardTiles,playerid);
		    			if(canDestroy != null) return canDestroy;		    			
					}
				}
			}
		}
		//You know where the gold is! Congrats!
		else {
			
			//Play Malus if you have it!
			if(isThereMalus) {												
				MyTools.updateCardNumberAvailable();
				return new SaboteurMove(new SaboteurMalus(), 0, 0, playerid);
			}
						
			SaboteurMove path = MyTools.goToNugget(moves,myCurrentHand, posGoldY,boardState);
			if(path != null) {
				goodmove = path;
			}
			else { 																			
				if(goodmove == null) goodmove = MyTools.chooseDrop(myCurrentHand,playerid);
				if(isThereDestroy) {												
					SaboteurMove canDestroy = MyTools.destroyBlockingTileCloseToGoal(boardTiles,playerid);
					if(canDestroy != null) return canDestroy;
				}
			}
		}

		//No smart moves were found... Bad luck :(
		if(goodmove == null) {
			MyTools.updateCardNumberAvailable();
			Move myMove = boardState.getRandomMove();
			return myMove;
		}
		
		MyTools.updateCardNumberAvailable();
		return goodmove;
	}
}