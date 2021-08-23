package com.pascal.bientotrentier.sources;

import com.pascal.bientotrentier.model.BRModel;

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

    public FileProcessor(String rootDirectory, Predicate<File> dirFilter, Predicate<File> fileFiter){
        this.rootDirectory = rootDirectory;
        this.dirFilter = dirFilter;
        this.fileFiter = fileFiter;
    }

    public List<BRModel> forEachFiles(Function<String, BRModel> processFile) throws IOException {
        try(Stream<Path> stream = Files.walk(Paths.get(rootDirectory), 5)){
            return stream
                    .filter(Files::isRegularFile)
                    .filter(f -> dirFilter.test(f.toFile().getParentFile()))
                    .filter(f -> fileFiter.test(f.toFile()))
                    .sorted()
                    .map(p -> processFile.apply(p.toFile().getAbsolutePath()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
}
