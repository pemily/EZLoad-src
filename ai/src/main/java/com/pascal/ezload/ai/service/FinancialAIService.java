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

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

public interface FinancialAIService {

    @SystemMessage("You are a professional trader." +
            "You are polite and concise." +
            "If you don't know the answer, you must inform that you don't know. " +
            "If your confidence level of the answer is not very high, you must give the accuracy of the answer, " +
            "   for example: The precision of this answer is 85%" +
            "If the user asks for company details, you MUST always checks"+
            "If you have to give a full date (day, month, year), the format of the date will ALWAYS be dd/mm/yyyy"+
            "If you have to give a number, you ALWAYS give the number with 2 digits after the comma"

    )
    String answer(String question);

    @SystemMessage("Summarize every message from user in {{n}} bullet points. Provide only bullet points.")
    List<String> summarize(@UserMessage String text, @V("n") int n);

}
