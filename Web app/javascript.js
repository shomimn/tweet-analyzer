var heatmap;
var heatmapArray = [];
var heatRadius = 10;
var currentLayer = 0;
var map;
var dataTables = {};
var playingAnim = null;
var test = '';

google.charts.load("current", {packages:["corechart", "bar"]});

window.onload = function () 
{
    $("#timeSelect").change(function()
    {
        var timePoint = $(this).val();
        
        drawChart("bar-chart", "charts", "Bar", dataTables[timePoint],
          { chart: { title: "Tweets per " + timePoint.substr(0, timePoint.length - 1) } });
    });
    
    var socket = new WebSocket("ws://localhost:8888");
            
    socket.onopen = function(event)
    {
        console.log("raaaaaadiiiiiiiii");
    };
    socket.onmessage = function(msg)
    {
        var data = JSON.parse(msg.data);
        
        createDataTables(data);
        addPointsOnMap(data.timeUnits);
        
        drawChart("pie-chart", "visualization", "PieChart", dataTables["places"],
                  { title: "Tweets per neighbourhood", pieHole: 0.4 });
        
        drawChart("bar-chart", "charts", "Bar", dataTables["minutes"],
                  { chart: { title: "Tweets per minute" } });
    
        $("#timeSelect").val("minutes");
    }
    console.log("ovde radi");
    
//    var data = JSON.parse(test);
//        
//    createDataTables(data);
//    addPointsOnMap(data.timeUnits);
//        
//    drawChart("pie-chart", "visualization", "PieChart", dataTables["places"],
//                  { title: "Tweets per neighbourhood", pieHole: 0.4 });
//        
//    drawChart("bar-chart", "charts", "Bar", dataTables["minutes"],
//                  { chart: { title: "Tweets per minute" } });
//    
//    $("#timeSelect").val("minutes");
}

function createDataTables(data)
{
    dataTables["places"] = [["Places", "Number of tweets"]];
    dataTables["days"] = [["Days", "Number of tweets"]];
    dataTables["hours"] = [["Hours", "Number of tweets"]];
    dataTables["minutes"] = [["Minutes", "Number of tweets"]];
    
    var orderedDays = sortDays(data.timePoints.days);
    var orderedHours = sortObject(data.timePoints.hours);
    var orderedMinutes = sortObject(data.timePoints.mins);
    
    objectToDataTable(data.places, "places");
    objectToDataTable(orderedDays, "days");
    objectToDataTable(orderedHours, "hours");
    objectToDataTable(orderedMinutes, "minutes");
}

function objectToDataTable(obj, table)
{
    $.each(obj, function(key, value)
    {
        dataTables[table].push([key, value]);
    });
    
    dataTables[table] = google.visualization.arrayToDataTable(dataTables[table]);
}

function sortObject(obj)
{
    var orderedObj = {};
    Object.keys(obj).sort().forEach(function (key)
    {
        orderedObj[key] = obj[key];
    });
    
    return orderedObj;
}

function sortDays(obj)
{
    var orderedDays = { Monday:0, Tuesday:0, Wednesday:0, Thursday:0, Friday:0, Saturday:0, Sunday:0 };
    Object.keys(obj).forEach(function (key)
    {
        orderedDays[key] = obj[key];
    });
    
    return orderedDays;
}

function drawChart(id, lib, type, data, options)
{
    var chart = new google[lib][type](document.getElementById(id));
    chart.draw(data, options);
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
function addPointsOnMap(dataArray)
{
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
    if(parseInt(layeriD) < 0 || parseInt(layeriD) >= heatmapArray.length-1)
    {
        $("#layer-id").val(currentLayer);
        return;
    }
    heatmapArray[currentLayer].setMap(null);
    currentLayer = layeriD;
    heatmapArray[currentLayer].setMap(map);
}
function showPrevious()
{
    if(currentLayer == 0)
        return;
    var layeriD = $("#layer-id").val();
    heatmapArray[currentLayer].setMap(null);
    currentLayer = parseInt(layeriD)-1;
    heatmapArray[currentLayer].setMap(map);
    $("#layer-id").val(currentLayer);
}
function showNext()
{
    if(currentLayer == heatmapArray.length-1)
    {
        clearInterval(playingAnim);
        $(".timestamp-animation").attr("src", "play.png");
        return;
    }
    var layeriD = $("#layer-id").val();
    heatmapArray[currentLayer].setMap(null);
    currentLayer = parseInt(layeriD)+1;
    heatmapArray[currentLayer].setMap(map);
    $("#layer-id").val(currentLayer);
}
function handle(e)
{
    if(e.keyCode === 13)
        showLayer();
}
function playAnimation()
{
    if(playingAnim)
    {
        clearInterval(playingAnim);
        $(".timestamp-animation").attr("src", "play.png");
        playingAnim = null;
        return;
    }
    else if(currentLayer != heatmapArray.length-1 && heatmapArray.length != 0)
    {
        $(".timestamp-animation").attr("src", "stop.png");
        playingAnim = setInterval(showNext, 1000);
    }
}