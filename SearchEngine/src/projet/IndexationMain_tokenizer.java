package projet;

import java.io.File;
import java.io.IOException;

import indexation.Indexation;
import tools.FrenchStemmer;
import tools.FrenchTokenizer;
import tools.Normalizer;
import indexation.Constantes;

public class IndexationMain_tokenizer {
	
	public static void main(String[] args) {
		try {
			Normalizer stemmerAllWords = new FrenchStemmer();
			Normalizer stemmerNoStopWords = new FrenchStemmer(new File(Constantes.STOPWORDS_FILENAME));
			Normalizer tokenizerAllWords = new FrenchTokenizer();
			Normalizer tokenizerNoStopWords = new FrenchTokenizer(new File(Constantes.STOPWORDS_FILENAME));
			Normalizer[] normalizers = {stemmerAllWords, stemmerNoStopWords, 
					tokenizerAllWords, tokenizerNoStopWords};
			
			Normalizer normalizer = tokenizerNoStopWords;
			
			File index_file = new File(Constantes.INDEX_TOKENIZER);
			
			Indexation indexation = new Indexation(normalizer, Constantes.OUT_INDEX_FILES, Constantes.OUT_INDEX_WORDS_TOKENIZER, index_file);
			//On donne un id au fichiers et aux mots
			indexation.make_indexes_words_file();
			System.out.println("Fin indexation identifiant");
			
			//Test de l'indexation
			indexation.index_corpus();
			System.out.println("Fin de l'indexation");
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
