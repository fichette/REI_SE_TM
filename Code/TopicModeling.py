# -*- coding: utf-8 -*-
from __future__ import unicode_literals
import os
from gensim import corpora, models
import string
import nltk
import numpy as np
from langdetect import detect
import re
import pickle
from pattern.text.fr import parse



def lemmatization(content_file):
    content_file = parse(content_file, relations=True, lemmata=True).split(" ")
    res = [elt.split("/")[5] for elt in content_file]
    return " ".join(res)


from nltk.stem.snowball import FrenchStemmer
stemmer = FrenchStemmer()



import codecs
file_stopwords = codecs.open("frenchST.txt","r", encoding="utf-8")
stopwords = file_stopwords.read().split("\n")
file_stopwords.close()


ponctuation = set(string.punctuation)

def remove_infrequent_words(content_file, min_freq):
    content_file = np.array(content_file.split())
    freq_dist = nltk.FreqDist(content_file)
    for elt in freq_dist:
        if freq_dist[elt] < min_freq:
            content_file = np.delete(content_file, np.where(content_file == elt)[0])
    return ' '.join(content_file)

def stemming(content_file):
    content_file = content_file.split()
    for word in range(len(content_file)):
        content_file[word] = stemmer.stem(content_file[word].decode("utf-8"))
    return ' '.join(content_file)



def preprocess_data_file(content_file):
    content_file = content_file.lower()
    content_file = content_file.replace('’', " ")
    content_file = content_file.replace('«', " ")
    content_file = content_file.replace('»', " ")
    content_file = content_file.replace('—', " ")
    content_file = content_file.replace('\'', " ")
    content_file = content_file.replace('©', " ")
    content_file = content_file.replace('–', " ")
    content_file = content_file.replace('¿', " ")
    content_file = re.sub("[0-9]+", " ", content_file)
    content_file = ''.join(char for char in content_file if char not in ponctuation) #remove ponctuation
    content_file = remove_infrequent_words(content_file, 2)
    #content_file = stemming(content_file)
    return content_file


chemin_corpus = os.getcwd()+"/Corpus/"
list_dir = os.listdir(chemin_corpus)
infopath = []
infolength=[]
texts  =[]

for dir_annee in list_dir:
    sub_dirs_annee = os.listdir(chemin_corpus+dir_annee)
    for sub_dir in sub_dirs_annee:
        sub_sub_dirs = os.listdir(chemin_corpus+dir_annee+"/"+sub_dir)
        for sub_sub_dir in sub_sub_dirs:
            files = os.listdir(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir)
            for f in files:
                if os.stat(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir+"/"+f).st_size!=0:
                    open_file = codecs.open(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir+"/"+f, "r", encoding="utf-8")
                    content_file = open_file.read()
                    if detect(content_file)== "fr": #Detecter la langue
                        content_file = lemmatization(content_file)
                        content_file = preprocess_data_file(content_file)
                        content_file2 = []
                        for word in content_file.lower().split():
                            existe = False
                            for stopword in stopwords:
                                if word == stopword:
                                    existe = True
                                    break
                            if existe != True:
                                content_file2.append(word)

                        content_file = content_file2

                        # Eliminer les fichiers qui après pré-traitement deviennent trop petits
                        if len(content_file) > 5:
                            texts.append(content_file)
                            # print ' '.join(content_file)
                            # print
                            info = []
                            infopath.append(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir+"/"+f)
                            infolength.append(len(content_file))
                    open_file.close()




dictionary = corpora.Dictionary(texts)
corpus = [dictionary.doc2bow(text) for text in texts] #term_frequency


lda = models.LdaMulticore(corpus=corpus, id2word=dictionary, num_topics=5, workers = 2,chunksize=10000, passes=1)


# Enregistrer le output LDA
#pickle.dump(lda, open(os.getcwd()+"/lda_output", "wb"))


# Recharger le output LDA
#lda = pickle.load(open(os.getcwd()+"/lda_output", "rb"))



# print corpus #---->term frequency for each doc



def doc_lengths(texts):
    doc_lengths = []
    for text in texts:
        doc_lengths.append(len(text))
    return doc_lengths
#print doc_lengths(texts)



def doc_topic_dists(corpus, lda, num_topics):
    doc_topic_dists = []
    for doc in range(len(corpus)):
        list_topic_proba = [0]*num_topics
        temp = lda.get_document_topics(corpus[doc], minimum_probability=0)
        for topic, proba in temp:
            list_topic_proba[topic]=proba
        doc_topic_dists.append(list_topic_proba)
    return doc_topic_dists
doc_topic_dists = doc_topic_dists(corpus, lda)
print doc_topic_dists

def topic_term_dists(lda, num_topics, len_vocab):
    topic_term_dists = []
    for topic in range(num_topics):
        list_term_proba = [0]*len_vocab
        temp = lda.get_topic_terms(topic,  topn=len_vocab)
        for term, proba in temp:
            list_term_proba[term-1] = proba
        topic_term_dists.append(list_term_proba)
    return topic_term_dists

#topic_term_dists = topic_term_dists(lda, 5, len(dictionary.token2id))


def get_vocabularyIDs(dictionary_tokens):
    vocabulary = []
    for token in dictionary_tokens:
        vocabulary.append(dictionary_tokens[token])
    return vocabulary

# vocabularyIDs = get_vocabularyIDs(dictionary.token2id)   #Pour avoir tout le vocabulaire où chaque mot est représenté par son idf

def get_vocabularyAlpha(dictionary_tokens):
    vocabulary = []
    for token in dictionary_tokens:
        vocabulary.append(token)
    return vocabulary
# vocabulary_alpha = get_vocabularyAlpha(dictionary.token2id) #Pour avoir tout le vocabulaire où chaque mot est représenté dans sa forme normale





# #lda.update(other_corpus)
