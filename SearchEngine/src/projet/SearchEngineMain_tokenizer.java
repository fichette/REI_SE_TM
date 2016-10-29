package projet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sun.javafx.image.impl.ByteIndexed.ToByteBgraAnyConverter;
import com.sun.javafx.scene.traversal.WeightedClosestCorner;

import indexation.Indexation;
import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;

import searchEngine.SearchEngine;
import indexation.Constantes;

/**
 * TP 5
 * @author Abdelhadi TEMMAR
 *
 */

/**
 * Etapes pour faire le moteur de recherche
 * Etape 1 : indexation de tout les documents. 
 * 		Poids de tout les mots pour chaque documents
 * Etape 2 utiliser la méthode vectoriel pour trouvez les documents les probables de correspondant à la requete
 **/
public class SearchEngineMain_tokenizer {

	/**Moteur de recherche : **/
	
	/**
	 * Main, permet de faire une recherche
	 */
	public static void main(String[] args) {
		try {
			Normalizer stemmerAllWords = new FrenchStemmer();
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(Constantes.STOPWORDS_FILENAME));
			Normalizer tokenizerAllWords = new FrenchTokenizer();
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(Constantes.STOPWORDS_FILENAME));
			Normalizer[] normalizers = {stemmerAllWords, stemmerNoStopWords, 
					tokenizerAllWords, tokenizerNoStopWords};
			
			//Ce programme utilise le modèle tokenizer
			Normalizer normalizer = tokenizerNoStopWords;
			
			System.out.println("Enter your request ");
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			String req = reader.nextLine();
			
			SearchEngine se = new SearchEngine(new File(Constantes.INDEX_TOKENIZER), Constantes.OUT_INDEX_WORDS_TOKENIZER,normalizer);
			List<Map.Entry<File, Double>> docs = se.searchDocuments(req);
			
			//On affiche les résultats
			for(Map.Entry<File, Double> file_simcos :  docs)
				System.out.println(file_simcos.getKey().getName() + " : " + file_simcos.getValue());
			
			String stats_file = Constantes.DIR_PROJECT + "//stats//" + String.join("_", req.split(" ")) + "_tokenizer_stats.txt";
			//On génere le fichier statistiques pour évaluer le modèle
			se.computeStaticalResult(req, docs, new File(stats_file));
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
