package org.freejava.tools.handlers;

import java.io.File;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class SearchFiles {

	private SearchFiles() {
	}

	public static TopDocs search(File indexDir, String queryString, int maxResult) throws Exception {
		String field = "contents";
		IndexSearcher searcher = new IndexSearcher(FSDirectory.open(indexDir));
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_31);
		QueryParser parser = new QueryParser(Version.LUCENE_31, field, analyzer);
		Query query = parser.parse(queryString);
		TopDocs results = searcher.search(query, maxResult);
		searcher.close();
		return results;
	}
}
