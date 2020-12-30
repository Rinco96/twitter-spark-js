package com.ece.demo.spark.streaming.twitter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import static java.util.Collections.reverseOrder;

import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.twitter.TwitterUtils;

import scala.Tuple2;
import twitter4j.Status;

/**
 * Cette classe permet de récupérer les 10 hashtags les plus utilisés à partir d'un stream de tweets.
 * 
 * @author robin
 *
 */
public class TwitterSparkStreaming {
	private static final Pattern SPACE = Pattern.compile(" ");
	
	private String popularTopics = "";
	private Map<String,Integer> popularTopicsMap = new HashMap<>();
	
	/**
	 * Cette méthode permet de récupérer les hashtags les plus utilisés sur twitter à partir d'un stream.
	 * Celui-ci est établi grâce à la librarie Twitter4J.
	 * 
	 * @throws InterruptedException
	 */
	public void streaming() throws InterruptedException  {
		
		// Initialisation des propriétés nécessaires à Twitter4J pour initialiser un stream
		System.setProperty("twitter4j.oauth.consumerKey", "Z7zYBQ9xpOAwoSG8NaKlv7ja8");
		System.setProperty("twitter4j.oauth.consumerSecret", "H0LinL4qLkaO4y7cks1QvBRZ7dAhKblpSaU87irI9vAUIMtP6l");
		System.setProperty("twitter4j.oauth.accessToken", "2590054080-XXNYwO8tVXAXu3Ze7WFoCXANAvNdT8CXEQGQ7gZ");
		System.setProperty("twitter4j.oauth.accessTokenSecret", "Bk3XWzdsjp8xphMEn9wws7rdI5HU1eJlLj846VLWWt6dJ");

		// création d'une configuration pour notre application Spark
		// setMaster("local[*]") permet de lancer le Spark localement en utilsant tous les coeurs disponibles
		SparkConf sparkConf = new SparkConf().setAppName("JavaTwitterFeed").setMaster("local[*]");
		
		// création d'un point d'entré vers les fonctionnalités de streaming de Spark
		JavaStreamingContext jsc = new JavaStreamingContext(sparkConf, new Duration(30000));

		// création d'un stream grâce à la classe TwitterUtils
		JavaReceiverInputDStream<Status> javaReceiverInputDStream = TwitterUtils.createStream(jsc);
		
		// application de différentes transformations sur le stream
		// récupérer des tweets anglais uniquement et transformation en string
		JavaDStream<Status> englishTweets = javaReceiverInputDStream.filter((status) -> "en".equalsIgnoreCase(status.getLang()));
		JavaDStream<String> englishTweetsString = englishTweets.map((status)-> status.getText().replaceAll("[^\\x00-\\x7F]", "").replace("\n", " "));

		// split pour récupérer les mots
		JavaDStream<String> words = englishTweetsString.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());
		//filtre pour récupérer les hashtags uniquement
		JavaDStream<String> hashTags = words.filter(w -> w.startsWith("#") && w.length()>3);
		
		// application du MapReduce pour compter le nombre d'occurrences de chaque hashtag
		JavaPairDStream<String, Integer> hashTagsMap = hashTags.mapToPair(x -> new Tuple2<>(x, 1));
		JavaPairDStream<String, Integer> hashTagsReduce = hashTagsMap.reduceByKeyAndWindow((a,b) -> a+b, Durations.minutes(5));
		// tri décroissant sur le nombre d'occurrences
		JavaPairDStream<String, Integer> hashTagsSorted = hashTagsReduce.mapToPair((h) -> h.swap())
				.transformToPair((rdd) -> rdd.sortByKey(false))
				.mapToPair((h) -> h.swap());
		
		// récupération des 10 hashtags les plus cités
		hashTagsSorted.foreachRDD((rdd) -> {
			List<Tuple2<String,Integer>> topList = rdd.take(10);
			System.out.println("----------------------------------------------------");
		    System.out.println(String.format("Popular topics out of %s total topics received:\n", rdd.count()));
		    int i = 1;
		    popularTopics = "";
		    popularTopicsMap.clear();
		    for(Tuple2<String,Integer> elem : topList){
		    	popularTopics += elem._1() + ",";
		    	// stockage de ces hashtags dans une map qui sera ensuite récupérées dans le code JS
		    	popularTopicsMap.put(elem._1(), elem._2());
		    	System.out.format("%d - %s : %d", i, elem._1(), elem._2());
		    	i++;
		    	System.out.println("\n");
		    }
		});
				
		jsc.start();
		try {
			jsc.awaitTermination();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return la Map contenant les 10 hashtags les plus utilisés
	 */
	public Map<String,Integer> getPopularTopicsMap() {
        List<Entry<String, Integer>> list = new ArrayList<>(popularTopicsMap.entrySet());
        list.sort(reverseOrder(Entry.comparingByValue()));

        Map<String, Integer> result = new LinkedHashMap<>();
        for (Entry<String, Integer> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
		return result;
	}

}
