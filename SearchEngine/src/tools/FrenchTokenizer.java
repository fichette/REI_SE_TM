package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import tools.FrenchTokenizerAutomaton.Signal;

public class FrenchTokenizer implements Normalizer {
	
	private FrenchTokenizerAutomaton transducer;
	private HashSet<String> stopWords;
	
	public FrenchTokenizer() {
		this.transducer = new FrenchTokenizerAutomaton();
		this.stopWords = new HashSet<String>();
	}
	
	public FrenchTokenizer(File stopWordFile) throws IOException {
		this.transducer = new FrenchTokenizerAutomaton();
		this.stopWords = new HashSet<String>();
		//lecture du fichier texte	
		InputStream ips=new FileInputStream(stopWordFile); 
		InputStreamReader ipsr=new InputStreamReader(ips);
		BufferedReader br=new BufferedReader(ipsr);
		String line;
		while ((line=br.readLine())!=null){
			this.stopWords.add(line);
		}
		br.close(); 
	}
	
	/**
	 * @return the stopWords
	 */
	public HashSet<String> getStopWords() {
		return stopWords;
	}

	/**
	 * @param stopWords the stopWords to set
	 */
	public void setStopWords(HashSet<String> stopWords) {
		this.stopWords = stopWords;
	}	

	@Override
	public ArrayList<String> normalize(File file) throws IOException {
		String text = "";
		//lecture du fichier texte	
		InputStream ips=new FileInputStream(file); 
		InputStreamReader ipsr=new InputStreamReader(ips);
		BufferedReader br=new BufferedReader(ipsr);
		String line;
		while ((line=br.readLine())!=null){
			text += line + " ";
		}
		br.close(); 
		ArrayList<String> tmpResult = this.tokenize(text.toLowerCase());
		ArrayList<String> result = new ArrayList<String>();
		if (!this.stopWords.isEmpty()) {
			for (String word : tmpResult) {
				if (!this.stopWords.contains(word)) {
					result.add(word);
				}
			}
		} else {
			result = tmpResult;
		}
		return result;
	}
	
	/**
	 * This method drives the automaton execution over the stream of chars.
	 */
	public ArrayList<String> tokenize(String text) {
		char[] textContent = text.toCharArray();
		ArrayList<String> tokens = new ArrayList<String>();
		// Initialize the execution
		int begin = -1;
		transducer.reset();
		String word;
		// Run over the chars
		for(int i=0 ; i<textContent.length ; i++) {
			Signal s = transducer.feedChar( textContent[i] );
			switch(s) {
			case start_word:
				begin = i;
				break;
			case end_word:
				word = text.substring(begin, i);
				this.addToken(tokens, word);
				begin = -1;
				break;
			case end_word_prev:
				word = text.substring(begin, i-1);
				this.addToken(tokens, word);
				break;
			case switch_word:
				word = text.substring(begin, i);
				this.addToken(tokens, word);
				begin = i;
				break;
			case switch_word_prev:
				word = text.substring(begin, i-1);
				this.addToken(tokens, word);
				begin = i;
				break;
			case cancel_word:
				begin = -1;
				break;
			default:
				break;
			}
		}
		// Add the last one
		if (begin != -1) {
			word = text.substring(begin, text.length());
			this.addToken(tokens, word);
		}
		
		return tokens;
	}
	
	private ArrayList<String> addToken(ArrayList<String> list, String token) {
		list.add(token);			
		return list;
	}
	

	@Override
	public ArrayList<String> normalize(String text) {
		return this.tokenize(text);
	}
	
}
