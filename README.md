<h1 align="center">
    Projet Environnement Big Data
</h1>

<p align="center">
  <strong>Réalisé par Iman, Robin & Sabri</strong><br>
</p>

*******************

## 📖 Introduction

Apache Spark est un moteur de traitement de données rapide et dédié qui permet d’effectuer un traitement de larges volumes de données de manière distribuée. Très en vogue depuis maintenant quelques années, il est de plus en plus accessible grâce au support multi-langage qu’il propose. En effet, le framework supporte différents langages de programmation tels que Java, Scala, Python et R. 

Cependant, parmi ces langages on remarque l’absence d’un langage tout aussi populaire et qui n’est pourtant pas supporté : le Javascript. 

Il existe certains projets open-source intéressants tel que EclairJS qui ont essayé de fournir un environnement capable d’utiliser Spark au sein d’un serveur NodeJS mais ce dernier n’est malheureusement plus maintenu depuis quelques années. 

Nous vous montrerons aujourd’hui une solution afin d’utiliser Spark dans un environnement NodeJs. 

Pour ce faire, nous créerons une application permettant de traiter et afficher en temps réel les hashtags les plus utilisés en utilisant l’API twitter et l’extension Apache Spark Streaming. 

## 📋 Liste des tâches réalisées

* Réalisation d'une application full Javascript permettant d'afficher le Top 10 des Hashtags provenant d'un stream de tweets en utilisant Twitter API et Apache Spark Streaming
* Réalisation de la même application en combinant Javascript et Java pour pallier aux problèmes de multithreading (voir partie Difficultés)
* Déploiement de l'application sans docker
* Déploiement de l'application avec docker

## 🖥️ GraalVM

Pour pallier le manque d'implémentation Javascript du framework Spark, nous allons utiliser le projet GraalVM. C'est une extension de la machine virtuelle JAVA (JVM) qui permet de supporter plus de langages et de mode d'exécution. Cette machine virtuelle polyglote permet d'exécuter du code de différents langages dans un même environnement. Les langages pris en charge sont les suivants :
* NodeJS
* Java
* Python
* R
* Ruby
* C/C++
* Et d'autres

Tous ces langages peuvent intéragir entre eux. Il est par exemple possible de créer une application express (en JS) utilsant du code JAVA et c'est justement ce qu'on nous avons réalisé. En effet, notre but sera d’importer des packages java contenant les fonctions de Spark pour ensuite les utiliser en langage Javascript.

## Implémentation Full JS

Notre objectif principal était de construire l'application en utilisant uniquement NodeJS.
Les premières parties de l’implémentation ont été un succès, nous avons réussi à :
1) Importer les packages Java Spark
2) Créer un contexte Spark
3) Récupérer un stream de tweets via l’API Twitter
4) Utiliser des fonctions de transformations sur les RDD.

Cependant la dernière étape a posé problème à cause d’un concept qui n’est pas intrinsèquement supporté par NodeJS : le multithreading.
```Multi threaded access requested by thread Thread[streaming-job-executor-0,5,main] but is not allowed for language(s) js.```

En effet, la fonction foreachRDD de Spark utilise du multithreading afin de dispatcher le traitement des RDD, ce qui n’est malheureusement pas possible en Javascript qui est mono-thread.
Il existe un moyen de faire du multithreading, en utilisant les workers, qui est présent dans les dernières versions de NodeJS mais cela ne compense pas le problème.

Vous trouverez dans le repo le fichier sparkFullJs.js qui comporte le code en full JS de l’application.
C'est pourquoi nous avons opté pour une implémentation mélant Javascript et Java afin de pallier le problème de multithreading.

## Implémentation JS/Java

Nous avons donc décider d’implémenter cette application en utilisant du Javascript et Java. Le Javascript nous permet de réaliser la partie « frontend » et l’appel au code Java qui lui est chargé de récupérer et de traiter les tweets à partir de l’API Twitter. 

La machine virtuelle GraalVM nous permet d’utiliser du code java dans une application Javascript et c’est ce que nous avons réalisé. 

Dans un premier temps, nous avons produit un code permettant de créer un « stream » depuis l’API Twitter en utilisant la librairie Twitter4J (http://twitter4j.org/en/) ainsi que les packages de la librairie Spark Java (https://spark.apache.org/docs/latest/api/java/index.html).

#### Présentation du code JAVA

La première étape est s'authentifier auprès de l'API Twitter par le biais de la librairie Twitter4J. Une fois cela réalisé, nous pouvons accéder au stream de tweets et les traiter. Les étapes a réaliser pour obtenir le top 10 des hashtags les plus citées sont les suivantes :

1. Filtre pour récupérer uniquement les tweets en anglais
2. Récupérer les hashtags
3. Exécuter une opération de MapReduce pour déterminer le nombre de citations de chaque hashtag
4. Tri des hashtags par nombre de citations
5. Application d'un forEachRDD pour récupérer les 10 hashtags les plus cités

#### Présentation du code Javascript

Dans ce projet, deux fichiers javascript sont présents, l'un permet de lancer l'application sur un serveur node et l'autre réalise l'appel au code Java permettant de récupérer les données récoltées sur le stream. Pour cela, GraalVM permet l'importation de package Java dans du Javascript. Voici le code permettant une telle chose :
```javascript
var TwitterSparkStreaming = Java.type("com.ece.demo.spark.streaming.twitter.TwitterSparkStreaming");
var tss = new TwitterSparkStreaming();
```
Par la suite, un worker javascript est lancé. Il permet l'exécution, en parallèle, de la méthode Java qui récupére et traite les données de Twitter. Ces données sont ensuite envoyées au serveur node et accessibles sur l'URL `http://localhost:8000/`.

## 🚀 Lancer l'application sans docker

Premièrement, il faut installer GraalVM : https://www.graalvm.org/downloads/

⚠️ Attention ! ⚠️ Prenez de préférence la version incluant Java 11 pour s’assurer de la compatibilité avec Spark 3. En effet, les jars présents dans ce repo incluent la version 3 de Spark. 

Il s’agit maintenant de préparer notre environnement GraalVM en exécutant la commande suivante : 

```export PATH=graalvm-ce-java11-20.3.0/bin:$PATH```

Si la commande a bien fonctionné vous devriez normalement utiliser l’environnement GraalVM en exécutant node. Vous pouvez le vérifier en tapant : 

```which node``` 

L’étape suivante va permettre d’utiliser les JARS de Spark dans notre environnement NodeJS. Malheureusement il n’est pas possible d’ajouter tout le dossier /jars dans GraalVM. 

Nous utiliserons donc la commande artisanale suivante pour ajouter tous les JARS dans une variable d’environnement : 

```CLASSPATH=$(find "jars/" -name '*.jar' | xargs echo | tr ' ' ':')```

Ensuite vous pouvez lancer l'application en utilisant la commande suivante :

```node --jvm --vm.cp $CLASSPATH js/serverTwitterSpark.js```

Une fois l'application lancée, le top 10 hashtags les plus cités sont disponibles sur `http://localhost:8000/`.

## 🐳 Lancer l'application avec docker

Il est possible de lancer l'application par le biais d'une image docker. Cette dernière est construite autour de l'image docker de GraalVM (https://hub.docker.com/r/oracle/graalvm-ce). Nous avons produit un dockerfile qui permet d'installer les modules node express et cors. De plus, il permet d'exposer les ports nécessaires et de lancer le script shell suivant : 
```
#!/bin/bash

CLASSPATH=$(find "./jars/" -name '*.jar' | xargs echo | tr ' ' ':')

node --jvm --vm.cp $CLASSPATH ./js/serverTwitterSpark.js
```
On y retrouve les commandes enoncées ci-dessus.

Voici la commande pour télécharger et lancer l'image docker :

```sudo docker run -p 7000:7000 -p 8000:8000 rinco/twitter-spark-js:latest```

Elle est disponible ici : https://hub.docker.com/r/rinco/twitter-spark-js

Une fois l'application lancée, le top 10 hashtags les plus cités sont disponibles sur `http://localhost:8000/`.
