"""
Super main Python file to run the whole thing

Relies on gensim and python2
"""

import logging
import os.path
import sys
from gensim.models import Word2Vec
from gensim.models.word2vec import LineSentence

download = False
concatenate = True
train = True

# configure logging
logging.basicConfig(filename='all.log', level=logging.DEBUG)

if __name__ == '__main__':
    program = os.path.basename(sys.argv[0])
    logger = logging.getLogger(program)

    # Download all the files
    if download:
        from download import download_files
        download_files()
        print 'Downloaded files'

    if concatenate:
        from concatenate import concatenate_files
        concatenate_files()
        print 'Build manuscript'

    if train:
        # Load in the voynich manuscript file and train word2vec on it
        voynich_model = Word2Vec(LineSentence('../corpa/voynich/manuscript.evt'), min_count=1)
        logging.info('Loaded voynich model')

        # Load in an English Word2vec model to ensure that our methodology is sound
        # english_model = Word2Vec(LineSentence('../corpa/english/raw_sentences.txt'))
        # logging.info('Loaded english model')

        # logging.info('Words most similar to day: ' + english_model.most_similar('day'))
        print 'words most similar to octhey:', voynich_model.most_similar('octhey')
        print 'Similarity between octhey and ocphy:', voynich_model.similarity('octhey', 'ocphy')
        print 'Similarity between octhey and qoekaiin:', voynich_model.similarity('octhey', 'qoekaiin')

        print 'Similarities between maybe translated words:'

        words = ['keerodal', 'kydainy', 'koaiin', 'kooiin', 'keedey']
        for word1 in words:
            for word2 in words:
                print 'Similarity between', word1, 'and', word2, ':', voynich_model.similarity(word1, word2)

        words_english = ['corriander', 'centaurea', 'hellebore', 'cumin', 'cotton']

        # print 'Checking English words...'
        # for word1 in words_english:
        #     for word2 in words_english:
        #         try:
        #             print 'Similarity between', word1, 'and', word2, ':', voynich_model.similarity(word1, word2)
        #         except:
        #             print 'Could not determine similarity'
