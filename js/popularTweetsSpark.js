

module.exports = app => {

    var TwitterSparkStreaming = Java.type("com.ece.demo.spark.streaming.twitter.TwitterSparkStreaming");

    var tss = new TwitterSparkStreaming();

    const {
        Worker,
    } = require('worker_threads');
    let w = new Worker(`
        const { parentPort } = require('worker_threads');
        parentPort.on('message', (m) => {
            m.streaming();
            parentPort.postMessage(m);
          });
    `, { eval: true });

    w.postMessage(tss);
    w.on('message', (m) => {
        console.log('Worker' + m);
    });

    var popularTopicsMap = [];
    setInterval(() => {
        popularTopicsMap = [];
        tss.getPopularTopicsMap().forEach((k, v) => {
            popularTopicsMap.push(k + " : " + v + " tweet(s)");
        });
    }, 15000);



    app.get('/getPopularHashtags', function (req, res) {
        res.json(popularTopicsMap)
    });
}
