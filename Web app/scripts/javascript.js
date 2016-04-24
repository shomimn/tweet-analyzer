var CONSTANT =
{
    PLACES: "places",
    DAYS: "days",
    HOURS: "hours",
    MINUTES: "minutes"
};

var REQUEST =
{
    UPDATE_INTERVAL: "changeUpdateInterval",
    TWEET_THRESHOLD: "changeTweetThreshold",
    TAXI_THRESHOLD: "changeTaxiThreshold"
};

var RESPONSE = 
{
    INIT: "initOptions",
    UPDATE: "updateUi"
};

var heatmap;
var heatmapArray = [];
var heatRadius = 10;
var currentLayer = -1;
var map;
var dataTables = {};
var charts = { current: ''};
var indices = {};
var playingAnim = null;
var test = '';
var newHeatmap = true;
var showPois = true;
var socket = null;
var markers = {};
var options = {};
var timeStamps = [];

var dateOptions = 
{
    year: 'numeric', month: 'numeric', day: 'numeric', weekday: 'long',
    hour: 'numeric', minute: 'numeric', second: 'numeric', hour12: false
};

google.charts.load("current", {packages:["corechart", "bar"]});

window.onload = function () 
{
//    $(".progress-bar").width("0%");
//    $(".progress-bar").css("width", "0%");
//    $(".progress-bar")[0].style.width = "100%";
    $("#timeSelect").change(function()
    {
        var timePoint = $(this).val();
        
        charts[charts.current] = null;
        charts.current = timePoint;
        
        drawChart("bar-chart", "charts", "Bar", timePoint,
          { chart: { title: "Tweets per " + timePoint.substr(0, timePoint.length - 1) } });
    });
    
    $("#newHeatmapCheck").on('ifToggled', function()
    {
        newHeatmap = !newHeatmap;
        console.log(newHeatmap);
    });
    
    $("#showPOICheck").on("ifChecked", function()
    {
        showPois = true;
        updateMarkers(currentLayer, map);
    });
    
    $("#showPOICheck").on("ifUnchecked", function()
    {
        showPois = false;
        updateMarkers(currentLayer, null);
    });
    
    socket = new WebSocket("ws://localhost:8888");
            
    socket.onopen = function(event)
    {
        console.log("raaaaaadiiiiiiiii");
    };
    socket.onmessage = function(msg)
    {
        var data = JSON.parse(msg.data);
        
        window[data.response](data.data);
    }
    console.log("ovde radi");
    
//    var data = JSON.parse(test);
//        
//    createDataTables(data);
//    addPointsOnMap(data.tweets);
//        
//    drawChart("pie-chart", "visualization", "PieChart", CONSTANT.PLACES,
//                  { title: "Tweets per neighbourhood", pieHole: 0.4 });
//        
//    drawChart("bar-chart", "charts", "Bar", CONSTANT.MINUTES,
//                  { chart: { title: "Tweets per minute" } });
    

    createDataTables();
    
    drawChart("bar-chart", "charts", "Bar", CONSTANT.MINUTES,                  
                  { chart: { title: "Tweets per minute" } });
    
    drawChart("pie-chart", "visualization", "PieChart", CONSTANT.PLACES,
                  { title: "Tweets per neighbourhood", pieHole: 0.4 });
    
    $("#timeSelect").val(CONSTANT.MINUTES);
    charts.current = CONSTANT.MINUTES;
}

function initOptions(data)
{
    options = data;
    
    animateProgress(options.timeLeft);
    
    $("#updateInterval").val(options.interval);
    $("#tweetThreshold").val(options.tweetThreshold);
    $("#taxiThreshold").val(options.taxiThreshold);
}

function updateUi(data)
{
    animateProgress(options.interval);

    var now = new Date(Date.now());
    var then = null;

    if (timeStamps.length == 0)
    {
        then = new Date(now.getTime() - options.interval);
        timeStamps.push(then);
    }
    else
        then = timeStamps[currentLayer + 1];
        
    if (newHeatmap)
        timeStamps.push(now);
    else
        timeStamps[timeStamps.length - 1] = now;
        
    addPointsOnMap(data.tweets);
    addPOIs(data.twitterPois, "twitter");
    addPOIs(data.taxiPois, "taxi");
    addPOIs(data.taxiTwitterPois, "taxiTwitter");
    addPOIs(data.vehiclesPOIS, "vehicle");
    console.log(data.taxiTwitterPois.length);

    updateCount("total-taxis", data.taxiTotal);
    updateCount("total-vehicles", data.vehicleTotal);
    setDate();

    $.each(data.places, function(key, value)
    {
        updateChart(CONSTANT.PLACES, key, value);
    });

    $.each(data.timePoints.days, function(key, value)
    {
        updateChart(CONSTANT.DAYS, key, value);
    });

    $.each(data.timePoints.hours, function(key, value)
    {
        updateChart(CONSTANT.HOURS, key, value);
    });

    $.each(data.timePoints.mins, function(key, value)
    {
        updateChart(CONSTANT.MINUTES, key, value);
    });
}

function setDate()
{
    var start = timeStamps[currentLayer];
    var end = timeStamps[currentLayer + 1];
    
    $("#fromTo").text(start.toLocaleString("en-GB", dateOptions) + " - " + 
                      end.toLocaleString("en-GB", dateOptions));
}

function animateProgress(millis)
{
    var offset = 2000;
    var progressBar = $(".progress-bar");
//    
    progressBar.stop(true, false);
    progressBar[0].style.width = "0%";
////    progressBar.animate({width: "-100%"}, 1);
    setTimeout(function()
    {
        progressBar.animate({width: "100%"}, millis - offset);
    }, offset);
}


function addPOIs(pois, poiType)
{
//    console.log(pois.length);
    var switchLayers = currentLayer > -1 && currentLayer == heatmapArray.length - 1;
    
    if (currentLayer > 0 && switchLayers)
        updateMarkers(currentLayer - 1, null);
    
    var array = [];
    for (var i = 0; i < pois.length; ++i)
    {
        var infowindow = new google.maps.InfoWindow({
            content: pois[i].name
        });

        var latLng = new google.maps.LatLng(pois[i].latitude, pois[i].longitude);
        var marker = new google.maps.Marker({
            position: latLng,
            map: showPois && switchLayers ? map : null,
            title: pois[i].name,
            icon: "images/" + poiType + "-marker.png"
        });
        
        marker.addListener('click', function() {
            infowindow.setContent(this.getTitle());
            infowindow.open(map, this);
        });
        
        array.push(marker);
    }
    
    markers[poiType] = markers[poiType] || [];
    
    if (newHeatmap)
        markers[poiType].push(array);
    else
    {
        var index = markers[poiType].length - 1;
        markers[poiType][index] = markers[poiType][index].concat(array);
    }
}


function updateMarkers(index, map)
{
    $.each(markers, function(key, array)
    {
        for (var i = 0; i < array[index].length; ++i)
            array[index][i].setMap(map);
    });
}

function changeInterval()
{
    options.interval = parseInt($("#updateInterval").val());
    
    socket.send(JSON.stringify({ request: REQUEST.UPDATE_INTERVAL, data: { interval: options.interval } }));
    animateProgress(options.interval);
}

function changeTweetThreshold()
{
    var threshold = parseInt($("#tweetThreshold").val());
    
    socket.send(JSON.stringify({ request: REQUEST.TWEET_THRESHOLD, data: { threshold: threshold } }));
}

function changeTaxiThreshold()
{
    var threshold = parseInt($("#taxiThreshold").val());
    
    socket.send(JSON.stringify({ request: REQUEST.TAXI_THRESHOLD, data: { threshold: threshold } }));
}

function createDataTables()
{
    dataTables[CONSTANT.PLACES] = [["Places", "Number of tweets"], ["dummy", 0]];
    dataTables[CONSTANT.DAYS] = [["Days", "Number of tweets"]];
    dataTables[CONSTANT.HOURS] = [["Hours", "Number of tweets"]];
    dataTables[CONSTANT.MINUTES] = [["Minutes", "Number of tweets"]];
    
    var orderedDays = { Monday:0, Tuesday:0, Wednesday:0, Thursday:0, Friday:0, Saturday:0, Sunday:0 };
    var orderedHours = {};
    var orderedMinutes = {};
    
    for (var i = 0; i < 24; ++i)
        orderedHours[i] = 0;
    
    for (var i = 0; i < 60; ++i)
        orderedMinutes[i] = 0;
    
    objectToDataTable(orderedDays, CONSTANT.DAYS);
    objectToDataTable(orderedHours, CONSTANT.HOURS);
    objectToDataTable(orderedMinutes, CONSTANT.MINUTES);
    objectToDataTable({}, CONSTANT.PLACES);
    
    dataTables[CONSTANT.PLACES].removeRow(0);
}

function objectToDataTable(obj, table)
{
    indices[table] = {current: 0};
    $.each(obj, function(key, value)
    {
        dataTables[table].push([key, value]);
        indices[table][key] = indices[table].current++;
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

function drawChart(id, lib, type, table, options)
{
    var chart = new google[lib][type](document.getElementById(id));
    chart.draw(dataTables[table], options);
    
    charts[table] = chart;
}

function updateChart(table, columnName, value)
{
    var index = indices[table][columnName];
    if (index === undefined)
    {
        indices[table][columnName] = index = indices[table].current++;
        dataTables[table].addRow([columnName, 0]);
    }
    var oldValue = dataTables[table].getValue(index, 1);
    
    dataTables[table].setValue(index, 1, oldValue + value);
    
    if (charts[table])
        charts[table].draw(dataTables[table]);
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

 function changeGradient() 
{
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
    updateCount("total-tweets", dataArray.length);
    
//    var index = currentLayer == heatmapArray.length - 1 ?
//        currentLayer : heatmapArray.length - 1;
    
    var index = currentLayer == -1 ? -1 : heatmapArray.length - 1;
    var latLngArray = new google.maps.MVCArray();

    for(var i=0; i<dataArray.length; ++i)
    {        
        var data = dataArray[i];
        if (currentLayer > -1 && !newHeatmap)
            heatmapArray[index].data.push(new google.maps.LatLng(data.latitude, data.longitude));
        else
            latLngArray.push(new google.maps.LatLng(data.latitude, data.longitude));        
    }
    
    if (currentLayer == - 1 || newHeatmap)
    {
        var hmap = new google.maps.visualization.HeatmapLayer({
              data: latLngArray,
              map: null
        });

        heatmapArray.push(hmap);

        if (index == currentLayer)
        {
            if (heatmapArray[currentLayer])
                heatmapArray[currentLayer].setMap(null);
            
            heatmapArray[++currentLayer].setMap(map);
            $("#layer-id").val(currentLayer);
        }
    }
}

function updateCount(id, count)
{
    var el = $("#" + id);
    var total = parseInt(el.text());
    
    el.text(total + count);
}

function showLayer()
{
    var layeriD = parseInt($("#layer-id").val());
    if(layeriD < 0 || layeriD > heatmapArray.length - 1)
    {
        $("#layer-id").val(currentLayer);
        return;
    }
    updateMarkers(currentLayer, null);
    heatmapArray[currentLayer].setMap(null);
    currentLayer = layeriD;
    heatmapArray[currentLayer].setMap(map);
    
    if (showPois)
        updateMarkers(currentLayer, map);
    
    setDate();
}

function showPrevious()
{
    if(currentLayer == 0)
        return;
    var layeriD = $("#layer-id").val();
    updateMarkers(currentLayer, null);
    heatmapArray[currentLayer].setMap(null);
    currentLayer = parseInt(layeriD)-1;
    heatmapArray[currentLayer].setMap(map);
    
    if (showPois)
        updateMarkers(currentLayer, map);
    
    $("#layer-id").val(currentLayer);
    
    setDate();
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
    updateMarkers(currentLayer, null);
    heatmapArray[currentLayer].setMap(null);
    currentLayer = parseInt(layeriD)+1;
    heatmapArray[currentLayer].setMap(map);
    
    if (showPois)
        updateMarkers(currentLayer, map);
    
    $("#layer-id").val(currentLayer);
    
    setDate();
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