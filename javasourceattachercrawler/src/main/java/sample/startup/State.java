package sample.startup;

import java.util.Map;
import java.util.Set;

public class State {

	private Map<String, Link> links;
	private Set<String> pairsProcessed;

	public Map<String, Link> getLinks() {
		return links;
	}
	public void setLinks(Map<String, Link> links) {
		this.links = links;
	}
	public Set<String> getPairsProcessed() {
		return pairsProcessed;
	}
	public void setPairsProcessed(Set<String> pairsProcessed) {
		this.pairsProcessed = pairsProcessed;
	}


}
