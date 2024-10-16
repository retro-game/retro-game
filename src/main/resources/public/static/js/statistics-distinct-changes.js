'use strict';

Chart.defaults.global.defaultFontColor = $('#points-changes').parent().css('color');

var kind = $('[data-statistics-kind]').attr('data-statistics-kind');
var dataColor;
switch (kind) {
  case 'BUILDINGS':
    dataColor = '#8e8';
    break;
  case 'TECHNOLOGIES':
    dataColor = '#ee8';
    break;
  case 'FLEET':
    dataColor = '#e88';
    break;
  case 'DEFENSE':
    dataColor = '#8ee';
    break;
  default:
    dataColor = '#ccc';
}

var changes = $('[data-changes]').attr('data-changes').split(';');
var labels = [];
var points = [];
var ranks = [];
for (var i = 0; i < changes.length; i++) {
  var data = changes[i].split(',');
  var seconds = 0 | data[0];
  labels.push(new Date(seconds * 1000));
  points.push(+data[1]);
  ranks.push(+data[2]);
}

function showChart(ctxId, data, reverseY) {
  var canvas = document.getElementById(ctxId);
  var label = $(canvas).parent().parent().prev().text().trim();
  var ctx = canvas.getContext('2d');
  new Chart(ctx, {
    type: 'line',
    data: {
      labels: labels,
      datasets: [{
        label: label,
        data: data,
        borderColor: dataColor,
        backgroundColor: dataColor,
        fill: false,
        lineTension: 0
      }]
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
          ticks: {
            callback: prettyNumber,
            reverse: reverseY
          }
        }]
      },
      tooltips: {
        callbacks: {
          label: function (tooltipItem, data) {
            return prettyNumber(data.datasets[tooltipItem.datasetIndex].data[tooltipItem.index]);
          }
        }
      }
    }
  });
}

showChart('points-changes', points, false);
showChart('rank-changes', ranks, true);
