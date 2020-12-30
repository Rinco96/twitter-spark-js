<h1 align="center">
    Projet Environnement Big Data
</h1>

<p align="center">
  <strong>Réalisé par Iman, Robin & Sabri</strong><br>
</p>

*******************

## 📋 Liste des tâches réalisées

* Réalisation d'une application full Javascript permettant d'afficher le Top 10 des Hashtags provenant d'un stream de tweets en utilsant Twitter API et Apache Spark Streaming
* Réalisation de la même application en combinant Javascript et Java pour pallier aux problèmes de multithreading (voir partie Difficultés)
* Sans docker
* Avec docker

## Introduction

Apache Spark est un moteur de traitement de données rapide dédié qui permet d’effectuer un traitement de larges volumes de données de manière distribuée. Très en vogue depuis maintenant quelques années, il est de plus en plus accessible grâce au support multi-langage qu’il propose. En effet, le framework supporte différents langages de programmation tels que Java, Scala, Python et R. 

Cependant, parmi ces langages on remarque l’absence d’un langage tout aussi populaire et qui n’est pourtant pas supporté : le Javascript. 

Il existe certains projets open-source intéressants tel que EclairJS qui ont essayé de fournir un environnement capable d’utiliser Spark au sein d’un serveur NodeJS mais ce dernier n’est malheureusement plus maintenu depuis quelques années. 

Nous vous montrerons aujourd’hui une solution afin d’utiliser Spark dans un environnement NodeJs. 

Pour ce faire, nous créerons une application permettant de traiter et afficher en temps réel les hashtags les plus utilisés en utilisant l’API twitter et l’extension Apache Spark Streaming. 

## GraalVM

## Implémentation Full JS

## 🚀 Implémentation JS/Java

1. Filtre pour récupérer uniquement les tweets en anglais
2. Récupérer les hashtags
