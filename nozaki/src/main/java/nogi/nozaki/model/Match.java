package nogi.nozaki.model;

public class Match {
	private String matchId;
	private String white;
	private String black;
	private String board;
	private String turn;
	private String state;

	public Match() {
	}

	public Match(String matchId, String white, String black, String board, String turn, String state) {
		this.matchId = matchId;
		this.white = white;
		this.black = black;
		this.board = board;
		this.turn = turn;
		this.state = state;
	}

	public String getMatchId() {
		return matchId;
	}

	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}

	public String getWhite() {
		return white;
	}

	public void setWhite(String white) {
		this.white = white;
	}

	public String getBlack() {
		return black;
	}

	public void setBlack(String black) {
		this.black = black;
	}

	public String getBoard() {
		return board;
	}

	public void setBoard(String board) {
		this.board = board;
	}

	public String getTurn() {
		return turn;
	}

	public void setTurn(String turn) {
		this.turn = turn;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}
}
