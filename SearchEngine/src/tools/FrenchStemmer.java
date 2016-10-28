package tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;


/**
 * Classe de racinisation (stemming) des mots
 * en français.
 * Modification légère du package SnowBall
 * http://snowball.tartarus.org/download.php
 * @author xtannier
 *
 */
public class FrenchStemmer extends org.tartarus.snowball.ext.frenchStemmer implements Normalizer {

	private static short REPEAT = 1;
	
	private HashSet<String> stopWords;
	
	public FrenchStemmer() {
		this.stopWords = new HashSet<String>();
	}
	
	public FrenchStemmer(File stopWordFile) throws IOException {
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
		StringBuilder text = new StringBuilder();
		//lecture du fichier texte	
		InputStream ips=new FileInputStream(file); 
		InputStreamReader ipsr=new InputStreamReader(ips);
		BufferedReader br=new BufferedReader(ipsr);
		String line;
		while ((line=br.readLine())!=null){
			text.append(line + " ");
		}
		br.close(); 
		
		return this.normalize(text.toString());
	}
	
	@Override
	public ArrayList<String> normalize(String text) {
		ArrayList<String> words = (new FrenchTokenizer()).tokenize(text.toLowerCase());
		ArrayList<String> result = new ArrayList<String>();
		for (String word : words) {
			// on ajoute le mot dans la liste s'il n'appartient pas ï¿œ la liste des mots-clï¿œs.
			// Idï¿œalement il faudrait utiliser une structure de donnï¿œes plus efficace que la liste,
			// mais ce n'est pas le sujet.
			if (this.stopWords.isEmpty() || (!(this.stopWords.contains(word) || word.length() == 1 || word.equals("...")))) {
				this.setCurrent(word);
				for (int i = REPEAT; i != 0; i--) {
					this.stem();
				}
				result.add(this.getCurrent());
			}

		}
		return result;
	}

}
