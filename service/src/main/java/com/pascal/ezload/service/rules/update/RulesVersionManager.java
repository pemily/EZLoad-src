package com.pascal.ezload.service.rules.update;

import com.pascal.ezload.service.config.MainSettings;
import com.pascal.ezload.service.sources.Reporting;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.merge.ContentMergeStrategy;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RulesVersionManager {
    public enum FileState {
        NO_CHANGE, NEW, UPDATED, DELETED, CONFLICT
    }

    // Ces Info viennent du compte Github
    private static String owner = "pemily";
    private static String repo = "EZLoad-Rules";
    private static String originBranch = "EZLoad-1.0";
    private static String accessToken = "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";  // extracted from https://github.com/settings/tokens/801639196
                                                                                    // Le token ne doit avoir que l'access: public_repo


    // la doc officiel openapi de github: https://github.com/github/rest-api-description
    // https://github.com/github/rest-api-description/tree/main/descriptions-next/api.github.com

    private final MainSettings mainSettings;
    private Git git;

    public RulesVersionManager(MainSettings mainSettings){
        this.mainSettings = mainSettings;
    }

    public void synchSharedRulesFolder(Reporting reporting) throws Exception {
        try(Reporting rep = reporting.pushSection("Vérification des mises à jour")) {
            createLocalRepoIfNotExists(getAccountId());
            mergeOrigin();

            reporting.info("Vous êtes à jour");
        }
    }

    private String getAccountId() {
        return mainSettings.getEzLoad().getAdmin().getAccountId();
    }


    public void mergeOrigin() throws IOException, GitAPIException {
        initIfNeeded();
        // fetch the updates of the server
        git.fetch()
                .setForceUpdate(true)
                .call();

        git.merge()
                .setContentMergeStrategy(ContentMergeStrategy.CONFLICT)
                .setStrategy(MergeStrategy.SIMPLE_TWO_WAY_IN_CORE)
                .setFastForward(MergeCommand.FastForwardMode.FF)
                .include(git.getRepository().findRef("refs/remotes/origin/"+originBranch))
                .call();
        // en cas de conflit, le getStatus de chaque fichier
    }

    public List<RemoteBranch> getAllRemoteBranches() throws IOException, GitAPIException {
        initIfNeeded();
        return git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call()
                .stream().map(b -> new RemoteBranch(b.getName().substring("refs/remotes/origin/".length())))
                .collect(Collectors.toList());
    }

    public void createLocalRepoIfNotExists(String localBranch) throws GitAPIException, IOException {
        if (!CreateBranchCommand.isValidBranchName(localBranch)){
            throw new IllegalArgumentException("Le nom "+localBranch+" n'est pas un nom valide");
        }

        if (! new File(getRepoDir().getAbsolutePath()+File.separator+".git").exists()) {
            getRepoDir().mkdirs();

            Git.cloneRepository()
                    .setURI("https://github.com/" + owner + "/" + repo + ".git")
                    .setDirectory(getRepoDir())
                    .setBranchesToClone(Arrays.asList("refs/heads/" + originBranch))
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
        if (new File(getRepoDir().getAbsolutePath()+File.separator+".git").exists()) {
            if (this.git == null)
                this.git = Git.open(getRepoDir());
        }
        else
            throw new IOException("Git directory not created: "+getRepoDir().getAbsolutePath()+File.separator+".git");
    }

    // if success true, false if nothing to commit
    public boolean commitAndPush(String authorName, String authorEmail, String commitMessage) throws IOException, GitAPIException {
        initIfNeeded();
        git.add()
                .addFilepattern(".")
                .setUpdate(false) // take also the new files
                .call();
        try {
            git.commit()
                    .setAuthor(authorName, authorEmail)
                    .setAllowEmpty(false)
                    .setMessage(commitMessage)
                    .call();

            git.push()
                    // ici dans authorName, on peut mettre n'importe quoi, sauf "", ce n'est pas pris en compte, c'est le token qui compte
                    .setCredentialsProvider(new UsernamePasswordCredentialsProvider(authorName, accessToken))
                    .setForce(true)
                    .call();
        }
        catch(EmptyCommitException e){
            return false;
        }
        return true;
    }

    @NotNull
    private File getRepoDir() {
        return new File(mainSettings.getEzLoad().getRulesDir());
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
        if (! abosluteFilePath.startsWith(getRepoDir().getAbsolutePath()))
            throw new IOException("Bad file path: "+ abosluteFilePath +" not in "+getRepoDir());

        String filePath = abosluteFilePath.substring(getRepoDir().getAbsolutePath().length()+1).replace('\\', '/');
        return filePath;
    }
}
