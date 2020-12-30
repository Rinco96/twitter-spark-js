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
 * @author robin
 *
 */
public class TwitterSparkStreaming {
	private static final Pattern SPACE = Pattern.compile(" ");
	
	private String popularTopics = "";
	private Map<String,Integer> popularTopicsMap = new HashMap<>();
	
	/**
	 * Cette méthode permet de récupérer les hashtags les plus utilisés sur twitter à par d'un stream.
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

		//
		SparkConf sparkConf = new SparkConf().setAppName("JavaTwitterFeed").setMaster("local[*]");
		JavaStreamingContext ssc = new JavaStreamingContext(sparkConf, new Duration(30000));

		JavaReceiverInputDStream<Status> javaReceiverInputDStream = TwitterUtils.createStream(ssc);
		JavaDStream<Status> frenchTweets = javaReceiverInputDStream.filter((status) -> "en".equalsIgnoreCase(status.getLang()));
		JavaDStream<String> frenchTweetsString = frenchTweets.map((status)-> status.getText().replaceAll("[^\\x00-\\x7F]", "").replace("\n", " "));
		frenchTweetsString.print();
		JavaDStream<String> words = frenchTweetsString.flatMap(x -> Arrays.asList(SPACE.split(x)).iterator());
		JavaDStream<String> hashTags = words.filter(x -> x.startsWith("#") && x.length()>3);
		JavaPairDStream<String, Integer> hashTagsOnes = hashTags.mapToPair(x -> new Tuple2<>(x, 1));
		JavaPairDStream<String, Integer> topCount60 = hashTagsOnes.reduceByKeyAndWindow((a,b) -> a+b, Durations.minutes(5));
		JavaPairDStream<String, Integer> topCount60Sorted = topCount60.mapToPair((s) -> s.swap())
				.transformToPair((rdd) -> rdd.sortByKey(false))
				.mapToPair((s) -> s.swap());
		
		topCount60Sorted.foreachRDD((rdd) -> {
			List<Tuple2<String,Integer>> topList = rdd.take(10);
			System.out.println("----------------------------------------------------");
		    System.out.println(String.format("Popular topics out of %s total topics received:\n", rdd.count()));
		    int i = 1;
		    popularTopics = "";
		    popularTopicsMap.clear();
		    for(Tuple2<String,Integer> elem : topList){
		    	popularTopics += elem._1() + ",";
		    	popularTopicsMap.put(elem._1(), elem._2());
		    	System.out.format("%d - %s : %d", i, elem._1(), elem._2());
		    	i++;
		    	System.out.println("\n");
		    }
		});
				
		ssc.start();
		try {
			ssc.awaitTermination();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @return
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
