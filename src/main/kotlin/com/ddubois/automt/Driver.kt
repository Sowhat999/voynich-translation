package com.ddubois.automt

import com.ddubois.automt.com.dduboic.automt.voynich.downloadWholeThing
import com.ddubois.automt.com.dduboic.automt.voynich.loadFromFiles
import org.apache.logging.log4j.LogManager
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors
import org.deeplearning4j.models.word2vec.Word2Vec
import org.deeplearning4j.text.sentenceiterator.BasicLineIterator
import org.deeplearning4j.text.tokenization.tokenizer.preprocessor.CommonPreprocessor
import org.deeplearning4j.text.tokenization.tokenizerfactory.DefaultTokenizerFactory
import java.io.File

/**
 * Created by David on 06-Feb-16.
 */

val LOG = LogManager.getLogger("com.ddubois.automt.Driver");

fun loadVoynichModel() : WordVectors {
    var voynichVec : WordVectors;

    // Try to load the vectors from file
    var vectorsFile = File("corpa/voynich/vectors.w2v");
    if(vectorsFile.canRead()) {
        // Read the vectors from the file
        voynichVec = WordVectorSerializer.loadTxtVectors(vectorsFile);
    } else {
        voynichVec = learnVoynichVectors()
    }

    return voynichVec;
}

private fun learnVoynichVectors() : WordVectors {
    var voynichVec : WordVectors;
    var manuscript = loadFromFiles();
    LOG.info("Voynich manuscript loaded");

    var iter = BasicLineIterator(manuscript.toString().byteInputStream());
    var tokenizerFactory = DefaultTokenizerFactory();
    tokenizerFactory.setTokenPreProcessor(CommonPreprocessor());

    voynichVec = Word2Vec.Builder()
            .minWordFrequency(5)
            .iterations(5)
            .layerSize(300)
            .seed(42)
            .windowSize(5)
            .iterate(iter)
            .tokenizerFactory(tokenizerFactory)
            .build();

    LOG.info("Fitting Voynich word vectors...");
    voynichVec.fit();

    WordVectorSerializer.writeWordVectors(voynichVec, "corpa/voynich/vectors.w2v");
    return voynichVec
}

fun loadEnglishModel() : WordVectors {
    var LOG = LogManager.getLogger("Driver");
    var train = false;

    var englishVec : WordVectors;

    if(train) {
        var file = Thread.currentThread().contextClassLoader.getResource("raw_sentences.txt");
        var filePath = file.path;

        LOG.info("Load & vectorize sentances");
        var iter = BasicLineIterator(filePath);
        var tokenizerFactory = DefaultTokenizerFactory();
        tokenizerFactory.setTokenPreProcessor(CommonPreprocessor());

        LOG.info("Building model...");
        englishVec = Word2Vec.Builder()
                .minWordFrequency(5)
                .iterations(2)
                .layerSize(300)
                .seed(42)
                .windowSize(5)
                .iterate(iter)
                .tokenizerFactory(tokenizerFactory)
                .build();

        LOG.info("Fitting Word2Vec model...");
        englishVec.fit();

        LOG.info("Writing word vectors to file...");

        WordVectorSerializer.writeWordVectors(englishVec, "vectors.tzt");

        // Closest: [game, week, public, year, director, night, season, time, office, group]

    } else {
        LOG.info("Loading google model file");
        var gModelFile = File("corpa/english/GoogleNews-vectors-negative300.bin");
        englishVec = WordVectorSerializer.loadGoogleModel(gModelFile, true);
        // Closest: [afternoon, hours, week, month, hour, weekend, days, time, evening, morning]
    }

    return englishVec;
}

fun loadSpanishModel(): WordVectors {
    var spanishFolder = File("corpa/spanish");
    var spanishVecBuilder = Word2Vec.Builder()
            .minWordFrequency(5)
            .iterations(2)
            .layerSize(100)
            .seed(42)
            .windowSize(5);

    for(file in spanishFolder.listFiles()) {
        spanishVecBuilder.iterate(BasicLineIterator(file.path));
    }

    var tokenizerFactory = DefaultTokenizerFactory();
    tokenizerFactory.setTokenPreProcessor(CommonPreprocessor());
    var spanishVec = spanishVecBuilder.tokenizerFactory(tokenizerFactory).build();

    spanishVec.fit();

    return spanishVec;
}

fun main(args : Array<String>) {
    var voynichModel = loadVoynichModel();
    var englishMode = loadEnglishModel();
}
