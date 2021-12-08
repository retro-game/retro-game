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

function galaxySubmit() {
  let galaxy = 0 | $('#galaxy-input [name="galaxy"]').val();
  if (galaxy < 1 || galaxy > 5)
    galaxy = 1;
  $('[name="galaxy"]', this).val(galaxy);

  let system = 0 | $('#system-input [name="system"]').val();
  if (system < 1 || system > 500)
    system = 1;
  $('[name="system"]', this).val(system);
}

$('#galaxy-input').submit(galaxySubmit);
$('#system-input').submit(galaxySubmit);

$('[data-spy]').click(function () {
  let reports = $('#reports');
  if (reports.children().length === 0) {
    reports.append('<tr><th>Espionage</th></tr>');
  }

  let addReport = (html) => $('#reports :first-child').first().after(html);

  const body = 0 | $($('[name="body"]')[0]).val();
  const galaxy = 0 | $(this).attr('data-galaxy');
  const system = 0 | $(this).attr('data-system');
  const position = 0 | $(this).attr('data-position');
  const kind = $(this).attr('data-kind');
  const count = 0 | $('#num-probes').val();

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
      const coordinates = [galaxy, system, position, kind[0]].join('-');
      if (data.success) {
        addReport('<tr><td>Probes were sent to ' + coordinates + ' successfully</td></tr>');
        return;
      }
      let message = 'Probes couldn\'t be sent to ' + coordinates + ', ';
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
        case 'CONCURRENCY':
          message += 'please try again'
          break;
      }
      addReport('<tr><td><font color="red">' + message + '</font></td></tr>');
    },
    error: function () {
      addReport('<tr><td><font color="red">Internal error</font></td></tr>');
    }
  });
});
