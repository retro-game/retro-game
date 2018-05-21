'use strict';

$(document).on('keydown', function (event) {
  switch (event.which) {
    case 40: // down arrow
      $('#galaxy-prev').submit();
      break;
    case 38: // up arrow
      $('#galaxy-next').submit();
      break;
    case 37: // left arrow
      $('#system-prev').submit();
      break;
    case 39: // right arrow
      $('#system-next').submit();
      break;
    case 32: // space
      var reload = $('#galaxy-reload');
      if (reload !== undefined) {
        $('form', reload).submit();
      }
      break;
  }
});

$('#galaxy-input').submit(function () {
  var system = 0 | $('#system-input [name="system"]').val();
  $('[name="system"]', this).val(system);
});

$('#system-input').submit(function () {
  var galaxy = 0 | $('#galaxy-input [name="galaxy"]').val();
  $('[name="galaxy"]', this).val(galaxy);
});

var reports = $('#reports');

$('[data-spy]').click(function () {
  if (reports.children().length === 0) {
    reports.append('<tr><th>Espionage</th></tr>');
  }
  var body = 0 | $($('[name="body"]')[0]).val();
  var galaxy = 0 | $(this).attr('data-galaxy');
  var system = 0 | $(this).attr('data-system');
  var position = 0 | $(this).attr('data-position');
  var kind = $(this).attr('data-kind');
  var count = 0 | $('#num-probes').val();
  $.ajax({
    type: 'post',
    url: '/flights/send-probes',
    contentType: 'application/json',
    data: JSON.stringify({
      body: body,
      galaxy: galaxy,
      system: system,
      position: position,
      kind: kind,
      count: count
    }),
    success: function (data) {
      var coordinates = [galaxy, system, position, kind[0]].join('-');
      if (data.success) {
        reports.append('<tr><td>Probes were sent to ' + coordinates + ' successfully</td></tr>');
        return;
      }
      var message = 'Probes couldn\'t be sent to ' + coordinates + ', ';
      switch (data.error) {
        case 'NO_MORE_FREE_SLOTS':
          message += 'no more free flight slots';
          break;
        case 'NOT_ENOUGH_CAPACITY':
          message += 'the target is too far away';
          break;
        case 'NOT_ENOUGH_DEUTERIUM':
          message += 'you don\'t have enough fuel';
          break;
        case 'NOT_ENOUGH_UNITS':
          message += 'you don\'t have enough probes';
          break;
      }
      reports.append('<tr><td><font color="red">' + message + '</font></td></tr>');
    },
    error: function () {
      reports.append('<tr><td><font color="red">Internal error</font></td></tr>');
    }
  });
});