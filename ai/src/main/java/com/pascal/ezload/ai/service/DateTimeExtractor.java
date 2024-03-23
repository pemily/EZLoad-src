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

import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface DateTimeExtractor {

    @UserMessage("Extract date from {{it}}")
    LocalDate extractDateFrom(String text);

    @UserMessage("Extract time from {{text}}")
    LocalTime extractTimeFrom(@V("text") String text);

    @UserMessage("Extract date and time from {{it}}")
    LocalDateTime extractDateTimeFrom(String text);

}
