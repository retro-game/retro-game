'use strict';

$('#all-units').click(function () {
  $('[data-set-for^="unit_"][data-set-value!=""][data-set-value!="0"]').click();
  return false;
});

$('#no-units').click(function () {
  $('[id^="unit_"]').val('').trigger('change');
  return false;
});

$('[data-set-max-resource]').click(function () {
  var resourceName = $(this).attr('data-set-max-resource');
  var max = Math.floor(getResources()[resourceName]);
  var capacity = +$('#capacity').attr('data-total-capacity');
  var input = $('#' + resourceName);
  capacity = Math.max(0, capacity + (+input.val()));
  input.val(Math.max(0, Math.min(capacity, max)));
  input.trigger('change');
  return false;
});

$('#all-resources').click(function () {
  $('[data-set-max-resource]').each(function () {
    $(this).click();
  });
  return false;
});

$('#no-resources').click(function () {
  var names = ['metal', 'crystal', 'deuterium'];
  for (var i = 0; i < names.length; i++) {
    var name = names[i];
    $('#' + name).val('').trigger('change');
  }
  return false;
});

$('[name="party"]').click(function () {
  $('#attack').prop('checked', true);
  var form = $('#send-fleet');
  var party = $(this);
  var names = ['galaxy', 'system', 'position', 'kind'];
  for (var i = 0; i < names.length; i++) {
    var name = names[i];
    $('[name="' + name + '"]', form).prop('readonly', true).val(party.attr('data-' + name));
  }
  party.trigger('change');
});

var holdTime = 1;

function update() {
  var val = $('[name="holdTime"]').val();
  if (val !== undefined) {
    holdTime = 0 | val;
  }

  var mission = $('[name="mission"]:checked').attr('id');
  var holdTimeElem = $('#hold-time');
  if (mission === 'hold') {
    if (holdTimeElem.children().length === 0) {
      var msg = holdTimeElem.attr('data-hold-time-message');
      holdTimeElem.html('<td>' + msg + '</td><td><input name="holdTime" type="number" min="0" max="100" value="' + holdTime + '"></td>');
    }
  } else {
    holdTimeElem.html('');
  }

  var start = $('#top-bar-body-list option:selected');
  var startGalaxy = 0 | start.attr('data-galaxy');
  var startSystem = 0 | start.attr('data-system');
  var startPosition = 0 | start.attr('data-position');

  var form = $('#send-fleet');

  var targetGalaxy;
  var targetSystem;
  var targetPosition;
  var targetKind;

  if (this !== undefined && this.id === 'own-bodies') {
    var selected = $(this).find(':selected');
    targetGalaxy = 0 | selected.attr('data-galaxy');
    targetSystem = 0 | selected.attr('data-system');
    targetPosition = 0 | selected.attr('data-position');
    targetKind = selected.attr('data-kind');
  } else {
    targetGalaxy = 0 | $('[name="galaxy"]', form).val();
    targetSystem = 0 | $('[name="system"]', form).val();
    targetPosition = 0 | $('[name="position"]', form).val();
    targetKind = $('[name="kind"]', form).val();
  }

  $('[name="galaxy"]').val(targetGalaxy);
  $('[name="system"]').val(targetSystem);
  $('[name="position"]').val(targetPosition);
  $('[name="kind"]').val(targetKind);

  var elem = $('#own-bodies option').toArray().find(function (e) {
    var self = $(e);
    var g = 0 | self.attr('data-galaxy');
    var s = 0 | self.attr('data-system');
    var p = 0 | self.attr('data-position');
    var k = self.attr('data-kind');
    return g === targetGalaxy && s === targetSystem && p === targetPosition && k === targetKind;
  });
  if (elem === undefined) {
    elem = $('#own-bodies option:first');
  }
  $(elem).prop('selected', true);

  var factor = 0 | $('[name="factor"] option:selected', form).val();

  var maxSpeed = -1;
  $('[id^="unit_"]', form).each(function () {
    var count = 0 | this.value;
    if (count > 0) {
      var speed = 0 | $(this).attr('data-speed');
      if (maxSpeed === -1 || speed < maxSpeed) {
        maxSpeed = speed;
      }
    }
  });

  var distance = 0;
  var diff;
  if (startGalaxy !== targetGalaxy) {
    diff = Math.abs(startGalaxy - targetGalaxy);
    distance = 20000 * Math.min(diff, 5 - diff);
  } else if (startSystem !== targetSystem) {
    diff = Math.abs(startSystem - targetSystem);
    distance = 95 * Math.min(diff, 500 - diff) + 2700;
  } else if (startPosition !== targetPosition) {
    diff = Math.abs(startPosition - targetPosition);
    distance = 5 * diff + 1000;
  } else {
    distance = 5;
  }

  var duration = -1;
  if (maxSpeed > 0) {
    duration = Math.round(35000.0 / factor * Math.sqrt(10.0 * distance / maxSpeed)) + 10;
  }

  var consumption = 0;
  var capacity = 0;
  $('[id^="unit_"]', form).each(function () {
    var count = 0 | this.value;
    if (count > 0) {
      var unit = $(this);
      var speed = 0 | unit.attr('data-speed');
      var cons = 0 | unit.attr('data-consumption');
      var cap = 0 | unit.attr('data-capacity');
      var x = 0.1 * factor * Math.sqrt(maxSpeed / speed) + 1.0;
      consumption += count * (cons * distance / 35000.0) * x * x;
      capacity += count * cap;
    }
  });

  consumption = Math.round(consumption) + 1;

  var metal = +$('#metal', form).val();
  var crystal = +$('#crystal', form).val();
  var deuterium = +$('#deuterium', form).val();
  capacity -= consumption + metal + crystal + deuterium;

  $('#max-speed').text(maxSpeed > 0 ? prettyNumber(maxSpeed) : '-');
  $('#distance').text(distance > 0 ? prettyNumber(distance) : '-');
  $('#duration').text(duration > 0 ? prettyTime(duration) : '-');
  $('#consumption').text(consumption > 0 ? prettyNumber(consumption) : '-');
  $('#capacity').text(prettyNumber(capacity)).attr('data-total-capacity', capacity);
}

$('#send-fleet :input').change(update);

update();
