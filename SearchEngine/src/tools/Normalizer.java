package tools;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Interface de normalisation des mots
 * @author xtannier
 *
 */
public interface Normalizer {
	/**
	 * Renvoie la liste d'unités lexicales contenus dans le fichier
	 * spécifié, en appliquant une normalisation. 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public ArrayList<String> normalize(File file) throws IOException;
	
	
	/**
	 * Renvoie la liste d'unités lexicales contenus dans le texte
	 * spécifié, en appliquant une normalisation. Equivaut à {@code normalize(text, false)}. 
	 * @param text
	 * @return
	 */
	public ArrayList<String> normalize(String text);

	/**
	 * @return the stopWords
	 */
	public HashSet<String> getStopWords();

	/**
	 * @param stopWords the stopWords to set
	 */
	public void setStopWords(HashSet<String> stopWords);

}
