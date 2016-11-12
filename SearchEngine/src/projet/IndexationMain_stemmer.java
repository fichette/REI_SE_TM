package projet;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import indexation.Indexation;
import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;
import indexation.Constantes;

public class IndexationMain_stemmer {

	
	public static void main(String[] args) {
		try {
			Normalizer stemmerAllWords = new FrenchStemmer();
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(Constantes.STOPWORDS_FILENAME));
			Normalizer tokenizerAllWords = new FrenchTokenizer();
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(Constantes.STOPWORDS_FILENAME));
			Normalizer[] normalizers = {stemmerAllWords, stemmerNoStopWords, 
					tokenizerAllWords, tokenizerNoStopWords};
			
			Normalizer normalizer = stemmerNoStopWords;
			
			System.out.println("Appuyer sur une entrée pour démarrer l'indexation");
			Scanner sc = new Scanner(System.in);
			sc.nextLine();
			

			File index_file = new File(Constantes.INDEX_STEEMER);
			
			long start_time = System.nanoTime();
			Indexation indexation = new Indexation(normalizer, Constantes.OUT_INDEX_FILES, index_file);
			//On donne un id au fichiers et aux mots
			indexation.make_indexe_file();
			long duration = (System.nanoTime()-start_time)/1000000;
			System.out.println("Fin indexation identifiant : " + duration +" ms");
			//Test de l'indexation
			start_time = System.nanoTime();
			indexation.index_corpus();
			duration = (System.nanoTime()-start_time)/1000000;
			System.out.println("Fin de l'indexation : " + duration +" ms");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
