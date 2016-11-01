import whoosh.index as index
from whoosh.fields import Schema, ID, TEXT
from whoosh.index import create_in
from whoosh.query import Term

def create_index(corpus, indexPath = "index"):

    """ Create index from corpus
    """
    
    # TODO change reader according to corus structure
    
    schema = Schema(content=TEXT(stored=True),nid=ID(stored=True))
    
    if not os.path.exists(indexPath):
        os.mkdir(indexPath)
    
    index = create_in(indexPath, schema)
    
    writer = index.writer()
    
    for texts in corpus:
        writer.update_document(content=post["content"],
                           nid=unicode(post["_id"]))
    writer.commit()
 
 
 def pmi_words(searcher, term1, term2):
    """ Compute pmi between term1 and term 2
    """
    doc_count =float(searcher.doc_count())
    tf1 = float(searcher.doc_frequency("content", term1)) + 1
    tf2 = float(.searcher.doc_frequency("content", term2)) + 1
    coocc = float(len(searcher.search(And([Term("content",term1),Term("content",term2)]))))
    
    return math.log(coocc * doc_count /(tf1*tf2) ,2) / (-math.log(coocc/doc_count ,2))

 def pmi_topic(searcher, term, topic):
    """ Compute pmi between term1 and term 2
    """
    doc_count =float(searcher.doc_count())
    tf1 = float(searcher.doc_frequency("content", term1)) + 1
    tf2 = #complete with structure
    
    coocc = #complete with structure
    
    return #complete with structure