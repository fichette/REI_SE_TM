package projet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import indexation.Indexation;
import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;

public class IndexationMain_stemmer {
	
	/**
	 * Le fichier contenant les mots vides
	 */
	private static String STOPWORDS_FILENAME = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine\\frenchST.txt";
	
	public static String out_index_files = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine\\index_files.txt";
	public static String out_index_words = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine\\index_words_stemmer.txt";
	
	public static void main(String[] args) {
		try {
			Normalizer stemmerAllWords = new FrenchStemmer();
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
			Normalizer tokenizerAllWords = new FrenchTokenizer();
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));
			Normalizer[] normalizers = {stemmerAllWords, stemmerNoStopWords, 
					tokenizerAllWords, tokenizerNoStopWords};
			
			Normalizer normalizer = stemmerNoStopWords;

			String index_dir = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine";
			File index_file = new File(index_dir + "\\index_stemmer.txt");
			
			Indexation indexation = new Indexation(normalizer, out_index_files, out_index_words, index_file);
			//On donne un id au fichiers et aux mots
			indexation.make_indexes_words_file();
			System.out.println("Fin indexation identifiant");
			
			//Test de l'indexation
			indexation.index_corpus();
			System.out.println("Fin de l'indexation");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
