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

	
	/**
	 * 
	 * @return hashmap contenant le nom de fichier correspondant à chaque identifiant
	 */
	private static HashMap<Integer, String> getFilesById()
	{
		File index_name_file = new File(Indexation.OUT_INDEX_FILES);
		
		HashMap<Integer, String> ids_files = new HashMap<Integer, String>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(index_name_file));
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] line_split = line.split("\t");
				ids_files.put(Integer.parseInt(line_split[0]), line_split[1]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ids_files;
	}
	
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
	
	/**
	 * Retourne la similarité cosinus entre une requete te un fichier*/
	public static double getSimilarityRequest(ArrayList<String> request, Integer file_name, HashMap<String, HashMap<Integer, Double>> best_files, HashMap<Integer, HashMap<String, Double>> weight_by_file)
	{

		
		//On calcule la mesure de similaritee avec la méthode cosinus
		Double simCos = 0.0;

		//On stocke les tfs idfs des deux fichiers
		String line;

		Double words_squaref1 = (double) request.size();//va contenir la somme des poids au carré pour le doc 1
		Double words_squaref2 = 0.0;//va contenir la somme des poids au carré pour le doc 2
		
		HashMap<String, Double> weights = weight_by_file.get(file_name);
		for(Double w : weights.values())
			words_squaref2 += Math.pow(w, 2);
		
		try
		{
			for(String word : request)
			{
				HashMap<Integer, Double> tfidf = best_files.get(word);
				if(tfidf == null)
					continue;
				Double val_word_f1 = tfidf.get(file_name);
				if(val_word_f1 != null)
					simCos += val_word_f1;
			}
			
			if(simCos != 0)//si les deux docs ont au moins un mot en commun
				simCos /= (Math.sqrt(words_squaref1) * Math.sqrt(words_squaref2));

		} catch (Exception e)
		{
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
	
	/***
	 * Retourne les meilleurs documents pour une requete donnée
	 * @param request
	 * @param file_name
	 * @param best_files
	 * @param weight_by_file
	 */
	public static void getSimilarDocumentsForRequest(ArrayList<String> request, HashMap<String, HashMap<Integer, Double>> best_files, HashMap<Integer, HashMap<String, Double>> weight_by_file)
	{
		HashMap<Integer, Double> simDocs = new HashMap<Integer, Double>(); //va stoquer la liste des similarité 
		
		//On récupère le nom de fichier correspondant à chaque id
		HashMap<Integer, String> ids_files = getFilesById();
		
		for(Integer file_name : weight_by_file.keySet())
			simDocs.put(file_name, getSimilarityRequest(request, file_name, best_files, weight_by_file));
		
		System.out.println("test");
		simDocs.entrySet().stream()
        .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
        .limit(50)
        .forEach(item->System.out.println(ids_files.get(item.getKey()) + " : " + item.getValue())); // or any other terminal method
		
		
	}
	
	

	/****
	 * request : On représente la requette comme un vecteur
	 * dir : continent les fichiers poids pour chaque documents 
	 * */
	public static void searchEngine(ArrayList<String> request, File index_file)
	{
		try {
			BufferedReader br_if = new BufferedReader(new FileReader(index_file));
			
			// va contenir pour chaque mot la liste des fichiers avec le poid associés
			HashMap<String, HashMap<Integer, Double>> best_files = new HashMap<String, HashMap<Integer, Double>>();
			//va contenir pour chaque fichier la liste des mots avec le poid associes
			HashMap<Integer, HashMap<String, Double>> weight_by_file = new HashMap<Integer, HashMap<String, Double>>();
			String line;
			//On récupere pour chaque mot de la requete les fichiers qui matchent et les tfidfs correspondant
			while ((line = br_if.readLine()) != null)
			{
				String[] line_parts = line.split("\t");
				String word = line_parts[0];
				
				if(!request.contains(word))
					continue;
				
				int[] files_names = Stream.of((line_parts[2].split(","))).mapToInt(Integer::parseInt).toArray();
				double[] tfidfs_word = Arrays.asList(line_parts[3].split(",")).stream().mapToDouble(Double::parseDouble).toArray();

				HashMap<Integer, Double> val_word = best_files.get(word);
				for(int i = 0; i < files_names.length; i++)
				{
					if(val_word == null)
						val_word = new HashMap<Integer, Double>();
					
					val_word.put(files_names[i], tfidfs_word[i]);
					best_files.put(word, val_word);
					
					HashMap<String, Double> val_files = weight_by_file.get(files_names[i]);
					if(val_files == null)
						val_files = new HashMap<String, Double>();
					val_files.put(word, tfidfs_word[i]);
					weight_by_file.put(files_names[i], val_files);
					
				}
			}
			
			getSimilarDocumentsForRequest(request, best_files, weight_by_file);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			
			String filename = "texte.95-1.txt.poids";
			File file = new File(WEIGHTS_FILES_DIRNAME + "\\" + filename);
			//Set<File> fileSet = Arrays.stream(new File(WEIGHTS_FILES_DIRNAME).listFiles()).collect(Collectors.toSet());
			//getSimilarDocuments(file, fileSet);
			
			String out_file = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP5\\index.txt";
			
			//On donne un id au fichiers et aux mots
			//Indexation.make_indexes_words_file(tokenizerNoStopWords);
			//System.out.println("Fin indexation identifiant");
			
			//Test de l'indexation
			//Indexation.index_corpus(new File(out_file), tokenizerNoStopWords);
			//System.out.println("Fin de l'indexation");
			
			Scanner reader = new Scanner(System.in);  // Reading from System.in
			System.out.println("Enter your request ");
			ArrayList<String> request = tokenizerNoStopWords.normalize(reader.nextLine()); // Scans the next token of the input as an int.
			
			searchEngine(request, new File(out_file));
			
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
