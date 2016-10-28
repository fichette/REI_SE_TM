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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import indexation.Indexation;
import tools.Normalizer;

public class SearchEngine {
	
	private File index_file;
	
	public static String out_index_files = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine\\index_files.txt";
	public static String out_index_words = "D:\\Users\\abdel\\Google Drive\\coursParisSud\\ExtractionInformation\\workspace\\REI_SE_TM\\SearchEngine\\index_words_stemmer.txt";
	
	public Normalizer normalizer;
	
	public SearchEngine(File index_file, Normalizer normalizer)
	{
		this.index_file = index_file;
		this.normalizer = normalizer;
	}
	
	/**
	 * Retourne la similarité cosinus entre une requete te un fichier*/
	private double getSimilarityRequest(ArrayList<String> request, Integer file_name, HashMap<String, HashMap<Integer, Double>> best_files, HashMap<Integer, HashMap<String, Double>> weight_by_file)
	{		
		//On calcule la mesure de similaritee avec la méthode cosinus
		Double simCos = 0.0;

		Double words_squareq = (double) request.size(); //va contenir la somme des poids au carré pour la requete, on concidère ici que chaque mot de la requete a un poid de 1
		Double words_squaref = 0.0; //va contenir la somme des poids au carré pour le doc 2
		
		HashMap<String, Double> weights = weight_by_file.get(file_name);
		// On calcule la somme des poids au carrée pour le document
		for(Double w : weights.values())
			words_squaref += Math.pow(w, 2);
		
		try
		{
			for(String word : request)
			{
				HashMap<Integer, Double> tfidf = best_files.get(word);
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
	 * Retourne les meilleurs documents pour une requete donnée
	 * @param request
	 * @param file_name
	 * @param best_files
	 * @param weight_by_file
	 */
	private List<String> getSimilarDocumentsForRequest(ArrayList<String> request, HashMap<String, HashMap<Integer, Double>> best_files, HashMap<Integer, HashMap<String, Double>> weight_by_file)
	{
		HashMap<Integer, Double> simDocs = new HashMap<Integer, Double>(); //va stoquer la liste des similarité 
		
		//On récupère le nom de fichier correspondant à chaque id
		Indexation indexation = new Indexation(null, out_index_files, out_index_words, null);
		HashMap<Integer, String> ids_files = indexation.getFilesById();
		
		for(Integer file_name : weight_by_file.keySet())
			simDocs.put(file_name, getSimilarityRequest(request, file_name, best_files, weight_by_file));
		
		List<String> docs = simDocs.entrySet().stream()
        .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
        .limit(100)
        .map(item -> ids_files.get(item.getKey()))
        .collect(Collectors.toList());
		
		return docs;
		
		
	}
	
	

	/****
	 * request : On représente la requette comme un vecteur
	 * dir : continent les fichiers poids pour chaque documents 
	 * */
	public List<String> searchDocuments(String req)
	{
		try {
			
			//Avant de commencer quoi que ce soit, on normalise la requête
			ArrayList<String> request = normalizer.normalize(req);
			
			
			BufferedReader br_if = new BufferedReader(new FileReader(this.index_file));
			
			//va contenir pour chaque mot la liste des fichiers avec le poid associés
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
				
				//Identifiants des fichiers
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
			
			return getSimilarDocumentsForRequest(request, best_files, weight_by_file);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/***
	 * Va permettre de donner des statistques sur les résultats retournée par le moteur de recherche, pour évaluer la fiabilité
	 * @param req
	 * @param files_result
	 * @param stats_file
	 */
	public void computeStaticalResult(String req, List<File> files_result, File stats_file)
	{
		try
		{
			//Avant de commencer quoi que ce soit, on normalise la requête
			ArrayList<String> request = normalizer.normalize(req);
			
			//Création du fichier qui va contenir les stats
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
			for(File file: files_result)
			{
				out.println(file.getName() + " : ");
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
