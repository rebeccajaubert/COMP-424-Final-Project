package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurDestroy;
import Saboteur.cardClasses.SaboteurDrop;
import Saboteur.cardClasses.SaboteurMalus;
import Saboteur.cardClasses.SaboteurTile;

import java.util.ArrayList;
import java.util.Arrays;


import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

/** A player file submitted by a student. */
public class StudentPlayer extends SaboteurPlayer {
	private boolean goldFound = false;
	private int countMalus= 0;
	
	
	
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
		ArrayList<SaboteurCard> myCurrentHand =  boardState.getCurrentPlayerCards();
		ArrayList<SaboteurMove> moves = boardState.getAllLegalMoves();
		int playerid = boardState.getTurnPlayer();
		SaboteurMove goodmove = null;
		//HIDDEN POS ARE  5=originPos . hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
		int posGoldY = 5;
		boolean isThereMalus = false;
		boolean isThereDestroy = false;

		//check if gold found 
		SaboteurTile[][] boardTiles =boardState.getHiddenBoard(); //board 14x14   
		
		if(boardTiles[12][3].getIdx().equals("nugget")) {goldFound=true; posGoldY = 3; } 
		else if(boardTiles[12][5].getIdx().equals("nugget")) { goldFound=true; posGoldY = 5;}  
		else if( boardTiles[12][7].getIdx().equals("nugget")) {goldFound=true; posGoldY=7; } //should never get here
		else if(!(boardTiles[12][3].getIdx().equals("8")) && !(boardTiles[12][5].getIdx().equals("8"))) { goldFound=true; posGoldY=7; }
		

		// search if malus present
		for(SaboteurCard card : myCurrentHand) {
			if(card instanceof SaboteurMalus) { isThereMalus=true;}
			if(card instanceof SaboteurDestroy) { isThereDestroy=true;}
		}



		//if malus on us
		if(boardState.getNbMalus(boardState.getTurnPlayer()) > 0) {								
			SaboteurMove repost = MyTools.counterMalus(myCurrentHand, boardTiles, playerid);
			if(repost != null) {goodmove = repost;}
			else { goodmove = MyTools.chooseDrop(myCurrentHand, playerid); }
			if(goodmove!=null) {
				MyTools.updateCardNumberAvailable();
				return goodmove;
			}
			else { //REAL BAD LUCK
				System.out.println("FUCK MALUS");											
				//TODO : a 2nd less strict choose drop
				return new SaboteurMove(new SaboteurDrop(),0,0,playerid);
			}
		}


		//if enemy is too close to goal state then should go here

		
		// /!\ need to handle if destroy card is played in middle of our path !! NB: STRATEGY TO MAKE THE OTHER ROBOT FAIL
		//IDEA : ONLY PLACE TILE IF CONNECTED TO ENTRANCE --> MAYBE THIS IS MANDATORY ANYWAY ???
		// board.isLegal if destroy was played
		//SaboteurTile[][] afterOpponentBoard = boardState.getHiddenBoard();
		//SAVE BOARD
		//currentBoard = boardState.getHiddenBoard(); //WHERE ?


		
		//TODO if in row x =12 ==> go to adjacent hidden objective


		//1st find gold
		//I dont know how to account for map cards played by adversary
		if(!goldFound) { 
			SaboteurMove mapMove = MyTools.searchGold(moves,boardTiles);					
			if(mapMove!=null) {
				goodmove=mapMove;
			}
			else {																			
				//play malus if in hand DOES METHOD CONTAIN WORKS?
				if(isThereMalus) {												
					MyTools.updateCardNumberAvailable();	
					return new SaboteurMove(new SaboteurMalus(), 0, 0, playerid);
				}
				//goodmove = MyTools.chooseDrop(myCurrentHand, playerid); // HERE should be GoDown from gotonugget()
				
				
				SaboteurMove path = MyTools.goToNugget(moves,myCurrentHand, posGoldY,boardState); //here gold default to 5		
				if(path != null) {
					goodmove= path;
				}
				else {	
																								
					if(goodmove == null) goodmove = MyTools.chooseDrop(myCurrentHand,playerid);
					if(isThereDestroy) {										System.out.println("6");
		    			SaboteurMove canDestroy = MyTools.destroyBlockingTileCloseToGoal(boardTiles,playerid);
		    			if(canDestroy != null) return canDestroy;		    			
					}
				}
			}
		}
		else {
			//play malus if in hand DOES METHOD CONTAIN WORKS?
			if(isThereMalus) {												
				MyTools.updateCardNumberAvailable();
				return new SaboteurMove(new SaboteurMalus(), 0, 0, playerid);
			}
						
																											System.out.println("7");
			SaboteurMove path = MyTools.goToNugget(moves,myCurrentHand, posGoldY,boardState);
			if(path != null) {
				goodmove= path;
			}
			else { 																			
				if(goodmove == null) goodmove = MyTools.chooseDrop(myCurrentHand,playerid);
				if(isThereDestroy) {												
					SaboteurMove canDestroy = MyTools.destroyBlockingTileCloseToGoal(boardTiles,playerid);
					if(canDestroy != null) return canDestroy;
				}
			}
		}

			// You probably will make separate functions in MyTools.
			// For example, maybe you'll need to load some pre-processed best opening
			// strategies...
			//MyTools.getSomething();

			
		
		
		if(goodmove == null) {
			System.out.println("REAL BAD LUCK");
			MyTools.updateCardNumberAvailable();
			Move myMove = boardState.getRandomMove();
			return myMove;
		}
		MyTools.updateCardNumberAvailable();
		return goodmove;
	}
}