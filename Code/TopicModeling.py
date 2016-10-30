# -*- coding: utf-8 -*-
import os
from gensim import corpora, models
import string
import nltk
import numpy as np
import pickle


from nltk.stem.snowball import FrenchStemmer
stemmer = FrenchStemmer()

ponctuation = set(string.punctuation)

def remove_infrequent_words(content_file, min_freq):
    content_file = np.array(content_file.split())
    freq_dist = nltk.FreqDist(content_file)
    for elt in freq_dist:
        if freq_dist[elt] < min_freq:
            content_file = np.delete(content_file, np.where(content_file==elt)[0])
    return ' '.join(content_file)


def preprocess_data_file(content_file):
    content_file = content_file.lower()
    content_file = content_file.replace('’', " ")
    content_file = content_file.replace('«', " ")
    content_file = content_file.replace('»', " ")
    content_file = content_file.replace('—', " ")
    content_file = content_file.replace('\'', " ")
    #content_file = content_file.replace('©', " ")
    content_file = ''.join(char for char in content_file if char not in ponctuation) #remove ponctuation
    content_file = remove_infrequent_words(content_file, 2)
    return content_file


documents=[]
chemin_corpus = "Corpus/"
list_dir = os.listdir(chemin_corpus)
for dir_annee in list_dir:
    sub_dirs_annee = os.listdir(chemin_corpus+dir_annee)
    for sub_dir in sub_dirs_annee:
        sub_sub_dirs = os.listdir(chemin_corpus+dir_annee+"/"+sub_dir)
        for sub_sub_dir in sub_sub_dirs:
            files = os.listdir(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir)
            for f in files[0:2]:
                open_file = open(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir+"/"+f, "r")
                content_file = open_file.read()
                content_file = preprocess_data_file(content_file)
                documents.append(content_file)
                open_file.close()

file_stopwords = open("frenchST.txt", "r")
stopwords = file_stopwords.read()
stopwords = stopwords.split("\n")
file_stopwords.close()
texts = [[word for word in document.lower().split() if word not in stopwords] for document in documents]

dictionary = corpora.Dictionary(texts)
corpus = [dictionary.doc2bow(text) for text in texts] #term_frequency


lda = models.ldamodel.LdaModel(corpus=corpus, id2word=dictionary, num_topics=5, update_every=1, chunksize=10000, passes=1)

#pickle.dump(lda, open(os.getcwd()+"/lda_output", "wb"))
#lda = pickle.load(open(os.getcwd()+"/lda_output", "rb"))

print lda.get_document_topics(corpus[0], minimum_probability=0.6) #----> 'doc_topic_dists'
print lda.get_topic_terms(3) #----> 'topic_term_dists'
print len(corpus[0]) #----> 'doc_lengths'
print dictionary.token2id #----> Vocabulaire de tout le corpus
print corpus #---->term frequency for each doc
