'use strict';

Chart.defaults.global.defaultFontColor = $('#distribution-changes').parent().css('color');

var changes = $('[data-changes]').attr('data-changes').split(';');
var labels = [];
var buildings = [];
var technologies = [];
var fleet = [];
var defense = [];
for (var i = 0; i < changes.length; i++) {
  var data = changes[i].split(',');
  var seconds = 0 | data[0];
  labels.push(new Date(seconds * 1000));

  var b = +data[1];
  var t = +data[2];
  var f = +data[3];
  var d = +data[4];
  var total = b + t + f + d;
  buildings.push(b / total * 100);
  technologies.push(t / total * 100);
  fleet.push(f / total * 100);
  defense.push(d / total * 100);
}

function makeDataset(label, color, data) {
  return {
    label: label,
    data: data,
    borderColor: color,
    backgroundColor: color,
    lineTension: 0
  };
}

var canvas = document.getElementById('distribution-changes');
var ctx = canvas.getContext('2d');
new Chart(ctx, {
  type: 'line',
  data: {
    labels: labels,
    datasets: [
      makeDataset('Buildings', '#8e8', buildings),
      makeDataset('Technologies', '#ee8', technologies),
      makeDataset('Fleet', '#e88', fleet),
      makeDataset('Defense', '#8ee', defense)
    ]
  },
  options: {
    responsive: true,
    scales: {
      xAxes: [{
        type: 'time',
        time: {
          tooltipFormat: 'll HH:mm'
        }
      }],
      yAxes: [{
        stacked: true,
        ticks: {
          callback: function (value) {
            return prettyNumber(value) + '%';
          },
          max: 100
        }
      }]
    },
    tooltips: {
      callbacks: {
        label: function (tooltipItem, data) {
          return prettyNumber(data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index]) + '%';
        }
      },
      mode: 'index'
    }
  }
});
