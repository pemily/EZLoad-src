
Source:


https://github.com/github/rest-api-description/blob/main/descriptions-next/api.github.com/api.github.com.yaml

J'ai enlevé le -- et le openapi: 3.0.0 a ete remplacé par 3.0.0


J'ai modifié le yaml:

Components/schemas/reaction-rollup/properties/"+1" et "-1" 
```
- "+1":
    type: integer
  "-1":
    type: integer
```
- ***Probleme*** => genere deux variables _1 en java
- ***Solution*** => j'ai supprimé les 2 properties suivantes


Dans Components/schemas/pull-request-review-comment et Components/schemas/review-comment, 
il y a :
```
    enum:
    - LEFT
    - RIGHT
    - 
    default: RIGHT
```
- ***Probleme*** => l'assignation par defaut a RIGHT, ne genere pas le bon code d'init
- ***Solution*** => j'ai supprimé tous les default: RIGHT présent dans le fichier (4 en tout)

