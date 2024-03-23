/**
 * ai - EZLoad an automatic loader for EZPortfolio
 * Copyright © 2021 EMILY Pascal (pascal.emily@gmail.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.pascal.ezload.ai.service;

import dev.langchain4j.data.document.*;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.query.router.LanguageModelQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.rag.query.transformer.CompressingQueryTransformer;
import dev.langchain4j.rag.query.transformer.QueryTransformer;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FinancialData {

    public RetrievalAugmentor loadWithQueryRouter(ChatLanguageModel model, List<FinancialDocumentSource> sources){
        // https://github.com/langchain4j/langchain4j-examples/blob/main/rag-examples/src/main/java/_01_Naive_RAG.java

        Map<ContentRetriever, String> retrieverToDescription = new HashMap<>();
        for (FinancialDocumentSource source : sources) {
            retrieverToDescription.put(getContentRetrieverWithDocSplitter(source), source.docDescription);
        }
        QueryRouter queryRouter = new LanguageModelQueryRouter(model, retrieverToDescription);

        QueryTransformer queryTransformer = new CompressingQueryTransformer(model);
        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .queryRouter(queryRouter)
                .build();
    }


    private ContentRetriever getContentRetrieverWithDocSplitter(FinancialDocumentSource docSource) {
        DocumentParser documentParser = new TextDocumentParser();
        Document doc = DocumentLoader.load(docSource, documentParser);

        DocumentSplitter splitter = DocumentSplitters.recursive(1000, 200); // je decoupe par morceaux de 1000 caracteres avec un overlap entre les blocks de 200 caracteres
        List<TextSegment> segments = splitter.split(doc);

        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        List<Embedding> embeddings = embeddingModel.embedAll(segments).content();

        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();
        embeddingStore.addAll(embeddings, segments);

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();
    }

    public RetrievalAugmentor loadWithTextSegment(Executor executor, ChatLanguageModel model, List<FinancialDocumentSource> docSources) {
        QueryTransformer queryTransformer = new CompressingQueryTransformer(model);
        return DefaultRetrievalAugmentor.builder()
                .queryTransformer(queryTransformer)
                .executor(executor)
                .contentRetriever(getContentRetriverWithTextSegment(docSources))
                .build();
    }

    private ContentRetriever getContentRetriverWithTextSegment(List<FinancialDocumentSource> docSources){
        EmbeddingModel embeddingModel = new AllMiniLmL6V2EmbeddingModel();
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

        docSources.forEach(docSource -> {
            TextSegment doc = new TextSegment(docSource.docDescription + "\n" + docSource.doc, docSource.metadata());
            Embedding embeddings = embeddingModel.embed(doc).content();
            embeddingStore.add(embeddings, doc);
        });

        return EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(2) // on each interaction we will retrieve the 2 most relevant segments
                .minScore(0.5) // we want to retrieve segments at least somewhat similar to user query
                .build();
    }


    public static class FinancialDocumentSource implements DocumentSource {

        private final String docDescription, doc;
        private final Metadata metadata;

        public FinancialDocumentSource(String docDescription, String doc, Map<String, ?> metadata){
            this.docDescription = docDescription;
            this.doc = doc;
            this.metadata = metadata == null ? new Metadata() : new Metadata(metadata);
        }

        public FinancialDocumentSource(String docDescription, String doc){
            this.docDescription = docDescription;
            this.doc = doc;
            this.metadata = new Metadata();
        }

        @Override
        public InputStream inputStream() {
            return new ByteArrayInputStream(doc.getBytes(StandardCharsets.UTF_8));
        }

        @Override
        public Metadata metadata() {
            return metadata;
        }
    }

}
