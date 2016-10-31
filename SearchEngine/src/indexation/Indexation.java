package indexation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

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
import indexation.Constantes;


/**
 * TP 2
 * @author Abdelhadi TEMMAR
 *
 */
public class Indexation {
	

	public String out_index_files;
	public String out_index_words;
	
	public File index_file;
	public Normalizer normalizer;
	
	public Indexation(Normalizer normalizer, String out_index_files, String out_index_words, File index_file)
	{
		this.normalizer = normalizer;
		this.out_index_files = out_index_files;
		this.out_index_words = out_index_words;
		this.index_file = index_file;
	}
	
	private void listFiles(File dir, ArrayList<File> files)
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
	private ArrayList<File> getCorpusSubIndex()
	{
		
		ArrayList<File> corpus = new ArrayList<File>();
		ArrayList<File> subindexFiles = new ArrayList<File>();
		listFiles(new File(Constantes.REP_SUBINDEX), subindexFiles);
		
		double size_corpus = 0.;
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
						File file = new File(Constantes.REP_TEXT + "/" + yyyy + "/" + mm + "/" + dd + "/" + date + "_" + id +".txt");
						if(file.exists())
						{
							corpus.add(file);
							size_corpus += file.length()/1024.;
						}
						
					}
				}
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
		

		

		System.out.println("size corpus : " + size_corpus);
		
		return corpus;
		
	}
	
	/***
	 * Pour gagner de la place en m�moire on va donner un num�ro � chaque mots et chaque fichier
	 */
	public void make_indexes_words_file()
	{
		
		File index_word_file = new File(out_index_words);
		File index_name_file = new File(out_index_files);
		
		
		ArrayList<File> files = getCorpusSubIndex();
		

		try {
			
			//Création du fichier index pour les fichiers
			OutputStreamWriter os;
			
			if (index_name_file.exists())
				   os = new OutputStreamWriter(new FileOutputStream(index_word_file), "UTF-8");//if file exists we remove everything.
			else
			{
				new File(index_name_file.getParentFile().getPath()).mkdirs();
				index_name_file.createNewFile();
				os = new OutputStreamWriter(new FileOutputStream(index_name_file), "UTF-8");
			}
				
			PrintWriter out_index_files = new PrintWriter (os, true);
			
			//Les fichiers ayant le moins de mots recevront un index plus grand
			files.sort(Comparator.comparingDouble(File::length).reversed());;
	
			for(int i = 0; i < files.size(); i++)
				out_index_files.println(i + "\t" + files.get(i).getPath());
			out_index_files.close();
			
			
			//Création du fichier qui va indexé tout les mots
			
			if (index_word_file.exists())
				   os = new OutputStreamWriter(new FileOutputStream(index_word_file), "UTF8");//if file exists we remove everything.
			else
			{
				new File(index_word_file.getParentFile().getPath()).mkdirs();
				index_word_file.createNewFile();
				os = new OutputStreamWriter(new FileOutputStream(index_word_file), "UTF8");
			}
			PrintWriter out_index_words = new PrintWriter (os, true);
			
			
			/*HashSet<String> setWords = new HashSet<String>();
			// On parcourt tout les fichiers du r�pertoire
			for(File file : files)
				setWords.addAll(normalizer.normalize(file));*/
			
			//On calcule les dfs dans le but dassigner un petit index aux mot les plus fréquents
			HashMap<String, Integer> dfs = new HashMap<String, Integer>();	
			for (File file : files) {
				HashSet<String> setWords = new HashSet(normalizer.normalize(file));
				for (String word : setWords) {
					Integer n = dfs.get(word);
					if( n == null)
						dfs.put(word, 1);
					else
						dfs.put(word, n+1);
					
				}
			}
			
			
			//on index tout les mots
			Integer index = 0;
			for(Entry<String, Integer> word : dfs.entrySet().stream().sorted(Map.Entry.<String, Integer>comparingByValue().reversed()).collect(Collectors.toList()))
				out_index_words.println(index++ + "\t" +  word.getKey());
			
			out_index_words.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @return hashmap contenant le nom de fichier correspondant à chaque identifiant
	 */
	public HashMap<Integer, String> getFilesById()
	{
		File index_name_file = new File(out_index_files);
		
		HashMap<Integer, String> ids_files = new HashMap<Integer, String>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_name_file), "UTF-8"));
			
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
	
	/**
	 * 
	 * @return hashmap contenant le mot correspondant � chaque identifiant
	 */
	public HashMap<Integer, String> getWordById()
	{
		File index_name_word = new File(out_index_words);
		
		HashMap<Integer, String> ids_words = new HashMap<Integer, String>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_name_word), "UTF-8"));
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] line_split = line.split("\t");
				ids_words.put(Integer.parseInt(line_split[0]), line_split[1]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ids_words;
	}
	
	/***
	 * 
	 * @return hashmap contenant les identifiants de chaque mots
	 */
	public HashMap<String, Integer> getIdWords()
	{
		File index_name_word = new File(out_index_words);
		
		HashMap<String, Integer> ids_words = new HashMap<String, Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_name_word), "UTF-8"));
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] line_split = line.split("\t");
				ids_words.put(line_split[1], Integer.parseInt(line_split[0]));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ids_words;
	}
	
	/**
	 * 
	 * @return hashmap contenant les identifiants pour chaque fichiers
	 */
	private HashMap<String, Integer> getIdFiles()
	{
		File index_name_file = new File(out_index_files);
		
		HashMap<String, Integer> ids_files = new HashMap<String, Integer>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_name_file), "UTF-8"));
			
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
	 * Cette fonction va indexé tout le corpus avec le normalizer normalizer
	 * */
	public void index_corpus() throws IOException {
		
		
		//Création du fichier index
		OutputStreamWriter os;
		if (index_file.exists())
		   os = new FileWriter(index_file,false);//if file exists we remove everything.
		else
		{
			new File(index_file.getParentFile().getPath()).mkdirs();
			index_file.createNewFile();
			os = new OutputStreamWriter(new FileOutputStream(index_file), "UTF-8");
		}
		
		PrintWriter out = new PrintWriter (os);
		
		//récupère pour chaque documments le nombre d'occurence de chaque mots pour chaque fichiers
		HashMap<Integer, HashMap<Integer, Integer>> tfs = new HashMap<Integer, HashMap<Integer, Integer>>();
		//TreeMap<Integer, HashMap<Integer, Float>> tfIdfs = new TreeMap<Integer, HashMap<Integer, Float>>();
		
		//On récupère les identifiants pour les fichiers
		HashMap<String, Integer> ids_files = getIdFiles();
		HashMap<String, Integer> ids_words = getIdWords();
		
		ArrayList<File> files = getCorpusSubIndex();
		int documentNumber = files.size();
		
		// On parcourt tout les fichiers du répertoire pour calculer les tfs
		for(File file : files) {
			ArrayList<String> words = normalizer.normalize(file);
			HashSet<String> words_set = new HashSet<String>(words);
			Integer id_file = ids_files.get(file.getPath());
			for(String word: words_set)
			{
				//on recupere la liste des fichiers avec les poids associees
				Integer id_word = ids_words.get(word);
				if(id_word == null)
					continue;
				HashMap<Integer, Integer> value = tfs.get(id_word);
				
				if(value == null)//Le mot n'est pas present dans la hashmap
					value = new HashMap<Integer, Integer>();
				
				value.put(id_file, Collections.frequency(words, word));
				tfs.put(id_word, value);
					
			}
		}
		
		//Caluls des dfs
		HashMap<Integer, Integer> dfs = new HashMap<Integer, Integer>();
		for(Integer word: tfs.keySet())
			dfs.put(word, tfs.get(word).size());
		
		// calcul du tf.idf
		for (Integer word : tfs.keySet())
		{
			HashMap<Integer, Integer> docs = tfs.get(word);
			HashMap<Integer, Float> tfidf = new HashMap<Integer, Float>();
			for(Integer doc : docs.keySet())
			{
				Integer tf = docs.get(doc);
				Double w = (double)tf * Math.log((double)(documentNumber) / dfs.get(word));
				BigDecimal bd = new BigDecimal(w);
				bd = bd.setScale(3, RoundingMode.HALF_UP);
				tfidf.put(doc, bd.floatValue());
			}
			//tfIdfs.put(word, tfidf);
			
			out.println(word + "\t" + tfidf.keySet().toString().replaceAll("[\\[\\] ]", "") + "\t" + tfidf.values().toString().replaceAll("[\\[\\] ]", ""));
		}
		out.close();
		
		//return tfIdfs;
	}
	
	/**
	 * Cette fonction va index� tout le corpus avec le normalizer normalizer
	 * */
	/*public void index_corpus()
	{
		try {
			
			//Cr�ation du fichier index
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
			
			//TODO : peut �tre utiliser une HashMap pour �tre plus rapide au niveau de l'insertion
			TreeMap<Integer, HashMap<Integer, Float>> tfIdfsAllFiles = getTfIdf();
			
			
			
			//On parcourt tout les mots et liste tout les documents auquels il appara�t et le poid associ�e.
			for(Integer word: tfIdfsAllFiles.keySet())
			{
				// On r�cup�re la liste de documents pour le mot
				HashMap<Integer, Float> tfIdfsForWord = tfIdfsAllFiles.get(word);
				Set<Integer> listDocs = tfIdfsForWord.keySet();
				
				out.println(word + "\t" + listDocs.size() + "\t" + listDocs.toString().replaceAll("[\\[\\] ]", "") + "\t" + tfIdfsForWord.values().toString().replaceAll("[\\[\\] ]", ""));
			}
			
			
			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
	
	
}
