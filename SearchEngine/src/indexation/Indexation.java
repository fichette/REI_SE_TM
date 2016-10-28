package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;


import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import java.io.File;



/**
 * TP 2
 * @author Abdelhadi TEMMAR
 *
 */
public class Indexation {
	
	
	/**
	 * Le répertoire du corpus
	 */
	// TODO CHANGER LE CHEMIN
	protected static String DIRNAME = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP2\\lemonde-utf8";
	/**
	 * Le fichier contenant les mots vides
	 */
	private static String STOPWORDS_FILENAME = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP2\\frenchST.txt";

	public static String OUT_INDEX_FILES = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP5\\index_files.txt";
	public static String OUT_INDEX_WORDS = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\TP5\\index_words.txt";
	
	public static String REP_SUBINDEX = "D:/Users/abdel/CORPUS/subindex";
	public static String REP_TEXT = "D:/Users/abdel/CORPUS/";
	
	private static void listFiles(File dir, ArrayList<File> files)
	{
		
		File[] files_dir = dir.listFiles();
		
		if(files_dir == null)
			return;
		
		for (File file : files_dir)
		{		
			if (file.isDirectory())
				listFiles(file, files);
			else
				files.add(file);
		}
	}
	
	/**
	 * Va lire les fichiers xml contenus dans subindex et va retourner la liste de fichier du corpus
	 * @return
	 */
	private static ArrayList<File> getCorpusSubIndex()
	{
		
		ArrayList<File> corpus = new ArrayList<File>();
		ArrayList<File> subindexFiles = new ArrayList<File>();
		listFiles(new File(REP_SUBINDEX), subindexFiles);
		
		for (File fXmlFile :  subindexFiles)
		{
			try
			{
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document doc = dBuilder.parse(fXmlFile);
	
				//optional, but recommended
				//read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
				doc.getDocumentElement().normalize();
	
				NodeList nList = doc.getElementsByTagName("doc");
	
				for (int temp = 0; temp < nList.getLength(); temp++) {
	
					Node nNode = nList.item(temp);
	
					if (nNode.getNodeType() == Node.ELEMENT_NODE) {
	
						Element eElement = (Element) nNode;
						
						String id_dd = eElement.getAttribute("dct");
						String yyyy = id_dd.substring(0, 4);
						String mm = id_dd.substring(4, 6);
						String dd = id_dd.substring(6, 8);
						String date = yyyy + mm + dd;
						
						String id = eElement.getAttribute("id");
						File file = new File(REP_TEXT + "/" + yyyy + "/" + mm + "/" + dd + "/" + date + "_" + id +".txt");
						if(file.exists())
							corpus.add(file);
						
					}
				}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		
		}
		
		return corpus;
		
	}
	
	/***
	 * Pour gagner de la place en mémoire on va donner un numéro à chaque mots et chaque fichier
	 */
	public static void make_indexes_words_file(Normalizer normalizer)
	{
		
		File index_word_file = new File(OUT_INDEX_WORDS);
		File index_name_file = new File(OUT_INDEX_FILES);
		
		
		ArrayList<File> files = getCorpusSubIndex();
		

		try {
			
			//Création du fichier index pour les fichiers
			FileWriter fw;
			if (index_name_file.exists())
			   fw = new FileWriter(index_name_file,false);//if file exists we remove everything.
			else
			{
				new File(index_name_file.getParentFile().getPath()).mkdirs();
				index_name_file.createNewFile();
				fw = new FileWriter(index_name_file);
			}
			
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter out_index_files = new PrintWriter (bw);
			
			for(int i = 0; i < files.size(); i++)
				out_index_files.println(i + "\t" + files.get(i).getName());
			out_index_files.close();
			
			
			//Création du fichier qui va indexé tout les mots
			if (index_word_file.exists())
				   fw = new FileWriter(index_word_file,false);//if file exists we remove everything.
			else
			{
				new File(index_word_file.getParentFile().getPath()).mkdirs();
				index_word_file.createNewFile();
				fw = new FileWriter(index_word_file);
			}
				
			bw = new BufferedWriter (fw);
			PrintWriter out_index_words = new PrintWriter (bw);
			
			
			Set<String> setWords = new HashSet<String>();
			// On parcourt tout les fichiers du répertoire
			for(File file : files)
				setWords.addAll(normalizer.normalize(file));
			
			//on index tout les mots
			Integer index = 0;
			for(String word : setWords)
				out_index_words.println(index++ + "\t" +  word);
			
			out_index_words.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @return hashmap contenant les identifiants pour chaque fichiers
	 */
	private static HashMap<String, Integer> getIdFiles()
	{
		File index_name_file = new File(OUT_INDEX_FILES);
		
		HashMap<String, Integer> ids_files = new HashMap<String, Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(index_name_file));
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] line_split = line.split("\t");
				ids_files.put(line_split[1], Integer.parseInt(line_split[0]));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ids_files;
	}
	
	
	/**
	 * exo 2.4 : Calcule le tf.idf des mots d'un fichier en fonction
	 * des df déjà calculés, du nombre de documents et de
	 * la méthode de normalisation.
	 * tf : nombre d'occurence du terme t dans le document d
	 */
	public static TreeMap<String, HashMap<Integer, Double>> getTfIdf(Normalizer normalizer) throws IOException {
		
		
		//récupère pour chaque mot le nombre d'occurence de chaque mots pour chaque fichiers
		HashMap<String, HashMap<Integer, Integer>> tfs = new HashMap<String, HashMap<Integer, Integer>>();
		TreeMap<String, HashMap<Integer, Double>> tfIdfs = new TreeMap<String, HashMap<Integer, Double>>();
		
		//On récupère les identifiants pour les fichiers
		HashMap<String, Integer> ids_files = getIdFiles();
		
		ArrayList<File> files = getCorpusSubIndex();
		int documentNumber = files.size();
		
		// On parcourt tout les fichiers du répertoire
		for(File file : files) {
			ArrayList<String> words = normalizer.normalize(file);
			for(String word: words)
			{
				//on recupere la liste des fichiers avec les poids associees
				HashMap<Integer, Integer> value = tfs.get(word);
				if(value == null)//Le mot n'est pas present dans la treemap
				{
					value = new HashMap<Integer, Integer>();
					value.put(ids_files.get(file.getName()), 1);
					tfs.put(word, value);
				}
				else
				{
					Integer nb_occur = value.get(file.getName());
					if(nb_occur == null)//le fichier n'a pas encore ete rencontree pour ce mot
						value.put(ids_files.get(file.getName()), 1);
					else
						value.put(ids_files.get(file.getName()), nb_occur + 1);
					tfs.put(word, value);
				}	
			}
		}
		
		//Caluls des dfs
		HashMap<String, Integer> dfs = new HashMap<String, Integer>();
		for(String word: tfs.keySet())
			dfs.put(word, tfs.get(word).size());
		
		// calcul du tf.idf
		for (String word : tfs.keySet())
		{
			HashMap<Integer, Integer> docs = tfs.get(word);
			HashMap<Integer, Double> tfidf = new HashMap<Integer, Double>();
			for(Integer doc : docs.keySet())
			{
				Integer tf = docs.get(doc);
				Double w = (double)tf * Math.log((double)(documentNumber) / dfs.get(word));
				BigDecimal bd = new BigDecimal(w);
				bd= bd.setScale(3,BigDecimal.ROUND_CEILING);
				w = bd.doubleValue();
				tfidf.put(doc, w);
			}
			tfIdfs.put(word, tfidf);
		}
		
		return tfIdfs;
	}
	
	/**
	 * Cette fonction va indexé tout le corpus avec le normalizer normalizer
	 * */
	public static void index_corpus( File index_file, Normalizer normalizer)
	{
		try {
			
			//Création du fichier index
			FileWriter fw;
			if (index_file.exists())
			   fw = new FileWriter(index_file,false);//if file exists we remove everything.
			else
			{
				new File(index_file.getParentFile().getPath()).mkdirs();
				index_file.createNewFile();
				fw = new FileWriter(index_file);
			}
			
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter out = new PrintWriter (bw);
			
			//TODO : peut être utiliser une HashMap pour être plus rapide au niveau de l'insertion
			TreeMap<String, HashMap<Integer, Double>> tfIdfsAllFiles = getTfIdf(normalizer);
			
			
			
			//On parcourt tout les mots et liste tout les documents auquels il apparaît et le poid associée.
			for(String word: tfIdfsAllFiles.keySet())
			{
				// On récupère la liste de documents pour le mot
				HashMap<Integer, Double> tfIdfsForWord = tfIdfsAllFiles.get(word);
				Set<Integer> listDocs = tfIdfsForWord.keySet();
				
				out.println(word + "\t" + listDocs.size() + "\t" + listDocs.toString().replaceAll("[\\[\\] ]", "") + "\t" + tfIdfsForWord.values().toString().replaceAll("[\\[\\] ]", ""));
			}
			
			
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
}
