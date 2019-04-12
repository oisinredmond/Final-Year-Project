const functions = require('firebase-functions');
const admin = require('firebase-admin');
const fetch = require('node-fetch');
const async = require('async');
/*
admin.initializeApp({
  credential: admin.credential.cert({
    projectId: 'fyp-locations',
    clientEmail: 'firebase-adminsdk-opf5z@fyp-locations.iam.gserviceaccount.com',
    privateKey: '-----BEGIN PRIVATE KEY-----\nMIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC10hy5RXHZBP6B\n7g3+Cpr27fcIP3QF6ouexkL2CnV6OJt1CSEQzt9McdiRlNAHYCE8RlGUR8Hms13S\nRCIlyOMeb7K9OHVFuZtJ5cQIVOLC0w9AQoCavmxhbFwt+Mi4mEJw6aIFOFxoSX+B\nZIWC2bfW5adeJ6QACJiki6zOZNCj0RFFC+8HYt3xcQjDCl0f7mDU66Nq9LVaQEnu\nzmQyaifhpnKBm7bW9aiRRE9v8b+KAd+KyEvuWTEpV6p161o6+QFUbjFQu06FWsH+\n+CrYa4VbgdIY5NLxz8iqFFq81YUImEGF0lu53+yGEVAoo1XlhEe5fko3HyHgnjhe\nLu+bu5rrAgMBAAECggEAB4bYK49FZD+Bra1GJQMZYSl+tXey/N+1we/hK8BJFrMr\nNFAZb1GxCsAXwAcSCYwgNBeRHvtNephoROEPLNNBG6xyEMU6jZVeaVO6psGk1X3Z\nPsbB/iXt5KaQtlJ7NLEQ5w206FRp9uM8BQY9RKpwotPE9u/bKSkx8HXbxHuF65IV\nHnf5OND7FKpkossus/9jYa0KZZt98ikJq+SlH/pa/dWJ4rYl8yF9J6zeGEsWJa0w\nMoIAq52xZ8YA7ueOFrE3WI64tsib6H11Sd2xmo3iMIyYGjqRwIKFSreuGBen68ia\nbGdCz1nKPfPtG+eTsHf5QEhfA+JcSzfVKU5wZXwCqQKBgQDsJPpDbCWd8la/WtKF\nEOgEaj2nIXhMER7DzlBinagL6+cPy1qyyvhPhXHm6rH/YzCDZFwMJGCjO4BXM0nQ\nTuLPu6F3hxstJEKpFVe/sW9Uc+nYSSQH57xKDtTMTrflnzzLMxG7P7+UFC2q6Ets\n6/Aao4LUeD+aWjyyRUb4AlLefQKBgQDFG9Bm7opaKu+DU6opeYyZpo8HRB4v8oJy\nsfdaIHX2k0eTADq5wkCxusOdiTHusZcmxU6aaTeVz7OXHTfbDISXhw226LvmxaXY\n//3irwHIT/oMmyGDsgMYM+0aY2HYFKqccbuC4m6z/7J6VVIxQDE36cNJj5Anye5e\nQ5hCt6kThwKBgHhk/edECwW1GT8kSfKnxPMDS32bVd7KV7oSxO8SXCv/0OU/k3VW\ngDTi30iQ3cFMS96hW0Chh1eRrYRH1NlMbtzkV5U+H7/rHcVg6UUFbLJNCqtH1wPk\nxR2o5BtqAt4iSnd7CQ5DmSQhCYt6NYJJ5DdgqAI1cI/8c7ecFGu7m45tAoGADXGm\nNz8R5LCFAZoVQxrtQhWgL8ivnmmWCCinTFhmc1j8SsR7POxI6VZpvTb8VuF6A8T1\nZYbDqc4u26G8i45BBOzLPKyHFGhqtXBQasOxBW6fCXkTQh5EI8R26ZPZupk+Qclo\nFrdHAbPxAnMLVhygvsWM8ll+ZZhaT8egfPx15cUCgYBZUz+/gkfX0M8vZBnXd3vm\nlyZ4jrsgwnZDl4aehP9DBMeoJlHRrpl5xkFIhg4YdR7uAEsblTkjm5yN1xWtFvgu\n9Cy2zGfg6LnCAB/o3chzxrUpLGf8xznodimvl8owGZfU4kt5AzqOJqVBrR4l/bNi\nCbG/kNCyNEHLl5SgKhpbtA==\n-----END PRIVATE KEY-----\n'
  }),
  databaseURL: 'https://fyp-locations.firebaseio.com/'
});*/

admin.initializeApp(functions.config().firebase);

const baseUrl = "http://api.worldweatheronline.com/premium/v1/marine.ashx?";
const params = "key=3bcdb7b319c04358a76141054190304&format=json&tide=yes&q=";
var dbRef = admin.database().ref('locations');

const requestWait = async (name, url) => {
  const response = await fetch(url);
  const json = await response.json();
  dbRef.child(name+'/weather').set(json.data.weather);
}

exports.updateForecasts = functions.https.onRequest((req, res) => {
  dbRef.once('value').then(function(snap){
    snap.forEach(snap2=>{
      let name = snap2.key;
      let childRef = dbRef.child(name);
      let coordRef = snap2.child('coordinates');
      let lat = coordRef.child('1').val();
      let lon = coordRef.child('0').val();
      let url = baseUrl + params + lat + ',' + lon;
      requestWait(name, url);
    });
    res.status(200).end();
  });
});
