package raptor.swt.chess;

import raptor.chess.Result;

/**
 * A pgn parse results row.
 */
public class PgnParseResultsRow {
	private long lineNumber;
	private String variant;
	private String date;
	private String event;
	private String white;
	private String whiteElo;
	private String black;
	private String blackElo;
	private String eco;
	private String opening;
	private Result result;
	private String resultDescription;

	public long getLineNumber() {
		return lineNumber;
	}

	public void setLineNumber(long lineNumber) {
		this.lineNumber = lineNumber;
	}

	public String getVariant() {
		return variant;
	}

	public void setVariant(String variant) {
		this.variant = variant;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getWhite() {
		return white;
	}

	public void setWhite(String white) {
		this.white = white;
	}

	public String getWhiteElo() {
		return whiteElo;
	}

	public void setWhiteElo(String whiteElo) {
		this.whiteElo = whiteElo;
	}

	public String getBlack() {
		return black;
	}

	public void setBlack(String black) {
		this.black = black;
	}

	public String getBlackElo() {
		return blackElo;
	}

	public void setBlackElo(String blackElo) {
		this.blackElo = blackElo;
	}

	public String getEco() {
		return eco;
	}

	public void setEco(String eco) {
		this.eco = eco;
	}

	public String getOpening() {
		return opening;
	}

	public void setOpening(String opening) {
		this.opening = opening;
	}

	public Result getResult() {
		return result;
	}

	public void setResult(Result result) {
		this.result = result;
	}

	public String getResultDescription() {
		return resultDescription;
	}

	public void setResultDescription(String resultDescription) {
		this.resultDescription = resultDescription;
	}

	@Override
	public String toString() {
		return "PgnParseResultsRow [lineNumber=" + lineNumber + ", variant=" + variant + ", date=" + date + ", event="
				+ event + ", white=" + white + ", whiteElo=" + whiteElo + ", black=" + black + ", blackElo=" + blackElo
				+ ", eco=" + eco + ", opening=" + opening + ", result=" + result + ", resultDescription="
				+ resultDescription + "]";
	}
}
