import logging
import operator

import numpy as np


logger = logging.getLogger('space')


class Space(object):

    def __init__(self, matrix_, id2row_):

        self.mat = matrix_
        self.id2row = id2row_
        self.create_row2id()

    def create_row2id(self):
        self.row2id = {}
        for idx, word in enumerate(self.id2row):
            if word in self.row2id:
                raise ValueError("Found duplicate word: %s" % (word))
            self.row2id[word] = idx


    @classmethod
    def build(cls, fname, lexicon=None):

        #if lexicon is provided, only data occurring in the lexicon is loaded
        id2row = []
        def filter_lines(f): 
            for i,line in enumerate(f): 
                word = line.split()[0]
                if i != 0 and (lexicon is None or word in lexicon) and len(line.split()) > 2:
                    id2row.append(word)
                    yield line

        #get the number of columns
        with open(fname) as f:
            f.readline()
            ncols = len(f.readline().split())

        with open(fname) as f: 
            logger.debug('Loading words from %s' % fname)
            logger.debug('Using %d columns' % (ncols - 1))
            m = np.matrix(np.loadtxt(filter_lines(f), comments=None, usecols=range(1,ncols)))

        return Space(m, id2row)

    def normalize(self):
        row_norms = np.sqrt(np.multiply(self.mat, self.mat).sum(1))
        row_norms = row_norms.astype(np.double)
        row_norms[row_norms != 0] = np.array(1.0/row_norms[row_norms != 0]).flatten()
        self.mat = np.multiply(self.mat, row_norms)

    def get_closest_words(self, word, num):
        """Gets the closest num words to the given word

        Implements a simple linear search through the target space, calculating the cosing distance between the target
        word and all the words in this space. Then, sorts all the distances and returns the top num distances

        :param word: The embedding of the word to get the closest words to
        :param num: The number of words to return

        :return: A list of tuples, where the first element in each tuple is the word and the second element is the
        cosine distance from that word to the source word
        """

        similarities = dict()
        for idx, embedding in enumerate(self.mat):
            similarities[idx] = np.dot(embedding, word)

        sorted_words = sorted(similarities.items(), key=operator.itemgetter(1))

        top_words = sorted_words[:num]

        return map(lambda x: (self.id2row[x[0]], x[1]), top_words)

