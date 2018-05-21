'use strict';

$('[data-spy]').click(function () {
  var elem = $(this);
  var body = 0 | elem.attr('data-body');
  var galaxy = 0 | elem.attr('data-galaxy');
  var system = 0 | elem.attr('data-system');
  var position = 0 | elem.attr('data-position');
  var kind = elem.attr('data-kind');
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
      if (data.success) {
        elem.prop('disabled', true);
        elem.html('&check;');
        elem.off('click');
        return;
      }
      var coordinates = [galaxy, system, position, kind[0]].join('-');
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
      alert(message);
    },
    error: function () {
      alert('Internal error');
    }
  });
});