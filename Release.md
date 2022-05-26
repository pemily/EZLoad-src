#How to create a new vresion

develop branch has a -SNAPSHOT version

To create a new version:

Merge develop on main branch
* git checkout main
* git merge origin/develop
* mvn versions:set `fill the new version (without -SNAPSHOT)`
* mvn clean install
* git add . 
* git commit -m "Set version X.Y"
* git push
* create a release on github 

Create new version in develop
* git checkout develop
* mvn versions:set `fill the new version (with -SNAPSHOT)`
* git add .
* git commit -m "Set version X.(Y+1)-SNAPSHOT"
* git push
 

#How to add copyrights
* mvn license:format 
* mvn versions:commit `To remove backuped pom.xml`
