#How to create a new vresion

develop branch has a -SNAPSHOT version

To create a new version:

Merge develop on main branch
* mvn versions:set `fill the new version (without -SNAPSHOT)`
* mvn install
* git add . 
* git commit -m "New version X.Y"
* git push

#How to add copyrights
* mvn license:format 
* mvn versions:commit `To remove backuped pom.xml`
