# -*- coding: utf-8 -*-
import os
from gensim import corpora, models



documents=[]

list_dir = os.listdir("2015")
for dir in list_dir:
    sub_dirs = os.listdir("2015/"+dir)
    for sub_sub_dir in sub_dirs:
        files = os.listdir("2015/"+dir+"/"+sub_sub_dir)
        for f in files[0:2]:
            open_file = open("2015/"+dir+"/"+sub_sub_dir+"/"+f, "r")
            content_file = open_file.read()
            documents.append(content_file)
            open_file.close()

file_stopwords = open("frenchST.txt", "r")
stopwords = file_stopwords.read()
stopwords = stopwords.split("\n")   #Problème d'Encodage
file_stopwords.close()

texts = [[word for word in document.lower().split() if word not in stopwords] for document in documents]
dictionary = corpora.Dictionary(texts)
corpus = [dictionary.doc2bow(text) for text in texts]

lda = models.ldamodel.LdaModel(corpus=corpus, id2word=dictionary, num_topics=5, update_every=1, chunksize=10000, passes=1)

print lda.print_topics(5)
