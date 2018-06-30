'use strict';

var productionSpeed = +$(document.body).attr('data-production-speed');

var input = $('#details-table');
var kind = input.attr('data-kind');
var currentLevel = +input.attr('data-level');
var temperature = +input.attr('data-temperature');
var energyTechnologyLevel = +input.attr('data-energy-technology-level');

function msg(name) {
  return input.attr('data-msg-' + name);
}

var generate = true;
var headers = [msg('level')];
var body = [
  function (level) {
    return level;
  }
];

var prodFunc;
var usageFunc;
var capacityFunc;
var additionalFieldsFunc;
if (kind === 'METAL_MINE' || kind === 'CRYSTAL_MINE' || kind === 'DEUTERIUM_SYNTHESIZER') {
  headers = headers.concat([msg('production'), msg('production-diff'), msg('energy'), msg('energy-diff')]);
  var usage;
  if (kind === 'METAL_MINE') {
    prodFunc = function (level) {
      var base = +$(document.body).attr('data-metal-mine-base-production');
      return Math.floor(base * level * Math.pow(1.1, level)) * productionSpeed;
    };
    usage = +$(document.body).attr('data-metal-mine-base-energy-usage');
  } else if (kind === 'CRYSTAL_MINE') {
    prodFunc = function (level) {
      var base = +$(document.body).attr('data-crystal-mine-base-production');
      return Math.floor(base * level * Math.pow(1.1, level)) * productionSpeed;
    };
    usage = +$(document.body).attr('data-crystal-mine-base-energy-usage');
  } else {
    prodFunc = function (level) {
      var base = +$(document.body).attr('data-deuterium-synthesizer-base-production');
      return Math.floor(base * level * Math.pow(1.1, level) * (1.28 - 0.002 * temperature)) * productionSpeed;
    };
    usage = +$(document.body).attr('data-deuterium-synthesizer-base-energy-usage');
  }
  body.push(prodFunc);
  body.push(function (level) {
    return prodFunc(level) - prodFunc(currentLevel);
  });
  usageFunc = function (level) {
    return -Math.ceil(usage * level * Math.pow(1.1, level));
  };
  body.push(usageFunc);
  body.push(function (level) {
    return usageFunc(level) - usageFunc(currentLevel);
  });
} else if (kind === 'SOLAR_PLANT') {
  headers = headers.concat([msg('energy'), msg('energy-diff')]);
  prodFunc = function (level) {
    var base = +$(document.body).attr('data-solar-plant-base-energy-production');
    return Math.floor(base * level * Math.pow(1.1, level));
  };
  body.push(prodFunc);
  body.push(function (level) {
    return prodFunc(level) - prodFunc(currentLevel);
  });
} else if (kind === 'FUSION_REACTOR') {
  headers = headers.concat([msg('energy'), msg('energy-diff'), msg('deuterium'), msg('deuterium-diff')]);
  prodFunc = function (level) {
    var base = +$(document.body).attr('data-fusion-reactor-base-energy-production');
    return Math.floor(base * level * Math.pow(1.05 + 0.01 * energyTechnologyLevel, level));
  };
  body.push(prodFunc);
  body.push(function (level) {
    return prodFunc(level) - prodFunc(currentLevel);
  });
  usageFunc = function (level) {
    var base = +$(document.body).attr('data-fusion-reactor-base-deuterium-usage');
    return -Math.ceil(base * level * Math.pow(1.1, level)) * productionSpeed;
  };
  body.push(usageFunc);
  body.push(function (level) {
    return usageFunc(level) - usageFunc(currentLevel);
  });
} else if (kind === 'METAL_STORAGE' || kind === 'CRYSTAL_STORAGE' || kind === 'DEUTERIUM_TANK') {
  headers = headers.concat([msg('capacity'), msg('capacity-diff')]);
  capacityFunc = function (level) {
    return Math.ceil(1 + Math.pow(1.6, level)) * 50000;
  };
  body.push(capacityFunc);
  body.push(function (level) {
    return capacityFunc(level) - capacityFunc(currentLevel);
  });
} else if (kind === 'TERRAFORMER' || kind === 'LUNAR_BASE') {
  headers = headers.concat([msg('additional-fields'), msg('additional-fields-diff')]);
  var perLevel;
  if (kind === 'TERRAFORMER') {
    perLevel = +$(document.body).attr('data-fields-per-terraformer-level');
  } else {
    perLevel = +$(document.body).attr('data-fields-per-lunar-base-level');
  }
  additionalFieldsFunc = function (level) {
    return perLevel * level;
  };
  body.push(additionalFieldsFunc);
  body.push(function (level) {
    return additionalFieldsFunc(level) - additionalFieldsFunc(currentLevel);
  });
} else if (kind === 'SENSOR_PHALANX') {
  headers = headers.concat([msg('range'), msg('range-diff')]);
  var rangeFunc = function (level) {
    return level * level - 1;
  };
  body.push(rangeFunc);
  body.push(function (level) {
    return rangeFunc(level) - rangeFunc(currentLevel);
  });
} else if (kind === 'MISSILE_SILO') {
  headers = headers.concat([msg('capacity'), msg('capacity-diff')]);
  capacityFunc = function (level) {
    return 10 * level;
  };
  body.push(capacityFunc);
  body.push(function (level) {
    return capacityFunc(level) - capacityFunc(currentLevel);
  });
} else if (kind === 'ASTROPHYSICS') {
  headers.push(msg('max-planets'));
  body.push(function (level) {
    return 1 + Math.floor((level + 1) / 2);
  });
} else {
  generate = false;
}

if (generate) {
  var levelFrom = Math.max(currentLevel - 2, 1);
  var levelTo = levelFrom + 10;
  var html = '<tr><th colspan="2">' + msg('table') + '</th></tr><tr><td colspan="2">';
  html += '<label>' + msg('level-from') + ': <input id="level-from" type="number" value="' + levelFrom + '" min="1" required></label>';
  html += '<label>' + msg('level-to') + ': <input id="level-to" type="number" value="' + levelTo + '" min="1" required></label>';
  html += '<table id="generated-table"></table>';
  html += '</td></tr>';
  $('#details').append(html);

  var generateTable = function () {
    var i;
    var html = '<tr>';
    for (i = 0; i < headers.length; i++) {
      html += '<th>' + headers[i] + '</th>';
    }
    var levelFrom = +$('#level-from').val();
    var levelTo = +$('#level-to').val();
    for (var level = levelFrom; level <= levelTo; level++) {
      html += '<tr>';
      for (i = 0; i < body.length; i++) {
        var value = prettyNumber(body[i](level));
        html += '<td>' + value + '</td>';
      }
      html += '</tr>';
    }
    $('#generated-table').html(html);
  };

  $('#level-from').change(generateTable);
  $('#level-to').change(generateTable);

  generateTable();
}
