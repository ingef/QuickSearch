package de.ingef.utils.search;

@FunctionalInterface
public interface SearchScorer {
	public double score(String keywordMatch, String keyword);
}
