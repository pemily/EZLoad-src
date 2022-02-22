package com.pascal.ezload.service.rules.update;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.sources.Reporting;
import com.pascal.ezload.service.util.FileProcessor;
import com.pascal.ezload.service.util.StringUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.JGitInternalException;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.dircache.DirCacheIterator;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.List;
import java.util.stream.Collectors;

public class RulesVersionManager {

    // Ces Info viennent du compte Github
    private static String owner = "pemily";
    private static String repo = "EZLoad";
    private static String originBranch = "EZLoad-1.0";
    private static String accessToken = "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";  // extracted from https://github.com/settings/tokens/801639196
                                                                                    // Le token ne doit avoir que l'access: public_repo


    // la doc officiel openapi de github: https://github.com/github/rest-api-description
    // https://github.com/github/rest-api-description/tree/main/descriptions-next/api.github.com

    private final String ezRepoDir;
    private final MainSettings mainSettings;
    private Git git;

    public RulesVersionManager(String ezRepoDir, MainSettings mainSettings){
        this.mainSettings = mainSettings;
        this.ezRepoDir = ezRepoDir;
    }

    public void initRepoIfNeeded() throws GitAPIException, IOException {
        createLocalRepoIfNotExists(getAccountId());
    }

    public void synchSharedRulesFolder(Reporting reporting) throws Exception {
        try(Reporting rep = reporting.pushSection("Vérification des mises à jour")) {
            if (mergeOrigin())
                reporting.info("Vous êtes à jour");
            else
                reporting.error("Vous avez un conflit dans les règles");
        }
    }

    private String getAccountId() {
        return mainSettings.getEzLoad().getAdmin().getBranchName();
    }


    public boolean mergeOrigin() throws IOException, GitAPIException {
        initIfNeeded();
        // fetch the updates of the server
        git.fetch()
                .setForceUpdate(true)
                .call();

        try {
            MergeResult mergeResult = git.merge()
                    .setContentMergeStrategy(ContentMergeStrategy.CONFLICT)
                    .setStrategy(MergeStrategy.SIMPLE_TWO_WAY_IN_CORE)
                    .setFastForward(MergeCommand.FastForwardMode.FF)
                    .include(git.getRepository().findRef("refs/remotes/origin/" + originBranch))
                    .call();

            return mergeResult.getMergeStatus() != MergeResult.MergeStatus.ABORTED
                    && mergeResult.getMergeStatus() != MergeResult.MergeStatus.FAILED
                    && mergeResult.getMergeStatus() != MergeResult.MergeStatus.CHECKOUT_CONFLICT
                    && mergeResult.getMergeStatus() != MergeResult.MergeStatus.NOT_SUPPORTED;
        }
        catch(NullPointerException | JGitInternalException e){
            return false;
        }
    }

    public List<RemoteBranch> getAllRemoteBranches() throws IOException, GitAPIException {
        initIfNeeded();
        return git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call()
                .stream().map(b -> new RemoteBranch(b.getName().substring("refs/remotes/origin/".length())))
                .collect(Collectors.toList());
    }

    private void createLocalRepoIfNotExists(String localBranch) throws GitAPIException, IOException {
        if (!CreateBranchCommand.isValidBranchName(localBranch)){
            throw new IllegalArgumentException("Le nom "+localBranch+" n'est pas un nom valide");
        }

        if (! new File(ezRepoDir+File.separator+".git").exists()) {
            new File(ezRepoDir).mkdirs();

            Git.cloneRepository()
                    .setURI("https://github.com/" + owner + "/" + repo + ".git")
                    .setDirectory(new File(ezRepoDir))
                    .setBranchesToClone(List.of("refs/heads/" + originBranch))
                    .setBranch("refs/heads/" + originBranch)
                    .call()
                    .branchCreate()
                    .setName(localBranch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                    .setForce(true) // si true => ecrase la branche du meme nom si elle existe
                    .setStartPoint(Constants.HEAD)
                    .call();

            initIfNeeded();

            git.checkout()
                    .setName(localBranch)
                    .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK)
                    .setForced(true)
                    .call();
        }
        else{
            initIfNeeded();
        }
    }

    private void initIfNeeded() throws IOException {
        if (new File(ezRepoDir+File.separator+".git").exists()) {
            if (this.git == null)
                this.git = Git.open(new File(ezRepoDir));
        }
        else
            throw new IOException("Git directory not created: "+ezRepoDir+File.separator+".git");
    }

    // if success true, false if nothing to commit
    public boolean commitAndPush(String authorEmail, String commitMessage) throws IOException, GitAPIException {
        initIfNeeded();
        git.add()
                .addFilepattern(".")
                .setUpdate(false) // take also the new files
                .call();
        try {
            git.commit()
                    .setAuthor(authorEmail, authorEmail)
                    .setAllowEmpty(false)
                    .setMessage(commitMessage)
                    .call();

            git.push()
                    // ici dans authorName, on peut mettre n'importe quoi, sauf "", ce n'est pas pris en compte, c'est le token qui compte
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(authorEmail, accessToken))
                    .setForce(true)
                    .call();
        }
        catch(EmptyCommitException e){
            return false;
        }
        return true;
    }

    public void revert(String absoluteFilePath) throws GitAPIException, IOException {
        initIfNeeded();
        git.checkout()
                .addPath(getGitFilePath(absoluteFilePath))
                .setStartPoint(Constants.HEAD)
                .call();
    }


    public FileState getState(String absoluteFilePath) throws IOException, GitAPIException {
        initIfNeeded();
        Status result = git.status()
                .call();

        String filePath = getGitFilePath(absoluteFilePath);
        if (result.getAdded().contains(filePath)) return FileState.NEW;
        if (result.getUntracked().contains(filePath)) return FileState.NEW;

        if (result.getChanged().contains(filePath)) return FileState.UPDATED;
        if (result.getModified().contains(filePath)) return FileState.UPDATED;

        if (result.getRemoved().contains(filePath)) return FileState.DELETED;
        if (result.getMissing().contains(filePath)) return FileState.DELETED;

        if (result.getConflicting().contains(filePath)) return FileState.CONFLICT;

        return FileState.NO_CHANGE;
    }

    @NotNull
    private String getGitFilePath(String abosluteFilePath) throws IOException {
        if (! abosluteFilePath.startsWith(ezRepoDir))
            throw new IOException("Bad file path: "+ abosluteFilePath +" not in "+ezRepoDir);

        return abosluteFilePath.substring(ezRepoDir.length()+1).replace('\\', '/');
    }

    public List<FileStatus> getAllChanges(String fromDirectory) throws IOException {
        initIfNeeded();
        return new FileProcessor(fromDirectory, f -> true, f -> true)
                .mapFile(filePath ->
                        {
                            try {
                                return new FileStatus(getGitFilePath(filePath), getState(filePath));
                            } catch (IOException | GitAPIException e) {
                                throw new RuntimeException(e);
                            }
                        }
                )
                .stream()
                .filter(f -> f.getFileState() != FileState.NO_CHANGE)
                .collect(Collectors.toList());
    }

    public String getChange(String absolutePath) throws Exception {
        initIfNeeded();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        DiffFormatter df = new DiffFormatter(result);
        df.setRepository(git.getRepository());
        DirCacheIterator oldTree = new DirCacheIterator(git.getRepository().readDirCache()); // the base user branch
        FileTreeIterator newTree = new FileTreeIterator(git.getRepository());
        df.setPathFilter(PathFilter.create(getGitFilePath(absolutePath)));
        df.format(oldTree, newTree);
        df.flush();
        df.close();

        String[] div = StringUtils.divide(result.toString(),"@@"); // remove the header
        if (div == null) return "";
        return "@@"+div[1];
    }

    private static AbstractTreeIterator prepareTreeParser(Repository repository, String ref) throws Exception {
        Ref head = repository.getRefDatabase().findRef(ref);
        RevWalk walk = new RevWalk(repository);
        RevCommit commit = walk.parseCommit(head.getObjectId());
        RevTree tree = walk.parseTree(commit.getTree().getId());

        CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
        ObjectReader oldReader = repository.newObjectReader();
        try {
            oldTreeParser.reset(oldReader, tree.getId());
        } finally {
            oldReader.close();
        }
        return oldTreeParser;
    }
}
