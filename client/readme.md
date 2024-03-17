
#First step
##Pour creer l'application au départ
 j'ai utilisé: npx create-react-app my-app --template redux-typescript
 
# developement 
 - telecharger la meme version de node que celle dans le vichier client/pom.xml <nodeVersion> 
 - mvn install a la racine
 - npm install dans le repertoire client
 - lancer le http server (main java dans le repertoire server)
 - lancer npm start dans le repertoire client client
 
# Pour mettre a jour les modules:
npm install grommet@latest
npm install grommet-controls@latest
npm install grommet-icons@latest
npm install grommet-theme-hpe@latest
npm install grommet-v2@latest
# puis pour mettre a jour les autres components
npx npm-check-updates -u
npm install 

# nettoyage du cache npm 
npm cache clean --force

# pour trouver les dependances:
npm ls react-redux
npm ls lru-cache