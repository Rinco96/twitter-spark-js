const express = require('express');
const bodyParser = require('body-parser');
var cors = require('cors');
const app = express();
const app2 = express();


app.use(bodyParser.json());
app.use(bodyParser.urlencoded({
    extended: true
}));
app.use(cors()); 


app.get('/', (req, res) => {
    res.send("Bienvenue sur le serveur de Twitter Popular Hashtags");
});

app.listen(7000, () => console.log(`Listening on port 7000`));


require('./popularTweetsSpark')(app);
module.exports = app;




app2.get('/', function(req, res){
    res.sendFile(__dirname + '/twitterSpark.html');
    });

app2.listen(8000, () => console.log(`Listening on port 8000`));