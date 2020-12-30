
var JavaSparkContext = Java.type("org.apache.spark.api.java.JavaSparkContext")
var JavaSparkConf = Java.type("org.apache.spark.SparkConf")
var VoidFunction = Java.extend(Java.type("org.apache.spark.api.java.function.VoidFunction"));
var Durations = Java.type("org.apache.spark.streaming.Durations");
var JavaStreamingContext = Java.type("org.apache.spark.streaming.api.java.JavaStreamingContext");
var TwitterUtils = Java.type("org.apache.spark.streaming.twitter.TwitterUtils");

var Level = Java.type("org.apache.log4j.Level");
var Logger = Java.type("org.apache.log4j.Logger");
var Tuple2 = Java.type("scala.Tuple2");
var OAuthAuthorization = Java.type("twitter4j.auth.OAuthAuthorization");
var ConfigurationBuilder = Java.type("twitter4j.conf.ConfigurationBuilder");


var sparkConf = new JavaSparkConf().setAppName("Twitter Top 10 Hastags").setMaster("local[*]");
var jsc = new JavaSparkContext(sparkConf);
var streamingContext = new JavaStreamingContext(jsc, Durations.seconds(1));
Logger.getRootLogger().setLevel(Level.DEBUG);

var conf = new ConfigurationBuilder().setDebugEnabled(true)
    .setOAuthConsumerKey("Z7zYBQ9xpOAwoSG8NaKlv7ja8")
    .setOAuthConsumerSecret("H0LinL4qLkaO4y7cks1QvBRZ7dAhKblpSaU87irI9vAUIMtP6l")
    .setOAuthAccessToken("2590054080-XXNYwO8tVXAXu3Ze7WFoCXANAvNdT8CXEQGQ7gZ")
    .setOAuthAccessTokenSecret("Bk3XWzdsjp8xphMEn9wws7rdI5HU1eJlLj846VLWWt6dJ")
    .build();

var twitterAuth = new OAuthAuthorization(conf);


var inputDStream = TwitterUtils.createStream(streamingContext, twitterAuth);



var statuses = inputDStream.filter(someFunc)
var someFunc = status => status.getText()

var words = statuses.flatMap(someFunc1);
var someFunc1 = (status) => Arrays.asList(SPACE.split(status))

var hashTags = words.filter(someFunc2);
var someFunc2 = (s) => s.startsWith("#") && s.length() > 3


var hashTagsOnes = hashTags.mapToPair(someFunc3);
var someFunc3 = (s) => new Tuple2(s, 1)

var topCount60 = hashTagsOnes.reduceByKeyAndWindow(someFunc4, Durations.minutes(5));
var someFunc4 = (a, b) => a + b


var func = new VoidFunction((rdd) => {
    console.log('inside void function',rdd.count())
});
inputDStream.foreachRDD(func); 


streamingContext.start();
streamingContext.awaitTermination();



