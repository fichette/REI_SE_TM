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
	
	/**
	/**
	 * Le fichier contenant les mots vides
	 */
	private static String STOPWORDS_FILENAME = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine\\frenchST.txt";
	
	/**Moteur de recherche : **/
	
	/**
	 * Main, permet de faire une recherche
	 */
	public static void main(String[] args) {
		try {
			Normalizer stemmerAllWords = new FrenchStemmer();
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
			Normalizer tokenizerAllWords = new FrenchTokenizer();
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));
			Normalizer[] normalizers = {stemmerAllWords, stemmerNoStopWords, 
					tokenizerAllWords, tokenizerNoStopWords};
			
			//Ce programme utilise le modèle tokenizer
			Normalizer normalizer = tokenizerNoStopWords;

			String index_dir = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine";
			String index_file = index_dir + "\\index_tokenizer.txt";
			
			System.out.println("Enter your request ");
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			String req = reader.nextLine();
			
			SearchEngine se = new SearchEngine(new File(index_file), normalizer);
			List<String> docs = se.searchDocuments(req);
			
			//On les transforme en file
			List<File> files_docs = docs.stream().map(item -> new File(item)).collect(Collectors.toList());
			
			//On affiche les résultats
			files_docs.stream().forEach(item -> System.out.println(item.getName()));
			
			String stats_file = index_dir + "//stats//" + String.join("_", req.split(" ")) + "_tokenizer_stats.txt";
			//On génere le fichier statistiques pour évaluer le modèle
			se.computeStaticalResult(req, files_docs, new File(stats_file));
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
