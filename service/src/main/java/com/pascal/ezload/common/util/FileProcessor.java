/**
 * ezService - EZLoad an automatic loader for EZPortfolio
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
package com.pascal.ezload.common.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileProcessor {

    private final String rootDirectory;
    private final Predicate<File> dirFilter, fileFiter;

    public FileProcessor(String rootDirectory, Predicate<File> dirFilter, Predicate<File> fileFilter){
        this.rootDirectory = rootDirectory;
        this.dirFilter = dirFilter;
        this.fileFiter = fileFilter;
    }

    public <FileResult> List<FileResult> mapFile(Function<String, FileResult> processFile) throws IOException {
        try(Stream<Path> stream = Files.walk(Paths.get(rootDirectory), 5)){
            return stream
                    .filter(Files::isRegularFile)
                    .filter(f -> dirFilter.test(f.toFile().getParentFile()))
                    .filter(f -> fileFiter.test(f.toFile()))
                    .sorted() // sort the files per date (use the name for that, it contains the date yyy/mm/dd)
                    .map(p -> processFile.apply(p.toFile().getAbsolutePath()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}
