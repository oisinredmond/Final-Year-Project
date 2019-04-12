const functions = require('firebase-functions');
const admin = require('firebase-admin');
const fetch = require('node-fetch');
admin.initializeApp(functions.config().firebase);
const baseUrl = "http://api.worldweatheronline.com/premium/v1/marine.ashx?";
const params = "key=3bcdb7b319c04358a76141054190304&format=json&tide=yes&q=";

exports.updateForecasts = functions.https.onRequest((req, res) => {
  var url = "";
  const dbRef = admin.database().ref('/locations');
  dbRef.once('value').then(snap=>{
    snap.forEach(snap2=>{
      var childRef = dbRef.child(snap2.key);
      var coordRef = snap2.child('coordinates');
      var lat = coordRef.child('1').val();
      var lon = coordRef.child('0').val();
      url = baseUrl + params + lat + ',' + lon;

      fetch(url)
      .then(resp=>{return resp.json()})
      .catch(e=>{
        console.log(e);
      })
      .then(json=>{
        childRef.child('weather').set(json.data.weather);
        return null;
      })
      .catch(e=>{
        console.log(e);
      });
    });
    return res.redirect(200);
  })
  .catch(error=>{
    console.log(error);
  });
});
