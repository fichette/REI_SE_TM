Voici les descriptions des classes réalisés :

	-Constantes
		C'est dans cette fonction que vous pouvez modifier le chemin ou se trouve le corpus, le fichier pour mot vides, mais aussi le nom des différents fichiers de sorties.
	-SearchEngine
		C'est cette classe qui contient les fonctions pour retourner les documents les plus similaire a une requete
	-SearchEngineMain_stemmer
		C'est le programme principale pour utiliser le moteur de recherche en utilisant l'index avec normalisation steemer.
		Le programme tourne en boucle pour vous permettre de lancer autant de requête que vous le désirez
	-SearchEngineMain_tokenizer
		C'est le programme principale pour utiliser le moteur de recherche en utilisant l'index avec normalisation tokenizer
		Le programme tourne en boucle pour vous permettre de lancer autant de requête que vous le désirez
	-Indexation
		C'est cette classe qui contient les fonctions indexer le corpus et contient aussi les fonctions pour donner un identifiant uniques aux fichiers
	-IndexationMain_stemmer
		C'est le programme principale pour indexer un corpus en utilisant la normalisation steemer. Après avoir lancé le programme, il faut appuyer sur entrée pour que l'indaxation commence
	-IndexationMain_tokenizer
		C'est le programme principale pour indexer un corpus en utilisant la normalisation tokenizer. Après avoir lancé le programme, il faut appuyer sur entrée pour que l'indaxation commence

Dans le répertoire stats, vous retrouver les résultats retournées pour chaque requête. Avec pour chaque document, le score obtenu, et le nombre de fois qu'est présent chaque mot du fichier 