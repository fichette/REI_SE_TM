package searchEngine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import indexation.Indexation;
import tools.Normalizer;
import indexation.Constantes;
import tools.ValueComparator;

public class SearchEngine {
	
	private File index_file;
	private String out_index_words;
	HashMap<String, Integer> ids_words;
	
	public Normalizer normalizer;
	
	public SearchEngine(File index_file, String out_index_words, Normalizer normalizer)
	{
		this.index_file = index_file;
		this.normalizer = normalizer;
		this.out_index_words = out_index_words;
		this.ids_words = new Indexation(normalizer, null, out_index_words, null).getIdWords();
	}
	
	/**
	 * Retourne la similarit� cosinus entre une requete te un fichier*/
	private double getSimilarityRequest(ArrayList<String> request, Integer file_name, HashMap<Integer, HashMap<Integer, Double>> best_files, HashMap<Integer, HashMap<Integer, Double>> weight_by_file)
	{		
		//On calcule la mesure de similaritee avec la m�thode cosinus
		Double simCos = 0.0;

		Double words_squareq = (double) request.size(); //va contenir la somme des poids au carr� pour la requete, on concid�re ici que chaque mot de la requete a un poid de 1
		Double words_squaref = 0.0; //va contenir la somme des poids au carr� pour le doc 2
		
		HashMap<Integer, Double> weights = weight_by_file.get(file_name);
		// On calcule la somme des poids au carr�e pour le document
		for(Double w : weights.values())
			words_squaref += Math.pow(w, 2);
		
		try
		{
			for(String word : request)
			{
				long start_time = System.nanoTime();
				HashMap<Integer, Double> tfidf = best_files.get(ids_words.get(word));
				if(tfidf == null)
					continue;
				Double val_word_f = tfidf.get(file_name);
				if(val_word_f != null)
					simCos += val_word_f;
			}
			
			if(simCos != 0)//si les deux docs ont au moins un mot en commun
				simCos /= (Math.sqrt(words_squareq) * Math.sqrt(words_squaref));

		} catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return simCos;
	}
	
	/***
	 * Retourne les meilleurs documents pour une requete donn�e
	 * @param request
	 * @param file_name
	 * @param best_files
	 * @param weight_by_file
	 */
	private List<Map.Entry<File, Double>> getSimilarDocumentsForRequest(ArrayList<String> request, HashMap<Integer, HashMap<Integer, Double>> best_files, HashMap<Integer, HashMap<Integer, Double>> weight_by_file)
	{
		HashMap<File, Double> simDocs = new HashMap<File, Double>(); //va stoquer la liste des similarit� 
		
		//On r�cup�re le nom de fichier correspondant � chaque id
		Indexation indexation = new Indexation(null, Constantes.OUT_INDEX_FILES, out_index_words, null);
		HashMap<Integer, String> ids_files = indexation.getFilesById();
		
		System.out.println("debut");
		for(Integer file_name : weight_by_file.keySet())
			simDocs.put(new File(ids_files.get(file_name)), getSimilarityRequest(request, file_name, best_files, weight_by_file));
		

		List<Map.Entry<File, Double>> docs = simDocs.entrySet().stream()
        .sorted(Map.Entry.<File, Double>comparingByValue().reversed())
        .limit(100)
        .collect(Collectors.toList());
		
        //.collect(Collectors.toMap(e->new File(ids_files.get(e.getKey())), Map.Entry::getValue));
	
		/*result.putAll(simDocs);
		Stream.of(result).limit(100).toMap(Map.Entry::getKey , Map.Entry::getValue, (a,b) -> a, new TreeMap<File, Double>(comparator));*/
		
		return docs;
	}
	
	

	/****
	 * request : On repr�sente la requette comme un vecteur
	 * dir : continent les fichiers poids pour chaque documents 
	 * */
	public List<Map.Entry<File, Double>> searchDocuments(String req)
	{
		try {
			
			//Avant de commencer quoi que ce soit, on normalise la requ�te
			ArrayList<String> request = normalizer.normalize(req);
			
			Indexation indexation = new Indexation(null, null, out_index_words, null);
			HashMap<Integer, String> wordById = indexation.getWordById();
			
			BufferedReader br_if = new BufferedReader(new FileReader(this.index_file));
			
			//va contenir pour chaque mot la liste des fichiers avec le poid associ�s
			HashMap<Integer, HashMap<Integer, Double>> best_files = new HashMap<Integer, HashMap<Integer, Double>>();
			//va contenir pour chaque fichier la liste des mots avec le poid associes
			HashMap<Integer, HashMap<Integer, Double>> weight_by_file = new HashMap<Integer, HashMap<Integer, Double>>();
			String line;
			
			//On r�cupere pour chaque mot de la requete les fichiers qui matchent et les tfidfs correspondant
			while ((line = br_if.readLine()) != null)
			{
				String[] line_parts = line.split("\t");
				Integer word = Integer.parseInt(line_parts[0]);
				
				if(!request.contains(wordById.get(word)))
					continue;
				
				//Identifiants des fichiers
				int[] files_names = Stream.of((line_parts[1].split(","))).mapToInt(Integer::parseInt).toArray();
				//tfidfs des fichiers
				double[] tfidfs_word = Arrays.asList(line_parts[2].split(",")).stream().mapToDouble(Double::parseDouble).toArray();

				HashMap<Integer, Double> val_word = best_files.get(word);
				for(int i = 0; i < files_names.length; i++)
				{
					if(val_word == null)
						val_word = new HashMap<Integer, Double>();
					
					val_word.put(files_names[i], tfidfs_word[i]);
					best_files.put(word, val_word);
					
					HashMap<Integer, Double> val_files = weight_by_file.get(files_names[i]);
					if(val_files == null)
						val_files = new HashMap<Integer, Double>();
					val_files.put(word, tfidfs_word[i]);
					weight_by_file.put(files_names[i], val_files);
					
				}
			}
			
			return getSimilarDocumentsForRequest(request, best_files, weight_by_file);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/***
	 * Va permettre de donner des statistques sur les r�sultats retourn�e par le moteur de recherche, pour �valuer la fiabilit�
	 * @param req
	 * @param docs
	 * @param stats_file
	 */
	public void computeStaticalResult(String req, List<Map.Entry<File, Double>> docs, File stats_file)
	{
		try
		{
			//Avant de commencer quoi que ce soit, on normalise la requ�te
			ArrayList<String> request = normalizer.normalize(req);
			
			//Cr�ation du fichier qui va contenir les stats
			FileWriter fw;
			if (stats_file.exists())
			   fw = new FileWriter(stats_file,false);//if file exists we remove everything.
			else
			{
				new File(stats_file.getParentFile().getPath()).mkdirs();
				stats_file.createNewFile();
				fw = new FileWriter(stats_file);
			}
			
			BufferedWriter bw = new BufferedWriter (fw);
			PrintWriter out = new PrintWriter (bw);
			
			//On calcule le % des mots de la requete dans chaque documents
			for(Map.Entry<File, Double> file_tfidf: docs)
			{
				File file = file_tfidf.getKey();
				Double tfidf = file_tfidf.getValue();
				out.println(file.getName() + " : " + tfidf);
				ArrayList<String> words = normalizer.normalize(file);
				for(String word : request)
				{
					int freq = Collections.frequency(words, word);
					out.println("\t" + word  + " : " + freq +" fois " + " pourcentage : " + freq/(float)words.size() + "%");
				}
			}
			
			out.close();
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}

}
