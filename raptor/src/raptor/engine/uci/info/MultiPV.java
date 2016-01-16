package raptor.engine.uci.info;

import raptor.engine.uci.UCIInfo;

public class MultiPV extends UCIInfo {
	protected int id;

	public MultiPV(String id) {
		super();
		this.id = Integer.parseInt(id);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
