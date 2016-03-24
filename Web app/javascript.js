var heatmap;
var heatmapArray = [];
var heatRadius = 10;
var currentLayer = 0;
var map;

window.onload = function () 
{
    var socket = new WebSocket("ws://localhost:8888");
            
    socket.onopen = function(event)
    {
        console.log("raaaaaadiiiiiiiii");
    };
    socket.onmessage = function(msg)
    {
        addPointsOnMap(msg.data);
    }
    console.log("ovde radi");
}

function initMap()
{
    var mapDiv = document.getElementById("map");
    map = new google.maps.Map(mapDiv, { center: {lat: 40.748817, lng: -73.985428}, zoom: 11});

}

function toggleHeatmap() 
{
//        heatmap.setMap(heatmap.getMap() ? null : map);
    heatmapArray[currentLayer].setMap(heatmapArray[currentLayer].getMap() ? null : map);
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
//        heatmap.set('gradient', heatmap.get('gradient') ? null : gradient);
        heatmapArray[currentLayer].set('gradient', heatmapArray[currentLayer].get('gradient') ? null : gradient);
      }

function changeRadius() 
{
    heatRadius += 5;
    if(heatRadius == 40)
        heatRadius = 10;
//    heatmap.set('radius', heatRadius);
    heatmapArray[currentLayer].set('radius', heatRadius);
}

function changeOpacity() 
{
//    heatmap.set('opacity', heatmap.get('opacity') ? null : 0.2);
    heatmapArray[currentLayer].set('opacity', heatmapArray[currentLayer].get('opacity') ? null : 0.2);
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
    var dataArray = JSON.parse(msg);
//    var heatmapArray = [];

    for(var i=0; i<dataArray.length; ++i)
    {
        var latLngArray = [];
        
        var data = dataArray[i];
        for(var j=0; j<data.length; ++j)
        {
            var lat = data[j].latitude;
            var lon = data[j].longitude;
            
            latLngArray.push(new google.maps.LatLng(lat, lon));
        }
        
        var hmap = new google.maps.visualization.HeatmapLayer({
          data: latLngArray,
          map: null
        });
        
        heatmapArray.push(hmap);
    }
    heatmapArray[currentLayer].setMap(map);
}

function showLayer()
{
    var layeriD = $("#layer-id").val();
    heatmapArray[currentLayer].setMap(null);
    currentLayer = layeriD;
    heatmapArray[currentLayer].setMap(map);
}

























