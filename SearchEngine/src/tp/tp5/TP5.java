package tp.tp5;

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
public class TP5 {
	
	/**
	 * Le repertoire du corpus
	 */
	protected static String DIRNAME = "D:/Users/abdel/CORPUS/2015/01";
	/**
	 * Le repertoire des fichiers .poids 
	 */
	protected static String WEIGHTS_FILES_DIRNAME = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP2\\NEW_OUT\\tools.FrenchStemmer_noSW";
	/**
	/**
	 * Le fichier contenant les mots vides
	 */
	private static String STOPWORDS_FILENAME = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP2\\frenchST.txt";

	
	
	/**Calcul la distance similarité entre les deux fichiers .poids**/
	public static double getSimilarity(File file1, File file2)
	{
		HashMap<String, Double> tfIdfs_file1 = new HashMap<String, Double>();
		HashMap<String, Double> tfIdfs_file2 = new HashMap<String, Double>();
		
		//On calcule la mesure de similaritee avec la méthode cosinus
		Double simCos = 0.0;
		try
		{
			BufferedReader br_f1 = new BufferedReader(new FileReader(file1));
			BufferedReader br_f2 = new BufferedReader(new FileReader(file2));
			
			//On stocke les tfs idfs des deux fichiers
			String line;

			Double words_squaref1 = 0.0;//va contenir la somme des poids au carré pour le doc 1
			Double words_squaref2 = 0.0;//va contenir la somme des poids au carré pour le doc 2
			while((line = br_f1.readLine()) != null)
			{
				String[] split1 = line.split("\t");
				Double val_word_f1 = Double.parseDouble(split1[1]);
				tfIdfs_file1.put(split1[0], val_word_f1);

				words_squaref1 += Math.pow(val_word_f1, 2);
			}
			while((line = br_f2.readLine()) != null)
			{
				String[] split1 = line.split("\t");
				Double val_word_f2 = Double.parseDouble(split1[1]);
				tfIdfs_file2.put(split1[0], val_word_f2);
				words_squaref2 += Math.pow(val_word_f2, 2);
			}
			
			br_f1.close();
			br_f2.close();
			
			
			for(String word : tfIdfs_file1.keySet())
			{
				Double val_word_f2 = tfIdfs_file2.get(word);
				
				if (val_word_f2 != null) //le mot existe dans le fichier2 
				{
					Double val_word_f1 = tfIdfs_file1.get(word);
					simCos += val_word_f1 * val_word_f2;
				}
			}
			
			if(simCos != 0)//si les deux docs ont au moins un mot en commun
				simCos /= (Math.sqrt(words_squaref1) * Math.sqrt(words_squaref2));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return simCos;
	}
	
	
	/**affiche les scores de similaritee entre un document (dont le  fichier .poids est passe en premier
	argument) et l'ensemble des documents du corpus. Le second argument represente le repertoire
	contenant l'ensemble des fichiers .poids du corpus.**/
	public static void getSimilarDocuments(File index_file, Set<File> fileList)
	{
		HashMap<String, Double> simDocs = new HashMap<String, Double>(); //va stoquer la liste des similarité 
		for(File file : fileList)
			simDocs.put(file.getName(), getSimilarity(index_file, file));
		
		simDocs.entrySet().stream()
        .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
        .forEach(System.out::println); // or any other terminal method
	}
	
	
	
	/**Moteur de recherche : **/
	
	/**
	 * Main, appels de toutes les méthodes des exercices du TP4 
	 */
	public static void main(String[] args) {
		try {
			Normalizer stemmerAllWords = new FrenchStemmer();
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(STOPWORDS_FILENAME));
			Normalizer tokenizerAllWords = new FrenchTokenizer();
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(STOPWORDS_FILENAME));
			Normalizer[] normalizers = {stemmerAllWords, stemmerNoStopWords, 
					tokenizerAllWords, tokenizerNoStopWords};
			
			Normalizer normalizer = stemmerNoStopWords;
			String filename = "texte.95-1.txt.poids";
			File file = new File(WEIGHTS_FILES_DIRNAME + "\\" + filename);
			Set<File> fileSet = Arrays.stream(new File(WEIGHTS_FILES_DIRNAME).listFiles()).collect(Collectors.toSet());
			getSimilarDocuments(file, fileSet);
			
			String out_file = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP5\\index.txt";

			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
