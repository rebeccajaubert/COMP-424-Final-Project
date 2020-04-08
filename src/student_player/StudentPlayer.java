package student_player;

import boardgame.Move;

import Saboteur.SaboteurPlayer;
import Saboteur.cardClasses.SaboteurCard;
import Saboteur.cardClasses.SaboteurTile;

import java.util.ArrayList;
import java.util.Arrays;

import Saboteur.SaboteurBoardState;
import Saboteur.SaboteurMove;

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
		ArrayList<SaboteurCard> myCurrentHand =  boardState.getCurrentPlayerCards();
		ArrayList<SaboteurMove> moves = boardState.getAllLegalMoves();
		int playerid = boardState.getTurnPlayer();
		SaboteurMove goodmove = null;
		//HIDDEN POS ARE  5=originPos . hiddenPos = {{originPos+7,originPos-2},{originPos+7,originPos},{originPos+7,originPos+2}};
		int posGoldY = 0;

		//check if gold found 
		SaboteurTile[][] boardTiles =boardState.getHiddenBoard(); //board 14x14 but only     	
		if(boardTiles[12][3].getIdx() == "nugget") {goldFound=true; posGoldY = 3; System.out.println("SHOULD NOT SEARCH ANYMORE GOLD");} // BUG IT DOESNT CHANGE -> it still search for middle
		else if(boardTiles[5+7][5].getIdx()== "nugget" ) { goldFound=true; posGoldY = 5;}  
		else if( boardTiles[5+7][5+2].getIdx()== "nugget") {goldFound=true; posGoldY=7; } //should never get here
		else if(boardTiles[5+7][5-2].getIdx() != "8" && boardTiles[5+7][5].getIdx()!= "8") { goldFound=true; posGoldY=7; }

		// moves.forEach(move->System.out.println(move.toPrettyString()));



		//if malus on us
		if(boardState.getNbMalus(boardState.getTurnPlayer()) > 0) {
			SaboteurMove testnull = MyTools.counterMalus(moves);
			if(testnull!=null) goodmove=testnull;
		}


		//if enemy is too close to goal state then should go here




		//1st find gold
		//I dont know how to account for map cards played by adversary
		if(!goldFound) { 
			SaboteurMove testnull = MyTools.searchGold(moves,boardTiles);
			if(testnull!=null) {
				goodmove=testnull;
			}
			else {
				goodmove = MyTools.chooseDrop(myCurrentHand, playerid); // HERE should be GoDown from gotonugget()
			}
		}
		else {
			SaboteurMove path = MyTools.goToNugget(moves, posGoldY);
			if(path != null) {
				if(!boardState.isLegal(path)) System.out.println("BUG");
				goodmove= path;
			}
			else { goodmove = MyTools.chooseDrop(myCurrentHand,playerid);

			}
		}

			// You probably will make separate functions in MyTools.
			// For example, maybe you'll need to load some pre-processed best opening
			// strategies...
			MyTools.getSomething();

			// Is random the best you can do?
			Move myMove = boardState.getRandomMove();

			// Return your move to be processed by the server.
			
		
		if(goodmove == null) {
			System.out.println("REAL BAD LUCK");
			return myMove;
		}
		return goodmove;
	}
}