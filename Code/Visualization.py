#Run the lda modal first
#All methods in this file uses 

import os
from gensim import corpora, models
import numpy as np
from itertools import groupby
from operator import itemgetter

def get_term_frequency(corpus):
    term_frequency= []
    flatenText = [item for sublist in corpus for item in sublist]
    first = itemgetter(0)
    sums ={}
    sums = {(k, sum(item[1] for item in tups_to_sum))
        for k, tups_to_sum in groupby(sorted(flatenText, key=first), key=first)}
    for t in sums:
        term_frequency.append(t[1])
    return term_frequency

def pyLDAvisData(lda, num_topics, len_vocab, corpus, text, dictionary_tokens):
    data = {'topic_term_dists':topic_term_dists(lda,num_topics,len_vocab), 
            'doc_topic_dists': doc_topic_dists(corpus, lda),
            'doc_lengths': doc_lengths(text),
            'vocab': get_vocabularyAlpha(dictionary_tokens),
            'term_frequency':get_term_frequency(corpus)
           }
    return data
  
# 1 - PyLDAvis
import pyLDAvis

data = pyLDAvisData(lda, 5, len(dictionary.token2id), corpus, texts, dictionary.token2id)
topics_model_data = data
topics_vis_data = pyLDAvis.prepare(**topics_model_data)
pyLDAvis.display(topics_vis_data)


# 2 - Tendance des topics
import matplotlib.pyplot as plt
from collections import Counter
%matplotlib inline 

def get_topic_apperences_year_month(data, info):
    dict_topics = {}
    for i in range(0,len(data)):
        idt = data[i].index(max(data[i]))
        dict_topics.setdefault(idt, []).append(info[i][0][7:14])
    return dict_topics

def get_topic_apperences_year(data, info):
    dict_topics = {}
    for i in range(0,len(data)):
        idt = data[i].index(max(data[i]))
        dict_topics.setdefault(idt, []).append(info[i][0][7:11])
    return dict_topics

def find_indices(lst, condition):
    return [i for i, elem in enumerate(lst) if condition(elem)]

def get_topic_apperences_year_month_threshold(data, info,threshold):
    dict_topics = {}
    for i in range(0,len(data)):
        idt = find_indices(data[i], lambda e: e > 0.1)
        for j in idt:
            dict_topics.setdefault(j, []).append(info[i][0][7:14])
    return dict_topics

def get_topic_apperences_year_threshold(data, info, threshold):
    dict_topics = {}
    for i in range(0,len(data)):
        idt = find_indices(data[i], lambda e: e > 0.1)
        for j in idt:
            dict_topics.setdefault(j, []).append(info[i][0][7:11])
    return dict_topics

def plot_topic_tendences(topic, data, info, typePlot='bar', period='year', threshold = 0):
    topic_doc_apperence = {}
    if (threshold > 0):
        if (period =='year'):
            topic_doc_apperence = get_topic_apperences_year(data, info)
        else :
            topic_doc_apperence = get_topic_apperences_year_month(data, info)
    else:
        if (period =='year'):
            topic_doc_apperence = get_topic_apperences_year_threshold(data, info,threshold)
        else :
            topic_doc_apperence = get_topic_apperences_year_month_threshold(data, info, threshold)
  
        
    cnt =Counter(topic_doc_apperence[topic])
    cnt = sorted(cnt.items(),key=itemgetter(0))
    dates = []
    frequencies = []
    for c in cnt:
        dates.append(str(c[0]))
        frequencies.append(c[1])
    x = [i for i in range(len(dates))]
    plt.xticks(x, dates)
    if (typePlot == 'bar'):
        plt.bar(x,frequencies,color=next(prop_iter)['color'])
    else:
        plt.plot(x, frequencies, marker='x')
      
    plt.setp(plt.xticks()[1], rotation=90)
    plt.title('Topic ' + str(topic+1) + ' tendance over the corpus')
    plt.xlabel('Dates')
    plt.ylabel('# Aapperences')
    plt.legend() 
    plt.show()
#couleurs  
prop_iter = iter(plt.rcParams['axes.prop_cycle'])
#params : num_topic, doc_topic_dists, list_Files_len (les paths des fichier), type de plot (bar ou line)
#periode (année ou mois), threshold)
#si threshold > 0, il récupére plusieurs topic par documents
#si threshold = 0, one topic par document (celui avec la plus grande proba)
plot_topic_tendences(1, data['doc_topic_dists'], list_Files_len, 'line', period='month', threshold=0.1)

# 3 - Cloud tags
from wordcloud import WordCloud
from PIL import Image
import numpy as np

def topics_to_counter(topic, nbrwords):
    word_prob =(lda.print_topic(topic, topn=nbrwords)).split("+")
    cnt = Counter()
    for w in word_prob:
        cnt[w.rsplit('*', 1)[1]] = float(w.rsplit('*', 1)[0])*1000
    return cnt

def cloudTag(cnt, mask):
    alice_mask = np.array(Image.open(mask))

    wordcloud = WordCloud(background_color="white", width=800, height=600, relative_scaling=.8)\
                .generate_from_frequencies(cnt.items())
    wordcloud.to_file("skills-cloud.png")

    plt.imshow(wordcloud)
    plt.axis("off")
    plt.show()
    
# Pour le mask, télécharger l'une de ces photos :     
cnt = topics_to_counter(1, 100)
cloudTag(cnt, 'mask.png')

# 4 - Topic shares

import random
def get_topics_documents_matrice(data, info, topics, documents):
    indexes = [random.randrange(len(data)) for i in range(documents)]
    doctopic = np.array([])
    docnames = []
    for i in range(0,len(indexes)):
        if (i==0):
            doctopic =  np.hstack((doctopic, np.array(data[indexes[i]])))
            docnames.append(list_Files_len[indexes[i]][0][18:])
        else:
            doctopic =  np.vstack((doctopic, np.array(data[indexes[i]])))
            docnames.append(list_Files_len[indexes[i]][0][18:])
    return doctopic, docnames

def topics_share_graphe(doctopic, docnames):
    N, K = np.array(doctopic).shape # N documents, K topics
    ind = np.arange(N)  # the x-axis locations for the novels
    width = 0.5  # the width of the bars
    plots = []
    height_cumulative = np.zeros(N)
    prop_iter = iter(plt.rcParams['axes.prop_cycle'])
    for k in range(K):
        if k == 0:
            p = plt.bar(ind, doctopic[:, k], width, color=next(prop_iter)['color'])
        else:
            p = plt.bar(ind, doctopic[:, k], width, bottom=height_cumulative, color=next(prop_iter)['color'])
        height_cumulative += doctopic[:, k]
        plots.append(p)

    plt.ylim((0, 1))
    plt.ylabel('Topics')
    plt.title('Topics in novels')
    plt.xticks(ind+width/2, docnames)
    plt.setp(plt.xticks()[1], rotation=90)
    plt.yticks(np.arange(0, 1, 10))
    topic_labels = ['Topic #{}'.format(k) for k in range(K)]
    plt.legend([p[0] for p in plots], topic_labels)
    
    plt.show()
def heatmap(doctopic, docnames):
    plt.pcolor(doctopic, norm=None, cmap='Blues')
    plt.yticks(np.arange(doctopic.shape[0])+0.5, docnames);
    plt.xticks(np.arange(doctopic.shape[1])+0.5, topic_labels);
    plt.gca().invert_yaxis()
    plt.xticks(rotation=90)
    plt.colorbar(cmap='Blues')
    plt.tight_layout() 
    plt.figure(figsize=(400,40))
    plt.show()

doctopic, docnames = get_topics_documents_matrice(data['doc_topic_dists'], list_Files_len, 5,5)
topics_share_graphe(doctopic, docnames)

# 5 - Heatmap
heatmap(doctopic, docnames)
