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
    #content_file = content_file.replace('©', " ")
    content_file = ''.join(char for char in content_file if char not in ponctuation) #remove ponctuation

    content_file = remove_infrequent_words(content_file, 2)
    #content_file = stemming(content_file)
    return content_file


documents=[]
chemin_corpus = "Corpus/"
list_dir = os.listdir(chemin_corpus)
list_Files_len = []
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
                #Eliminer les fichiers qui après pré-traitement deviennent trop petits
                if len(content_file.split()) > 10:
                    documents.append(content_file)
                    info = []
                    info.append(chemin_corpus+dir_annee+"/"+sub_dir+"/"+sub_sub_dir+"/"+f)
                    info.append(len(content_file.split()))
                    list_Files_len.append(info)
                open_file.close()

file_stopwords = open("frenchST.txt", "r")
stopwords = file_stopwords.read()
stopwords = stopwords.split("\n")
file_stopwords.close()
texts = [[word for word in document.lower().split() if word not in stopwords] for document in documents]


dictionary = corpora.Dictionary(texts)
corpus = [dictionary.doc2bow(text) for text in texts] #term_frequency


lda = models.LdaMulticore(corpus=corpus, id2word=dictionary, num_topics=5, workers = 2,chunksize=10000, passes=1)

#pickle.dump(lda, open(os.getcwd()+"/lda_output", "wb"))
#lda = pickle.load(open(os.getcwd()+"/lda_output", "rb"))



# print corpus #---->term frequency for each doc



def doc_lengths(texts):
    doc_lengths = []
    for text in texts:
        doc_lengths.append(len(text))
    return doc_lengths
#print doc_lengths(texts)



def doc_topic_dists(corpus, lda):
    doc_topic_dists = []
    for doc in range(len(corpus)):
        list_topic_proba = []
        temp = lda.get_document_topics(corpus[doc], minimum_probability=0)
        for topic, proba in temp:
            list_topic_proba.append(proba)
        doc_topic_dists.append(list_topic_proba)
    return doc_topic_dists
doc_topic_dists = doc_topic_dists(corpus, lda)
print doc_topic_dists

def topic_term_dists(lda, num_topics, len_vocab):
    topic_term_dists = []
    for topic in range(num_topics):
        list_term_proba = [0]*len_vocab
        temp = lda.get_topic_terms(topic)
        for term, proba in temp:
            list_term_proba[term] = proba
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





#lda.update(other_corpus)
