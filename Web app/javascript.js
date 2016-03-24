var heatmap;
var heatRadius = 10;

window.onload = function () 
{
    var socket = new WebSocket("ws://192.168.58.1:8888");
            
    socket.onopen = function(event)
    {
        console.log("raaaaaadiiiiiiiii");
    };
    socket.onmessage = function(msg)
    {
        heatmap = new google.maps.visualization.HeatmapLayer({
          data: addPointsOnMap(msg.data),
          map: map
        });
    }
    console.log("ovde radi");
}

function initMap()
{
    var mapDiv = document.getElementById("map");
    var map = new google.maps.Map(mapDiv, { center: {lat: 40.748817, lng: -73.985428}, zoom: 11});
    


}


function toggleHeatmap() 
{
        heatmap.setMap(heatmap.getMap() ? null : map);
}

 function changeGradient() {
        var gradient = [
          'rgba(0, 255, 255, 0)',
          'rgba(0, 255, 255, 1)',
          'rgba(0, 191, 255, 1)',
          'rgba(0, 127, 255, 1)',
          'rgba(0, 63, 255, 1)',
          'rgba(0, 0, 255, 1)',
          'rgba(0, 0, 223, 1)',
          'rgba(0, 0, 191, 1)',
          'rgba(0, 0, 159, 1)',
          'rgba(0, 0, 127, 1)',
          'rgba(63, 0, 91, 1)',
          'rgba(127, 0, 63, 1)',
          'rgba(191, 0, 31, 1)',
          'rgba(255, 0, 0, 1)'
        ]
        heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
      }

function changeRadius() 
{
    heatRadius += 5;
    if(heatRadius == 40)
        heatRadius = 10;
    heatmap.set('radius', heatRadius);
}

function changeOpacity() 
{
    heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
}

function getPoints() 
{
        return [
            new google.maps.LatLng(40.748817, -73.985428),
            new google.maps.LatLng(40.59524418, -73.9551571)
        ];
}

function addPointsOnMap(msg)
{
    var data = JSON.parse(msg);

    var latLngArray = [];
    for(var i=0; i<data.length; ++i)
    {
        var latlng = [];
        var lat = data[i].latitude;
        var lon = data[i].longitude;
        
        latLngArray.push(new google.maps.LatLng(lat, lon));
    }
    return latLngArray;
}
