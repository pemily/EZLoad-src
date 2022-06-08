#How to create a new version

develop branch has a -SNAPSHOT version

To create a new version:

* Complete the history in file exec/src/main/resources/ReadMe.md
* Commit and Push on develop

Merge develop on main branch
* git checkout main
* git fetch
* git merge origin/develop `Sur les conflits prendre tous les theirs (develop)`
* mvn versions:set `fill the new version (without -SNAPSHOT)`
* mvn clean install
* git add . 
* git commit -m "Set version X.Y"
* git push
* create a release on github: https://github.com/pemily/EZLoad-src/releases
  * Cliquez sur Draft a new release
  * Cliquez sur choose a tag et entrer la version, exemple: "v1.2" puis "Create new tag v1.2 on publish"
  * Target: main
  * Release title: V1.2
  * Describe this release: copier le contenu des changements venant de exec/src/main/resources/ReadMe.md
  * Aller dans le repertoire /release et prendre tous les fichiers pour les dropper sur la release
  * Cliquez sur Publish Release
  * Mail d'annonce:
    * Nouvelle version ici: https://github.com/pemily/EZLoad-src/releases télécharger le .zip

Create new version in develop
* git checkout develop
* mvn versions:set `fill the new version (with -SNAPSHOT)`
* git add .
* git commit -m "Set version X.(Y+1)-SNAPSHOT"
* git push
 

#How to add copyrights
* mvn license:format 
* mvn versions:commit `To remove backuped pom.xml`
