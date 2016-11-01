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
import tools.String_id;


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
	
	//caracteres utilisés pour donner un identifiant à chaque fichier
	public String caracteres = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
	public String out_index_files;
	
	public File index_file;
	public Normalizer normalizer;
	
	public Indexation(Normalizer normalizer, String out_index_files, File index_file)
	{
		this.normalizer = normalizer;
		this.out_index_files = out_index_files;
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
    *  va générer tout les sous chaines de caracteres de this.caracteres et de taille length
    *  Permet de donner un identfiant à chaque fichiers
    ****/
   private int generate(String str, int pos, ArrayList<String> ids, int length)
    {
        if (length == 0)//si on a générer un id pour chaque fichier
            return -1;
        else {
            for (int i = pos; i < caracteres.length(); i++) {
            	String id = str + caracteres.charAt(i);
            	ids.add(id);
            	generate(id, 0, ids, length-1);
 
            }
            
        }
        
        return 0;
    }
	
	/***
	 * Pour gagner de la place en mémoire on va donner un num�ro � chaque mots et chaque fichier
	 */
	public void make_indexe_file()
	{
		
		File index_name_file = new File(out_index_files);
		
		
		ArrayList<File> files = getCorpusSubIndex();
		

		try {
			
			//Création du fichier index pour les fichiers
			OutputStreamWriter os;
			
			if (index_name_file.exists())
				   os = new OutputStreamWriter(new FileOutputStream(index_name_file), "UTF-8");//if file exists we remove everything.
			else
			{
				new File(index_name_file.getParentFile().getPath()).mkdirs();
				index_name_file.createNewFile();
				os = new OutputStreamWriter(new FileOutputStream(index_name_file), "UTF-8");
			}
				
			PrintWriter out_index_files = new PrintWriter (os, true);
			
			//Les fichiers ayant le moins de mots recevront un index plus grand
			files.sort(Comparator.comparingDouble(File::length).reversed());
			
			ArrayList<String> ids = new ArrayList<String>();
			generate("", 0, ids, (int)Math.ceil(Math.log(files.size()) / Math.log(caracteres.length())));
			ids.sort(Comparator.comparingInt(String::length));
			
			//write_index_file(out_index_files, files, "", 0, 0);
			//out_index_files.close();
			
			//String id = "A";
			for(int i = 0; i < files.size(); i++)
				out_index_files.println(ids.get(i) + "\t" + files.get(i).getPath());
			
			out_index_files.close();
		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	/**
	 * 
	 * @return hashmap contenant le nom de fichier correspondant à chaque identifiant
	 */
	public HashMap<String, String> getFilesById()
	{
		File index_name_file = new File(out_index_files);
		
		HashMap<String, String> ids_files = new HashMap<String, String>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_name_file), "UTF-8"));
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] line_split = line.split("\t");
				ids_files.put(line_split[0], line_split[1]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ids_files;
	}
	
	
	/**
	 * 
	 * @return hashmap contenant les identifiants pour chaque fichiers
	 */
	private HashMap<String, String> getIdFiles()
	{
		File index_name_file = new File(out_index_files);
		
		HashMap<String, String> ids_files = new HashMap<String, String>();
		
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(index_name_file), "UTF-8"));
			
			String line;
			
			while((line = br.readLine()) != null)
			{
				String[] line_split = line.split("\t");
				ids_files.put(line_split[1], line_split[0]);
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
		HashMap<String, HashMap<String, Integer>> tfs = new HashMap<String, HashMap<String, Integer>>();
		//TreeMap<Integer, HashMap<Integer, Float>> tfIdfs = new TreeMap<Integer, HashMap<Integer, Float>>();
		
		//On récupère les identifiants pour les fichiers
		HashMap<String, String> ids_files = getIdFiles();
		
		//ArrayList<File> files = getCorpusSubIndex();
		int documentNumber = 0;
		
		// On parcourt tout les fichiers du répertoire pour calculer les tfs
		for(File file : getCorpusSubIndex()) {
			ArrayList<String> words = normalizer.normalize(file);
			HashSet<String> words_set = new HashSet<String>(words);
			String id_file = ids_files.get(file.getPath());
			for(String word: words_set)
			{
				if(word == null)
					continue;
				HashMap<String, Integer> value = tfs.get(word);
				
				if(value == null)//Le mot n'est pas present dans la hashmap
					value = new HashMap<String, Integer>();
				
				value.put(id_file, Collections.frequency(words, word));
				tfs.put(word, value);
					
			}
			
			documentNumber++;
		}
		
		//Caluls des dfs
		/*HashMap<String, Integer> dfs = new HashMap<String, Integer>();
		for(String word: tfs.keySet())
			dfs.put(word, tfs.get(word).size());*/
		
		// calcul du tf.idf
		//On convertit tfs en treemap pour pouvoir stocker les mots dans l'ordre alphabétique
		for (String word : (new TreeMap<String, HashMap<String, Integer>>(tfs)).keySet())
		{
			HashMap<String, Integer> docs = tfs.get(word);
			HashMap<String, Float> tfidf = new HashMap<String, Float>();
			for(String doc : docs.keySet())
			{
				Integer tf = docs.get(doc);
				Integer df = docs.size();
				Double w = (double)tf * Math.log((double)(documentNumber) / df);
				BigDecimal bd = new BigDecimal(w);
				bd = bd.setScale(2, RoundingMode.HALF_UP);
				tfidf.put(doc, bd.floatValue());
			}
			//tfIdfs.put(word, tfidf);
			out.println(word + "\t" + tfidf.keySet().toString().replaceAll("[\\[\\] ]", "") + "\t" + tfidf.values().toString().replaceAll("[\\[\\] ]", ""));
		}
		out.close();
		
		//return tfIdfs;
	}
	
	
}
