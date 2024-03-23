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
package com.pascal.ezload.ai;

import com.pascal.ezload.ai.service.*;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.service.AiServices;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

public class HelloIAWorld {

    /*
    https://docs.langchain4j.dev/tutorials/tools
    https://github.com/quarkiverse/quarkus-langchain4j/blob/a6572529384588d9a04dca00a929afa719077a6c/docs/modules/ROOT/pages/agent-and-tools.adoc
    Please note that not all models support tools. Currently, the following models have tool support:

    OpenAiChatModel
    AzureOpenAiChatModel
    LocalAiChatModel
    QianfanChatModel

    */

    public static void main(String[] args) {
        // Create an instance of a model

        ChatLanguageModel model = OpenAiChatModel.builder()
                                    .apiKey(ApiKeys.Demo)
                                    .modelName("gpt-3.5-turbo") // "gpt-3.5-turbo" is the default
                                    .temperature(0d)
                                    .logRequests(false)
                                    .logResponses(false)
                                    .build();
/*
        ChatLanguageModel model = HuggingFaceChatModel.builder()
                .accessToken(ApiKeys.HuggingFace)
                .modelId("intfloat/multilingual-e5-base") // https://huggingface.co/spaces/mteb/leaderboard
                .temperature(0.1d)
                .waitForModel(false)
                .returnFullText(false)
                .build();
*/
        financialTest(model);
    }

    private static void financialTest(ChatLanguageModel model){
        List<FinancialData.FinancialDocumentSource> docs = List.of(new FinancialData.FinancialDocumentSource(
                        """
                                Below is a list of dividendes for each company with the date of the dividende.
                                The date format is: yyyy/mm/dd
                                The format is a css format (with comma separator):
                                companyCode, date, dividend value
                                """,
                """
                    avgo, 2023/01/01, 10.0
                    avgo, 2023/02/01, 12.0
                    avgo, 2023/03/01, 15.0
                    avgo, 2023/04/01, 16.0
                    avgo, 2023/05/01, 17.0
                    avgo, 2023/06/01, 18.0
                    """,
                null),
                new FinancialData.FinancialDocumentSource(
                        """
                                Below is a list of dividendes for each company with the date of the dividende.
                                The date format is: yyyy/mm/dd
                                The format is a css format (with comma separator):
                                companyCode, date, dividend value
                                """,
                        """
                            hpq, 2023/01/01, 22.0
                            hpq, 2023/02/01, 23.0
                            hpq, 2023/03/01, 24.0
                            hpq, 2023/04/01, 25.0
                            hpq, 2023/05/01, 26.0
                            hpq, 2023/06/01, 27.0
                            """,
                        null),
                new FinancialData.FinancialDocumentSource(
                    """
                    Below is the company name and its company code.
                    The format is css:
                    company name, company code         
                    """,
                        """
                        Hewlett packard, hpq
                        Broadcom, avgo
                        """
                ));

        // Si on veut avoir une memoire perssistente du chat, voir ici:
        // https://github.com/langchain4j/langchain4j-examples/blob/main/other-examples/src/main/java/ServiceWithPersistentMemoryExample.java
        MessageWindowChatMemory chatMemory = MessageWindowChatMemory
                                                    .builder()
                                                    .maxMessages(20)
                                                    .build();

        ExecutorService executor = Executors.newSingleThreadExecutor();

        RetrievalAugmentor retrievalAugmentor = new FinancialData().loadWithTextSegment(executor, model, docs);
        //RetrievalAugmentor retrievalAugmentor = new FinancialData().loadWithQueryRouter(model , docs);
        FinancialAIService extractor = AiServices.builder(FinancialAIService.class)
                                                .chatLanguageModel(model)
                                                .retrievalAugmentor(retrievalAugmentor)
                                                .chatMemory(chatMemory) // Pour pouvoir faire du stockage (utile pour faire des calculs avec Judge0JavaScriptExecutionTool)
                                                .build();
        String answer = null;

        String question =
                "Quel est le dividende de la société hewlett packard le 1er avril 2023?";
                //"What's the size of the company \"broadcom\"?";

        answer = extractor.answer(question);
        System.out.println(answer);

        chatMemory.clear();
        executor.shutdown();
    }


    interface Assistant {

        String chat(String userMessage);
    }

}
