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

public class SearchEngine {
	
	private File index_file;
	
	public Normalizer normalizer;
	
	public SearchEngine(File index_file, Normalizer normalizer)
	{
		this.index_file = index_file;
		this.normalizer = normalizer;
	}
	
	/***
	 * Retourne la similarité cosinus entre une requete et un fichier
	 * @param request
	 * @param file_name
	 * @param best_files : contient pour chaque mot la liste des fichiers avec le poid associés
	 * @param weight_by_file : contient pour chaque fichier la liste des mots avec le poid associes
	 * @param weight_request : contient les poids des mots dans la requêtes
	 * @return
	 */
	private double getSimilarityRequest(ArrayList<String> request, String file_name, HashMap<String, HashMap<String, Double>> best_files, HashMap<String, 
			HashMap<String, Double>> weight_by_file, HashMap<String, Double> weight_request)
	{		
		//On calcule la mesure de similaritee avec la méthode cosinus
		double simCos = 0.0;

		//Double words_squareq = (double) request.size(); //va contenir la somme des poids au carré pour la requete, on concidére ici que chaque mot de la requete a un poid de 1
		double words_squareq = 0.0; //va contenir la somme des poids au carré pour la requete
		double words_squaref = 0.0; //va contenir la somme des poids au carré pour les docs
		
		HashMap<String, Double> weights = weight_by_file.get(file_name);
		// On calcule la somme des poids au carrée pour le document
		for(double w : weights.values())
			words_squaref += Math.pow(w, 2);
		
		for(double w : weight_request.values())
			words_squareq += Math.pow(w, 2);
		
		try
		{
			for(String word : request)
			{
				HashMap<String, Double> tfidf = best_files.get(word);
				if(tfidf == null)//le mot n'est pas présent dans le corpus
					continue;
				Double val_word_f = tfidf.get(file_name);
				if(val_word_f != null)
					simCos += weight_request.get(word) * val_word_f;
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
	 * @param best_files : contient pour chaque mot la liste des fichiers avec le poid associés
	 * @param weight_by_file : contient pour chaque fichier la liste des mots avec le poid associes
	 * @param weight_request : contient les poids des mots dans la requêtes
	 */
	private List<Map.Entry<File, Double>> getSimilarDocumentsForRequest(ArrayList<String> request, HashMap<String, HashMap<String, Double>> best_files, HashMap<String, HashMap<String, Double>> weight_by_file, HashMap<String, Double> weight_request)
	{
		HashMap<File, Double> simDocs = new HashMap<File, Double>(); //va stoquer la liste des similarité
		
		//On réupère le nom de fichier correspondant à chaque id
		Indexation indexation = new Indexation(null, Constantes.OUT_INDEX_FILES, null);
		HashMap<String, String> ids_files = indexation.getFilesById();
		
		for(String file_name : weight_by_file.keySet())
			simDocs.put(new File(ids_files.get(file_name)), getSimilarityRequest(request, file_name, best_files, weight_by_file, weight_request));
		

		List<Map.Entry<File, Double>> docs = simDocs.entrySet().stream()
        .sorted(Map.Entry.<File, Double>comparingByValue().reversed())
        .limit(100)
        .collect(Collectors.toList());
		
		return docs;
	}
	
	

	/****
	 * request : On représente la requette comme un vecteur
	 * dir : continent les fichiers poids pour chaque documents 
	 * */
	public List<Map.Entry<File, Double>> searchDocuments(String req)
	{
		try {
			
			//Avant de commencer quoi que ce soit, on normalise la requête
			ArrayList<String> request = this.normalizer.normalize(req);
			
			Indexation indexation = new Indexation(null, null, null);
			
			BufferedReader br_if = new BufferedReader(new FileReader(this.index_file));
			
			//va contenir pour chaque mot la liste des fichiers avec le poid associés
			HashMap<String, HashMap<String, Double>> best_files = new HashMap<String, HashMap<String, Double>>();
			//va contenir pour chaque fichier la liste des mots avec le poid associes
			HashMap<String, HashMap<String, Double>> weight_by_file = new HashMap<String, HashMap<String, Double>>();
			//va contenir le poid des mots dans la requêtes, pour cela on fait la moyenne des tfidfs pour chaque mots
			HashMap<String, Double> weight_request = new HashMap<String, Double>();
			String line;
			
			//On initialise weight_request 
			for(String word: request)
				weight_request.put(word, 0.0);
			
			//On récupere pour chaque mot de la requete les fichiers qui matchent et les tfidfs correspondant
			while ((line = br_if.readLine()) != null)
			{
				String[] line_parts = line.split("\t");
				String word = line_parts[0];
				
				if(!request.contains(word))
					continue;
				
				//Identifiants des fichiers
				String[] files_names = line_parts[1].split(",");
				//tfidfs des fichiers
				double[] tfidfs_word = Arrays.asList(line_parts[2].split(",")).stream().mapToDouble(Double::parseDouble).toArray();

				HashMap<String, Double> val_word = best_files.get(word);
				double weight_word_request = 0.0;
				for(int i = 0; i < files_names.length; i++)
				{
					if(val_word == null)
						val_word = new HashMap<String, Double>();
					
					val_word.put(files_names[i], tfidfs_word[i]);
					best_files.put(word, val_word);
					
					HashMap<String, Double> val_files = weight_by_file.get(files_names[i]);
					if(val_files == null)
						val_files = new HashMap<String, Double>();
					val_files.put(word, tfidfs_word[i]);
					weight_by_file.put(files_names[i], val_files);
					weight_word_request += tfidfs_word[i];
				}
				weight_request.put(word, weight_word_request/(float)files_names.length);
				
			}
			
			return getSimilarDocumentsForRequest(request, best_files, weight_by_file, weight_request);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/***
	 * Va permettre de donner des statistques sur les r�sultats retourn�e par le moteur de recherche, pour �valuer la fiabilit�
	 * @param req : Requetes
	 * @param docs : Liste des fichiers retournées
	 * @param stats_file : Fichier dans lequel on veut écrire les statistiques
	 * @param duration : Temps d'execution de la recherche
	 */
	public void computeStaticalResult(String req, List<Map.Entry<File, Double>> docs, File stats_file, long duration)
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
			out.println("Requete renvoyée en " + duration + "ms");
			out.println("");
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
